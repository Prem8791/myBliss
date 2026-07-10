package com.home.launcher.system.hiddenapi

import android.os.IBinder
import android.util.Log
import java.lang.reflect.Method

internal object HiddenApiBridge {
    private const val TAG = "HiddenApiBridge"

    val activityTaskManager: Any? by lazy { resolveActivityTaskManager() }

    fun invoke(target: Any?, methodName: String, vararg args: Any?): Any? {
        return call(target, methodName, *args).value
    }

    fun call(target: Any?, methodName: String, vararg args: Any?): HiddenApiResult {
        if (target == null) {
            Log.w(TAG, "invoke skipped: target is null for $methodName")
            return HiddenApiResult(invoked = false, value = null)
        }
        return runCatching {
            val method = findCompatibleMethod(target, methodName, *args)
            if (method == null) {
                Log.w(TAG, "method not found: ${target.javaClass.name}.$methodName(${args.signature()})")
                HiddenApiResult(invoked = false, value = null)
            } else {
                method.isAccessible = true
                HiddenApiResult(invoked = true, value = method.invoke(target, *args))
            }
        }.onFailure {
            Log.e(TAG, "invoke failed: ${target.javaClass.name}.$methodName(${args.signature()})", it)
        }.getOrDefault(HiddenApiResult(invoked = false, value = null))
    }

    fun findCompatibleMethod(target: Any?, methodName: String, vararg args: Any?): Method? {
        if (target == null) return null
        return findCompatibleMethod(target.javaClass, methodName, args)
    }

    fun getMethod(owner: Class<*>, methodName: String, vararg argTypes: Class<*>): Method? {
        return runCatching {
            owner.getDeclaredMethod(methodName, *argTypes).also { it.isAccessible = true }
        }.onFailure {
            Log.w(TAG, "method lookup failed: ${owner.name}.$methodName", it)
        }.getOrNull()
    }

    fun classForName(name: String): Class<*>? {
        return runCatching { Class.forName(name) }
            .onFailure { Log.w(TAG, "class lookup failed: $name", it) }
            .getOrNull()
    }

    private fun resolveActivityTaskManager(): Any? {
        val viaActivityTaskManager = runCatching {
            val atmClass = Class.forName("android.app.ActivityTaskManager")
            val getService = atmClass.getDeclaredMethod("getService")
            getService.isAccessible = true
            getService.invoke(null)
        }.onSuccess {
            Log.i(TAG, "IActivityTaskManager resolved via ActivityTaskManager.getService")
        }.onFailure {
            Log.w(TAG, "ActivityTaskManager.getService failed; trying ServiceManager", it)
        }.getOrNull()

        if (viaActivityTaskManager != null) return viaActivityTaskManager

        return runCatching {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val getService = serviceManager.getDeclaredMethod("getService", String::class.java)
            getService.isAccessible = true
            val binder = getService.invoke(null, "activity_task") as? IBinder
            val asInterface = Class.forName("android.app.IActivityTaskManager\$Stub")
                .getDeclaredMethod("asInterface", IBinder::class.java)
            asInterface.isAccessible = true
            asInterface.invoke(null, binder)
        }.onSuccess {
            Log.i(TAG, "IActivityTaskManager resolved via ServiceManager")
        }.onFailure {
            Log.e(TAG, "failed to resolve IActivityTaskManager", it)
        }.getOrNull()
    }

    private fun findCompatibleMethod(owner: Class<*>, name: String, args: Array<out Any?>): Method? {
        return owner.methods.firstOrNull { method ->
            method.name == name && method.parameterTypes.accept(args)
        } ?: owner.declaredMethods.firstOrNull { method ->
            method.name == name && method.parameterTypes.accept(args)
        }
    }

    private fun Array<Class<*>>.accept(args: Array<out Any?>): Boolean {
        if (size != args.size) return false
        return indices.all { index ->
            val arg = args[index]
            arg == null || this[index].wrapPrimitive().isAssignableFrom(arg.javaClass)
        }
    }

    private fun Class<*>.wrapPrimitive(): Class<*> = when (this) {
        java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        java.lang.Byte.TYPE -> java.lang.Byte::class.java
        java.lang.Character.TYPE -> java.lang.Character::class.java
        java.lang.Double.TYPE -> java.lang.Double::class.java
        java.lang.Float.TYPE -> java.lang.Float::class.java
        java.lang.Integer.TYPE -> java.lang.Integer::class.java
        java.lang.Long.TYPE -> java.lang.Long::class.java
        java.lang.Short.TYPE -> java.lang.Short::class.java
        java.lang.Void.TYPE -> java.lang.Void::class.java
        else -> this
    }

    private fun Array<out Any?>.signature(): String =
        joinToString(",") { it?.javaClass?.simpleName ?: "null" }

    data class HiddenApiResult(
        val invoked: Boolean,
        val value: Any?
    )
}
