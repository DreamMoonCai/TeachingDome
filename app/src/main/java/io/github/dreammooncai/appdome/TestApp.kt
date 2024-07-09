package io.github.dreammooncai.appdome

internal class TestApp {
    @get:JvmName("a")
    val name = "TestApp"

    @JvmName("c")
    fun test(){
        println("test")
    }
}