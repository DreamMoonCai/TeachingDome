package io.github.dreammooncai.classloaderdome_1

import android.content.Context
import android.content.ContextWrapper
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ActivityThreadClass
import com.highcapable.yukihookapi.hook.type.android.ContextImplClass
import com.highcapable.yukihookapi.hook.type.android.LoadedApkClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.dreammooncai.appdome.TestApp
import io.github.dreammooncai.yukihookapi.kt.factory.encaseKotlin
import io.github.dreammooncai.yukireflection.factory.bindProperty
import io.github.dreammooncai.yukireflection.factory.classLoader
import io.github.dreammooncai.yukireflection.factory.impl
import io.github.dreammooncai.yukireflection.factory.property
import io.github.dreammooncai.yukireflection.factory.ref
import io.github.dreammooncai.yukireflection.factory.refImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


private const val DomeAppPackageName = "io.github.dreammooncai.appdome"

@InjectYukiHookWithXposed(
    entryClassName = "HookXposedEntrance",
    isUsingXposedModuleStatus = true
)
class XposedEntrance : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "DreamLog"
            isEnable = true
        }
        isDebug = true
        isEnableDataChannel = true
    }

    override fun onHook() = encaseKotlin {
        try {
            loadAppKotlin(name = DomeAppPackageName) {
                MainScope().launch {
                    withContext(Dispatchers.IO){
                        delay(1000)
                        ClassLoaderFactory.attach(XposedEntrance::class.classLoader)
                        ClassLoaderFactory.add(appClassLoader!!)
                    }
                    YLog.debug("梦寐提示: $packageName --- 模块开始加载...")
                    val test = TestApp()
                    val name by TestApp::class.bindProperty<String>(test)
                    TestApp::name.invoke(test)//错误 ->> getName
                    TestApp::name.refImpl?.invoke(test)//ok ->> a
                    TestApp::name.toKProperty().invoke(test)//ok ->> a
                    TestApp::class.property { this.name = "name" }.get(test).string()//ok ->> a
                    TestApp::name.getter.hook().replaceTo("new")
                    YLog.debug(name)
                }
            }
        } catch (e: Exception) {
            YLog.error("根模块信息错误 :: ____", e)
        }
    }
}