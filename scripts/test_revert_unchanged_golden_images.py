"""revert-unchanged-golden-images.py のユニットテスト（一時 git リポジトリで実行する）。"""

import os
import shutil
import struct
import subprocess
import tempfile
import unittest
import zlib

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "revert-unchanged-golden-images.py")
GLOB = "*/jvmTest/snapshots/*.png"


def make_png(width, height, pixels, color_type):
    """pixels: 行優先の (r, g, b, a) タプルのリスト。color_type 2 なら alpha を捨てる。"""
    channels = 3 if color_type == 2 else 4
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # filter type 0 (None)
        for x in range(width):
            px = pixels[y * width + x]
            raw += bytes(px[:channels])

    def chunk(ctype, body):
        return struct.pack(">I", len(body)) + ctype + body + struct.pack(">I", zlib.crc32(ctype + body) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", width, height, 8, color_type, 0, 0, 0)
    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(bytes(raw)))
        + chunk(b"IEND", b"")
    )


PIXELS = [(10, 20, 30, 255), (40, 50, 60, 255), (70, 80, 90, 255), (1, 2, 3, 255)]


class RevertUnchangedGoldenImagesTest(unittest.TestCase):
    def setUp(self):
        self.repo = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.repo, True)
        self.git("init", "-q")
        self.git("config", "user.name", "test")
        self.git("config", "user.email", "test@example.com")
        self.git("config", "commit.gpgsign", "false")
        os.makedirs(os.path.join(self.repo, "app", "jvmTest", "snapshots"))
        os.makedirs(os.path.join(self.repo, "app", "build", "jvmTest", "snapshots"))
        os.makedirs(os.path.join(self.repo, "other"))

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.repo, check=True, capture_output=True).stdout

    def write(self, rel, data):
        path = os.path.join(self.repo, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "wb") as f:
            f.write(data)

    def read(self, rel):
        with open(os.path.join(self.repo, rel), "rb") as f:
            return f.read()

    def commit_all(self):
        self.git("add", "-A")
        self.git("commit", "-q", "-m", "base")

    def run_script(self, glob=GLOB):
        result = subprocess.run(
            ["python3", SCRIPT, glob], cwd=self.repo, capture_output=True, text=True
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        return result.stdout

    def status(self):
        return self.git("status", "--porcelain", "-uall").decode()

    def test_same_pixels_different_format_is_reverted(self):
        rel = "app/jvmTest/snapshots/a.png"
        rgba = make_png(2, 2, PIXELS, 6)
        self.write(rel, rgba)
        self.commit_all()
        self.write(rel, make_png(2, 2, PIXELS, 2))

        out = self.run_script()

        self.assertEqual(self.read(rel), rgba)
        self.assertEqual(self.status(), "")
        self.assertIn("戻した 1 件 / 残した 0 件", out)

    def test_different_pixels_are_kept(self):
        rel = "app/jvmTest/snapshots/b.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        changed = list(PIXELS)
        changed[3] = (0, 0, 0, 255)
        self.write(rel, make_png(2, 2, changed, 2))

        out = self.run_script()

        self.assertIn(rel, self.status())
        self.assertIn("戻した 0 件 / 残した 1 件", out)

    def test_different_size_is_kept(self):
        rel = "app/jvmTest/snapshots/size.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        self.write(rel, make_png(4, 1, PIXELS, 2))

        self.run_script()

        self.assertIn(rel, self.status())

    def test_alpha_below_255_is_kept_against_rgb(self):
        rel = "app/jvmTest/snapshots/alpha.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        translucent = list(PIXELS)
        translucent[0] = (10, 20, 30, 128)
        self.write(rel, make_png(2, 2, translucent, 6))

        self.run_script()

        self.assertIn(rel, self.status())

    def test_untracked_new_file_is_kept(self):
        rel = "app/jvmTest/snapshots/new.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))

        out = self.run_script()

        self.assertTrue(os.path.exists(os.path.join(self.repo, rel)))
        self.assertIn("?? " + rel, self.status())
        self.assertIn("戻した 0 件 / 残した 0 件", out)

    def test_glob_mismatch_is_kept(self):
        rel = "other/snapshots/c.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        self.write(rel, make_png(2, 2, PIXELS, 2))

        out = self.run_script()

        self.assertIn(rel, self.status())
        self.assertIn("戻した 0 件 / 残した 0 件", out)

    def test_build_directory_is_excluded(self):
        rel = "app/build/jvmTest/snapshots/d.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        self.write(rel, make_png(2, 2, PIXELS, 2))

        out = self.run_script()

        self.assertIn(rel, self.status())
        self.assertIn("戻した 0 件 / 残した 0 件", out)

    def test_undecodable_png_is_kept_without_error(self):
        rel = "app/jvmTest/snapshots/palette.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        self.write(rel, b"not a png at all")

        out = self.run_script()

        self.assertIn(rel, self.status())
        self.assertIn("残した 1 件", out)

    def test_head_missing_file_is_kept(self):
        rel = "app/jvmTest/snapshots/staged.png"
        self.write(rel, make_png(2, 2, PIXELS, 2))
        self.git("add", "-N", rel)  # HEAD には無いが intent-to-add で作業ツリー差分に含める

        out = self.run_script()

        self.assertTrue(os.path.exists(os.path.join(self.repo, rel)))
        self.assertIn("残した 1 件", out)

    def test_deleted_file_is_kept(self):
        rel = "app/jvmTest/snapshots/gone.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        os.remove(os.path.join(self.repo, rel))

        out = self.run_script()

        self.assertIn("残した 1 件", out)

    def test_unsupported_color_type_is_kept(self):
        rel = "app/jvmTest/snapshots/gray.png"
        self.write(rel, make_png(2, 2, PIXELS, 6))
        self.commit_all()
        # color type 0 (グレースケール) は非対応として変更ありのまま残す
        gray = bytearray(make_png(2, 2, PIXELS, 6))
        gray[25] = 0  # IHDR の color type（シグネチャ8 + 長さ4 + 型4 + 幅4 + 高さ4 + bit depth 1 の直後）を 0 に書き換える
        self.write(rel, bytes(gray))

        out = self.run_script()

        self.assertIn(rel, self.status())
        self.assertIn("残した 1 件", out)

    def test_pixel_filters_are_decoded(self):
        # 非 0 のフィルタ（Sub/Up/Average/Paeth）で符号化された画像も HEAD と比較できること
        import importlib.util

        spec = importlib.util.spec_from_file_location("revert_script", SCRIPT)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        width, height = 3, 4
        pixels = [((x * 37 + y * 11) % 256, (x * 5) % 256, (y * 70) % 256, 255) for y in range(height) for x in range(width)]
        raw_rows = []
        bpp = 4
        stride = width * bpp
        flat = [c for px in pixels for c in px]
        prev = bytearray(stride)
        for y in range(height):
            ftype = y % 5
            line = bytearray(flat[y * stride:(y + 1) * stride])
            enc = bytearray(stride)
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                b = prev[x]
                c = prev[x - bpp] if x >= bpp else 0
                if ftype == 0:
                    pred = 0
                elif ftype == 1:
                    pred = a
                elif ftype == 2:
                    pred = b
                elif ftype == 3:
                    pred = (a + b) >> 1
                else:
                    pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                    pred = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                enc[x] = (line[x] - pred) & 255
            raw_rows.append(bytes([ftype]) + bytes(enc))
            prev = line

        def chunk(ctype, body):
            return struct.pack(">I", len(body)) + ctype + body + struct.pack(">I", zlib.crc32(ctype + body) & 0xFFFFFFFF)

        png = (
            b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(b"".join(raw_rows)))
            + chunk(b"IEND", b"")
        )
        w, h, rgba = module.decode_rgba(png)
        self.assertEqual((w, h), (width, height))
        self.assertEqual(rgba, bytes(flat))


if __name__ == "__main__":
    unittest.main()
