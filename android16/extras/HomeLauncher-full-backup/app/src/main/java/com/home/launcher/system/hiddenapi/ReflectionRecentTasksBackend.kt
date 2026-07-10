package com.home.launcher.system.hiddenapi

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.HardwareBuffer
import android.os.Bundle
import android.util.Log
import com.home.launcher.task.RecentTask
import com.home.launcher.task.RecentTasksBackend
import com.home.launcher.task.TaskListenerRegistration
import java.lang.reflect.Proxy

class ReflectionRecentTasksBackend(private val context: Context) : RecentTasksBackend {
    private val iAtm: Any?
        get() = HiddenApiBridge.activityTaskManager

    override fun getRecentTasks(maxNum: Int): List<RecentTask> {
        val service = iAtm ?: return emptyList<RecentTask>()
        val raw = HiddenApiBridge.invoke(service, "getRecentTasks", maxNum, RECENT_WITH_EXCLUDED, USER_CURRENT)
        val taskList = unwrapParceledList(raw) ?: return emptyList<RecentTask>()
        return parseRecentTasks(taskList)
    }

    override fun removeTask(taskId: Int): Boolean {
        val removed = HiddenApiBridge.invoke(iAtm, "removeTask", taskId) as? Boolean
        val success = removed == true
        Log.i(TAG, "removeTask($taskId) success=$success")
        return success
    }

    override fun removeAllVisibleRecentTasks(): Boolean {
        val service = iAtm ?: return false
        if (HiddenApiBridge.findCompatibleMethod(service, "removeAllVisibleRecentTasks") == null) {
            Log.w(TAG, "removeAllVisibleRecentTasks method unavailable")
            return false
        }
        return runCatching {
            val result = HiddenApiBridge.call(service, "removeAllVisibleRecentTasks")
            Log.i(TAG, "removeAllVisibleRecentTasks invoked=${result.invoked}")
            result.invoked
        }.onFailure {
            Log.e(TAG, "removeAllVisibleRecentTasks failed", it)
        }.getOrDefault(false)
    }

    override fun startTaskFromRecents(taskId: Int): Boolean {
        val service = iAtm ?: return false
        if (HiddenApiBridge.findCompatibleMethod(service, "startActivityFromRecents", taskId, Bundle()) == null) {
            Log.w(TAG, "startActivityFromRecents(int, Bundle) method unavailable")
            return false
        }
        val options = Bundle()
        val call = runCatching {
            HiddenApiBridge.call(service, "startActivityFromRecents", taskId, options)
        }.onFailure {
            Log.e(TAG, "startActivityFromRecents($taskId, Bundle) failed", it)
        }.getOrDefault(HiddenApiBridge.HiddenApiResult(invoked = false, value = null))

        val success = call.invoked
        Log.i(TAG, "startActivityFromRecents($taskId) invoked=$success result=${call.value}")
        return success
    }

