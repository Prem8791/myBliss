package com.home.launcher.task

import android.graphics.Bitmap
import android.util.Log

/**
 * Placeholder for the Android 14+ TaskOrganizer/WM Shell backend.
 *
 * This class intentionally does not register a TaskOrganizer yet. It defines the replacement
 * boundary for the reflection backend so both implementations can coexist during migration.
 */
class TaskOrganizerRecentTasksBackend : RecentTasksBackend {
    override fun getRecentTasks(maxNum: Int): List<RecentTask> {
        Log.w(TAG, "TaskOrganizer backend is not implemented yet")
        return emptyList<RecentTask>()
    }

    override fun removeTask(taskId: Int): Boolean = false

    override fun removeAllVisibleRecentTasks(): Boolean = false

    override fun startTaskFromRecents(taskId: Int): Boolean = false

    override fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean): Bitmap? = null

    override fun forceStopPackage(packageName: String): Boolean = false

    override fun registerTaskChangeListener(onChanged: () -> Unit): TaskListenerRegistration? = null

    private companion object {
        const val TAG = "TaskOrganizerBackend"
    }
}
