package io.github.dreammooncai.classloaderdome_1

import com.highcapable.yukihookapi.hook.factory.extends
import com.highcapable.yukihookapi.hook.factory.toClass
import dalvik.system.BaseDexClassLoader
import io.github.dreammooncai.yukihookapi.kt.factory.field
import io.github.dreammooncai.yukihookapi.kt.factory.method
import io.github.dreammooncai.yukireflection.factory.classLoader
import java.io.File
import java.lang.reflect.Array
import java.util.Collections

object ClassLoaderFactory {
    @get:Synchronized
    val dynamicClassLoader: MutableList<ClassLoader> =
        Collections.synchronizedList(mutableListOf<ClassLoader>())

    /**
     * 增加动态加载器
     *
     * @param clr 被增加的类加载器
     */
    @Synchronized
    fun add(clr: ClassLoader) {
        dynamicClassLoader.add(clr)
    }

    /**
     * 将指定 [BaseDexClassLoader] 加载器置换为 [newAttachLoader]
     *
     * 针对模块时推荐使用多线程的方式延时加载避免当前正在使用的类出现丢失的问题
     *
     * @param baseClassLoader 被附加或置换的 [BaseDexClassLoader]
     * @param newAttachLoader 所需新的 [ClassLoader] 默认为 [DefaultClassLoader]
     */
    fun attach(
        baseClassLoader: ClassLoader? = ClassLoaderFactory::class.classLoader,
        newAttachLoader: (dexPath: String, librarySearchPath: String, parent: ClassLoader?) -> ClassLoader? =
            { dexPath, librarySearchPath, parent ->
                DefaultClassLoader(
                    dexPath,
                    librarySearchPath,
                    parent
                )
            }
    ) {
        if (baseClassLoader is DefaultClassLoader) return
        require(baseClassLoader is BaseDexClassLoader) { "baseClassLoader must be BaseDexClassLoader" }

        //获取所有dex列表实例
        val pathList = BaseDexClassLoader::class.field { name = "pathList" }.get(baseClassLoader).any() ?: return

        //获取其中dex路径列表
        val dexPaths = pathList::class.method { name = "getDexPaths" }.get(pathList).list<String>()
        //获取其中native库路径列表
        val nativeLibraryDirectories =
            pathList::class.method { name = "getNativeLibraryDirectories" }.get(pathList)
                .list<File>()
        //创建自定义类加载器并附加已有所有相关列表
        val newLoader = newAttachLoader(
            dexPaths.joinToString(File.pathSeparator),
            nativeLibraryDirectories.joinToString(File.pathSeparator),
            baseClassLoader.parent
        ) ?: return

        if (getRepeat(baseClassLoader, newLoader::class.java) != null) return

        //修改原类加载器的父类加载器以达到附加加载器的目的
        ClassLoader::class.field { name = "parent" }.get(baseClassLoader).set(newLoader)

        //清除原加载器dex列表实例的上下文类加载器
        pathList::class.field { name = "definingContext" }.get(pathList).set(null)
        //清除原加载器dex列表实例的native库路径列表
        pathList::class.field { name = "nativeLibraryDirectories" }.get(pathList)
            .set(mutableListOf<File>())
        pathList::class.field { name = "dexElements" }.get(pathList).array<Any>().forEach {
            val dexFile = it::class.field { name = "dexFile" }.get(it).any() ?: return@forEach
            dexFile::class.method { name = "close" }.get(dexFile).call()
        }
        //清除原加载器dex列表实例的dex元素列表
        pathList::class.field { name = "dexElements" }.get(pathList)
            .set(Array.newInstance("dalvik.system.DexPathList\$Element".toClass(), 0))
    }

    /**
     * 获取指定类加载器其祖父链上重复的加载器,引起重复的孩子,未找到重复则返回Null
     *
     * @param target 被指定的检测起始的加载器
     * @param repeat 被指定的检测重复的加载器 [Class]
     * @return 如果有则返回重复的孩子,否则Null
     */
    @JvmStatic
    fun getRepeat(target: ClassLoader?, repeat: Class<*> = DefaultClassLoader::class.java): ClassLoader? {
        if (target == null)return null
        if (target::class.java extends repeat) return target
        tailrec fun repeat(parent: ClassLoader?): ClassLoader? {
            return if (parent == null) null else if (parent.parent != null && parent.parent::class.java extends repeat) parent else repeat(
                parent.parent
            )
        }

        return repeat(target)
    }
}