    override fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean): Bitmap? {
        val rawSnapshot = HiddenApiBridge.invoke(iAtm, "getTaskSnapshot", taskId, isLowResolution)
        if (rawSnapshot == null) {
            Log.i(TAG, "getTaskSnapshot($taskId) returned null")
            return null
        }

        return runCatching {
            val snapshotClass = rawSnapshot.javaClass
            val hardwareBuffer = runCatching {
                snapshotClass.getDeclaredMethod("getHardwareBuffer").invoke(rawSnapshot) as? HardwareBuffer
            }.getOrElse {
                Log.w(TAG, "getHardwareBuffer unavailable for task $taskId; trying getSnapshot", it)
                null
            }

            val bitmap = hardwareBuffer?.let { buffer ->
                Bitmap.wrapHardwareBuffer(buffer, null).also { buffer.close() }
            }

            if (bitmap == null) {
                Log.w(TAG, "snapshot bitmap unavailable for task $taskId")
                return null
            }

            val orientation = runCatching {
                snapshotClass.getDeclaredMethod("getOrientation").invoke(rawSnapshot) as? Int
            }.getOrDefault(0) ?: 0

            if (orientation == 0) {
                bitmap
            } else {
                val matrix = Matrix()
                matrix.postRotate(orientation.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }.onFailure {
            Log.w(TAG, "getTaskSnapshot failed for task $taskId", it)
        }.getOrNull()
    }

    override fun forceStopPackage(packageName: String): Boolean {
        return runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val method = Class.forName("android.app.ActivityManager")
                .getDeclaredMethod("forceStopPackage", String::class.java)
            method.isAccessible = true
            method.invoke(am, packageName)
            Log.i(TAG, "forceStopPackage($packageName) invoked")
            true
        }.onFailure {
            Log.e(TAG, "forceStopPackage($packageName) failed", it)
        }.getOrDefault(false)
    }

    override fun registerTaskChangeListener(onChanged: () -> Unit): TaskListenerRegistration? {
        val listenerClass = HiddenApiBridge.classForName("android.app.ITaskStackListener") ?: return null
        val listener = runCatching {
            Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass)) { _, method, _ ->
                when (method.name) {
                    "onTaskStackChanged",
                    "onTaskAdded",
                    "onTaskRemoved",
                    "onTaskMovedToFront" -> onChanged()
                }
                null
            }
        }.onFailure {
            Log.e(TAG, "failed to create ITaskStackListener proxy", it)
        }.getOrNull() ?: return null

        val registered = runCatching {
            HiddenApiBridge.call(iAtm, "registerTaskStackListener", listener).invoked
        }.onFailure {
            Log.e(TAG, "registerTaskStackListener failed", it)
        }.getOrDefault(false)

        if (!registered) return null
        Log.i(TAG, "TaskStackListener registered")
        return TaskListenerRegistration {
            runCatching {
                HiddenApiBridge.invoke(iAtm, "unregisterTaskStackListener", listener)
                Log.i(TAG, "TaskStackListener unregistered")
            }.onFailure {
                Log.e(TAG, "unregisterTaskStackListener failed", it)
            }
        }
    }

    private fun unwrapParceledList(raw: Any?): List<*>? {
        return when (raw) {
            is List<*> -> raw
            null -> null
            else -> HiddenApiBridge.invoke(raw, "getList") as? List<*>
        }
    }

    private fun parseRecentTasks(taskList: List<*>): List<RecentTask> {
        val taskInfoClass = HiddenApiBridge.classForName("android.app.TaskInfo") ?: return emptyList<RecentTask>()
        val fTaskId = runCatching { taskInfoClass.getField("taskId") }.getOrNull() ?: return emptyList<RecentTask>()
        val fBaseIntent = runCatching { taskInfoClass.getField("baseIntent") }.getOrNull() ?: return emptyList<RecentTask>()
        val fUserId = runCatching { taskInfoClass.getField("userId") }.getOrNull()
        val fTaskDescription = runCatching { taskInfoClass.getField("taskDescription") }.getOrNull()

        val tasks = taskList.mapNotNull { task ->
            runCatching {
                val taskId = fTaskId.getInt(task)
                val baseIntent = fBaseIntent.get(task) as? android.content.Intent
                val userId = fUserId?.getInt(task) ?: USER_CURRENT
                val taskDescription = fTaskDescription?.get(task)
                val packageName = baseIntent?.component?.packageName ?: return@runCatching null
                val label = taskDescription?.let { description ->
                    HiddenApiBridge.invoke(description, "getLabel") as? String
                } ?: baseIntent.component?.shortClassName
                RecentTask(taskId, packageName, label, userId)
            }.onFailure {
                Log.w(TAG, "failed to parse recent task entry", it)
            }.getOrNull()
        }

        Log.i(TAG, "getRecentTasks parsed ${tasks.size}/${taskList.size} tasks")
        return tasks
    }

    private companion object {
        const val TAG = "ReflectionTasksBackend"
        const val RECENT_WITH_EXCLUDED = 1
        const val USER_CURRENT = 0
    }
}
