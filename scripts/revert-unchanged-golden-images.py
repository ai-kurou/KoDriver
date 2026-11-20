#!/usr/bin/env python3
"""作業ツリー上の変更済み golden PNG のうち、HEAD と画素が同一のものを git checkout で戻す。

形式だけ変わった（例: RGBA -> RGB）画像を差分に含めないために、CI の再記録後・git add の前に実行する。
リポジトリのルートをカレントディレクトリにして実行すること。

使い方: python3 scripts/revert-unchanged-golden-images.py <snapshotPathGlob>
  例: python3 scripts/revert-unchanged-golden-images.py "*/jvmTest/snapshots/*.png"

判定:
- 対象は `git diff --name-only` で得られる変更済みの追跡ファイルのうち、"./" + パス が glob に一致し、
  `/build/` を含まないもの。未追跡の新規ファイルは対象外（触らない）。
- HEAD 版と作業ツリー版の幅・高さ・全画素（RGB は alpha=255 として扱う）が一致すれば戻す。
- 不一致・デコード不能（8bit 非インターレース・color type 2/6 以外など）・HEAD に無いファイルは残す。
"""

import fnmatch
import struct
import subprocess
import sys
import zlib


class UnsupportedPng(Exception):
    pass


def git_bytes(*args):
    return subprocess.run(["git", *args], check=True, capture_output=True).stdout


def decode_rgba(data):
    """PNG を (幅, 高さ, RGBA バイト列) にデコードする。非対応形式は UnsupportedPng。"""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise UnsupportedPng("not a PNG")
    pos = 8
    idat = []
    width = height = None
    while pos < len(data):
        length, ctype = struct.unpack(">I4s", data[pos:pos + 8])
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if ctype == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(">IIBBBBB", chunk)
        elif ctype == b"IDAT":
            idat.append(chunk)
    if width is None:
        raise UnsupportedPng("no IHDR")
    if bit_depth != 8 or interlace != 0 or color_type not in (2, 6):
        raise UnsupportedPng("unsupported format")

    bpp = 3 if color_type == 2 else 4
    stride = width * bpp
    raw = zlib.decompress(b"".join(idat))
    if len(raw) != height * (stride + 1):
        raise UnsupportedPng("unexpected data size")

    out = bytearray()
    prev = bytearray(stride)
    i = 0
    for _ in range(height):
        ftype = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if ftype == 1:
                line[x] = (line[x] + a) & 255
            elif ftype == 2:
                line[x] = (line[x] + b) & 255
            elif ftype == 3:
                line[x] = (line[x] + ((a + b) >> 1)) & 255
            elif ftype == 4:
                pa = abs(b - c)
                pb = abs(a - c)
                pc = abs(a + b - 2 * c)
                pred = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 255
            elif ftype != 0:
                raise UnsupportedPng("unknown filter")
        out += line
        prev = line

    if bpp == 4:
        return width, height, bytes(out)
    n = width * height
    rgba = bytearray(n * 4)
    rgba[0::4] = out[0::3]
    rgba[1::4] = out[1::3]
    rgba[2::4] = out[2::3]
    rgba[3::4] = b"\xff" * n
    return width, height, bytes(rgba)


def is_pixel_identical(path):
    """HEAD 版と作業ツリー版の画素が一致するか。判定できない場合は False（残す）。"""
    try:
        head = git_bytes("show", f"HEAD:{path}")
        with open(path, "rb") as f:
            work = f.read()
    except (subprocess.CalledProcessError, OSError):
        return False
    if head == work:
        return True
    try:
        return decode_rgba(head) == decode_rgba(work)
    except Exception:  # 壊れた PNG・非対応形式は変更ありとして残す
        return False


def main():
    if len(sys.argv) != 2:
        print("usage: revert-unchanged-golden-images.py <snapshotPathGlob>", file=sys.stderr)
        return 2
    glob = sys.argv[1]

    changed = git_bytes("diff", "--name-only", "-z").split(b"\0")
    targets = []
    for raw in changed:
        if not raw:
            continue
        path = raw.decode("utf-8")
        candidate = "./" + path
        if "/build/" in candidate or not fnmatch.fnmatchcase(candidate, glob):
            continue
        targets.append(path)

    reverted = 0
    kept = 0
    for path in targets:
        if is_pixel_identical(path):
            subprocess.run(["git", "checkout", "--", path], check=True)
            reverted += 1
            print(f"revert: {path}")
        else:
            kept += 1
            print(f"keep:   {path}")

    print(f"戻した {reverted} 件 / 残した {kept} 件")
    return 0


if __name__ == "__main__":
    sys.exit(main())
