package kurou.kodriver.core.windowssharedmemory.datasource

import com.sun.jna.Function
import com.sun.jna.Library
import com.sun.jna.NativeLibrary
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class Kernel32FileMappingTest {
    private val library = mockk<NativeLibrary>()

    @Test
    fun `INSTANCEは初回参照までロードせず以後は同じインスタンスを返す`() {
        val mappingClass = isolatedMappingClass()
        val options = libraryOptions(mappingClass)
        mockkStatic(NativeLibrary::class)
        try {
            every { NativeLibrary.getInstance("kernel32", options) } returns library
            val companion = mappingClass.getField("Companion").get(null)
            val getter = companion.javaClass.getMethod("getINSTANCE")

            verify(exactly = 0) { NativeLibrary.getInstance("kernel32", options) }
            val instance = getter.invoke(companion)
            val handler = assertIs<Library.Handler>(Proxy.getInvocationHandler(instance))
            assertSame(mappingClass, handler.interfaceClass)
            assertSame(library, handler.nativeLibrary)
            assertSame(instance, getter.invoke(companion))

            verify(exactly = 1) { NativeLibrary.getInstance("kernel32", options) }
            confirmVerified(NativeLibrary::class)
        } finally {
            unmockkStatic(NativeLibrary::class)
        }
    }

    @Test
    fun `INSTANCEはロード失敗を伝播し次の参照で再試行して成功値を保持する`() {
        val mappingClass = isolatedMappingClass()
        val options = libraryOptions(mappingClass)
        val failure = UnsatisfiedLinkError("kernel32 unavailable")
        mockkStatic(NativeLibrary::class)
        try {
            every { NativeLibrary.getInstance("kernel32", options) } throws failure andThen library
            val companion = mappingClass.getField("Companion").get(null)
            val getter = companion.javaClass.getMethod("getINSTANCE")

            val exception = assertFailsWith<InvocationTargetException> { getter.invoke(companion) }
            assertSame(failure, exception.targetException)
            val instance = getter.invoke(companion)
            val handler = assertIs<Library.Handler>(Proxy.getInvocationHandler(instance))
            assertSame(mappingClass, handler.interfaceClass)
            assertSame(library, handler.nativeLibrary)
            assertSame(instance, getter.invoke(companion))

            verify(exactly = 2) { NativeLibrary.getInstance("kernel32", options) }
            confirmVerified(NativeLibrary::class)
        } finally {
            unmockkStatic(NativeLibrary::class)
        }
    }

    private fun isolatedMappingClass(): Class<out Library> {
        val original = Kernel32FileMapping::class.java
        val isolated = IsolatedMappingClassLoader(original).loadClass(original.name)
        assertNotSame(original, isolated)
        return isolated.asSubclass(Library::class.java)
    }

    private fun libraryOptions(mappingClass: Class<out Library>): Map<String, Any> =
        mapOf(
            Library.OPTION_CALLING_CONVENTION to Function.C_CONVENTION,
            Library.OPTION_CLASSLOADER to mappingClass.classLoader,
        )

    /** lazyの保持値を通常のクラスや他のテストへ残さないため、対象とその生成クラスだけを隔離する。 */
    private class IsolatedMappingClassLoader(
        private val original: Class<*>,
    ) : ClassLoader(original.classLoader) {
        override fun loadClass(
            name: String,
            resolve: Boolean,
        ): Class<*> {
            if (name != original.name && !name.startsWith("${original.name}$")) {
                return super.loadClass(name, resolve)
            }
            return synchronized(getClassLoadingLock(name)) {
                val loaded =
                    findLoadedClass(name) ?: run {
                        val resource = name.replace('.', '/') + ".class"
                        val bytes = requireNotNull(parent.getResourceAsStream(resource)).use { it.readBytes() }
                        defineClass(name, bytes, 0, bytes.size)
                    }
                if (resolve) resolveClass(loaded)
                loaded
            }
        }
    }
}
