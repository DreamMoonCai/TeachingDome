package io.github.dreammooncai.classloaderdome_1

import dalvik.system.PathClassLoader

open class DefaultClassLoader:PathClassLoader {
    constructor(dexPath: String, parent: ClassLoader?) : super(dexPath, parent)
    constructor(dexPath: String, librarySearchPath: String?, parent: ClassLoader?) : super(
        dexPath,
        librarySearchPath,
        parent
    )

    @Synchronized
    override fun loadClass(name: String): Class<*> {
        fun repeatLoadClass(repeatFind: ClassLoader?): Class<*>? {
            val repeat = ClassLoaderFactory.getRepeat(repeatFind)
            return if (repeat != null) {
                repeatLoadClass(repeat.parent)
            } else {
                try {
                    repeatFind?.loadClass(name)
                } catch (e: ClassNotFoundException) {
                    null
                }
            }
        }

        return ClassLoaderFactory.dynamicClassLoader.firstNotNullOfOrNull { repeatLoadClass(it) }
            ?: try {
                super.loadClass(name)
            } catch (e: ClassNotFoundException) {
                throw e
            }
    }
}