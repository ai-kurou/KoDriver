package kurou.kodriver.core.texttospeechdata.windows

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

/** 短命なPowerShellプロセスの終了とタイムアウトを制御する。 */
internal class FakeSpeechCommandProcess(
    private val exitCode: Int = 0,
    private val completes: Boolean = true,
    private val waitFailure: Exception? = null,
) : Process() {
    val calls = mutableListOf<String>()

    override fun getInputStream(): InputStream = InputStream.nullInputStream()

    override fun getErrorStream(): InputStream = InputStream.nullInputStream()

    override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()

    override fun waitFor(): Int = exitCode

    override fun waitFor(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean {
        calls.add("wait:${unit.toSeconds(timeout)}")
        waitFailure?.let { throw it }
        return completes
    }

    override fun exitValue(): Int = exitCode

    override fun destroyForcibly(): Process {
        calls.add("force")
        return this
    }

    override fun destroy() {
        calls.add("destroy")
    }
}
