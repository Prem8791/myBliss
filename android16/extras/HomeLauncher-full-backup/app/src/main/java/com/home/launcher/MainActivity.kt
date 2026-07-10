package com.home.launcher

import android.app.ActivityManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.provider.CalendarContract
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.adapter.RecentAppsAdapter
import com.home.launcher.adapter.RecentTaskTile
import kotlin.math.roundToInt
import com.home.launcher.data.AppIndex
import com.home.launcher.service.NotificationEntry
import com.home.launcher.service.NotificationListener
import com.home.launcher.task.RecentTasksRepository
import com.home.launcher.task.TaskListenerRegistration
import com.home.launcher.ui.AppListOverlay
import com.home.launcher.ui.SystemStatsBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Layout references
    private lateinit var leftColumn: LinearLayout
    private lateinit var centerColumn: LinearLayout
    private lateinit var rightColumn: LinearLayout
    private lateinit var rootLayout: LinearLayout
    private lateinit var recentAppsGrid: RecyclerView
    private lateinit var recentAppsAdapter: RecentAppsAdapter
    private lateinit var killAllButton: TextView
    private lateinit var notificationContainer: LinearLayout
    private lateinit var notificationPlaceholder: TextView
    private lateinit var notificationScroll: View
    private lateinit var todayDate: TextView
    private lateinit var todayEvent: TextView
    private lateinit var todayTasks: TextView

    // Data
    private val appIndex by lazy { AppIndex(this) }
    private val recentTasksRepository by lazy { RecentTasksRepository(this) }
    private lateinit var statsBar: SystemStatsBar
    private var taskListenerRegistration: TaskListenerRegistration? = null
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshRecentTasks()
            handler.postDelayed(this, 3000)
        }
    }

    // Notification expansion state
    private var expandedPackage: String? = null
    private var expandedContainer: LinearLayout? = null
    private val notificationRefreshRunnable = object : Runnable {
        override fun run() {
            updateNotificationIcons()
            handler.postDelayed(this, 2000)
        }
    }

    private val notifReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNotificationIcons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setContentView(R.layout.activity_main)

        initViews()
        initAlphabetColumns()
        initRecentApps()
        initKillAll()
        initSettings()
        initNotifications()
        initToday()
        initStatsBar()

        appIndex.load()
        updateAlphabetAvailability()
    }

    override fun onResume() {
        super.onResume()
        leftColumn.visibility = View.VISIBLE
        rightColumn.visibility = View.VISIBLE
        refreshRecentTasks()
        registerTaskListener()
        handler.postDelayed(refreshRunnable, 3000)
        statsBar.start()
        handler.postDelayed(notificationRefreshRunnable, 2000)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(notifReceiver, IntentFilter(NotificationListener.ACTION_NOTIF_POSTED))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(notifReceiver, IntentFilter(NotificationListener.ACTION_NOTIF_REMOVED))
    }

    override fun onPause() {
        super.onPause()
        unregisterTaskListener()
        handler.removeCallbacks(refreshRunnable)
        statsBar.stop()
        handler.removeCallbacks(notificationRefreshRunnable)

        LocalBroadcastManager.getInstance(this).unregisterReceiver(notifReceiver)
    }

    // ============ VIEW INITIALIZATION ============

    private fun initViews() {
        rootLayout = findViewById<LinearLayout>(R.id.rootLayout)!!
        leftColumn = findViewById<LinearLayout>(R.id.leftColumn)!!
        centerColumn = findViewById<LinearLayout>(R.id.centerColumn)!!
        rightColumn = findViewById<LinearLayout>(R.id.rightColumn)!!
    }

    // ============ ALPHABET COLUMNS ============

    private fun initAlphabetColumns() {
        val leftLetters = listOf('A','B','C','D','E','F','G','H','I','J','K','L','M')
        val rightLetters = listOf('N','O','P','Q','R','S','T','U','V','W','X','Y','Z')

        for (letter in leftLetters) {
            val id = resources.getIdentifier("letter_$letter", "id", packageName)
            findViewById<TextView>(id)?.setOnClickListener { onLetterTap(letter) }
        }
        findViewById<TextView>(R.id.letter_HASH)?.setOnClickListener { onLetterTap('#') }

        for (letter in rightLetters) {
            val id = resources.getIdentifier("letter_$letter", "id", packageName)
            findViewById<TextView>(id)?.setOnClickListener { onLetterTap(letter) }
        }
        findViewById<TextView>(R.id.letter_STAR)?.setOnClickListener { onLetterTap('*') }
    }

    private fun updateAlphabetAvailability() {
        val available = appIndex.getAvailableLetters()
        val letterIds = ('A'..'Z').map { it to resources.getIdentifier("letter_$it", "id", packageName) } +
            listOf('#' to R.id.letter_HASH, '*' to R.id.letter_STAR)
        for ((letter, id) in letterIds) {
            val tv = findViewById<TextView>(id) ?: continue
            if (letter in available) {
                tv.setTextColor(resources.getColor(R.color.alphabet_letter, theme))
            } else {
                tv.setTextColor(resources.getColor(R.color.alphabet_letter_disabled, theme))
            }
        }
    }

    private fun onLetterTap(letter: Char) {
        val apps = appIndex.getAppsForLetter(letter)
        val title = when (letter) {
            '#' -> getString(R.string.numbers)
            '*' -> getString(R.string.favourites)
            else -> "\"$letter\""
        }
        if (apps.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_apps, letter.toString()), Toast.LENGTH_SHORT).show()
            return
        }
        leftColumn.visibility = View.GONE
        rightColumn.visibility = View.GONE
        AppListOverlay.show(this, centerColumn, title, apps) {
            leftColumn.visibility = View.VISIBLE
            rightColumn.visibility = View.VISIBLE
        }
    }

    // ============ RECENT APPS ============

    private fun initRecentApps() {
        recentAppsGrid = findViewById<RecyclerView>(R.id.recentAppsGrid)!!
        killAllButton = findViewById<TextView>(R.id.killAllButton)!!

        recentAppsAdapter = RecentAppsAdapter(
            context = this,
            onClose = { tile -> closeTask(tile) },
            onResume = { tile -> resumeTask(tile) },
            thumbnailLoader = { taskId -> recentTasksRepository.getTaskSnapshot(taskId, false) }
        )

        val glm = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recentAppsGrid.layoutManager = glm
        recentAppsGrid.adapter = recentAppsAdapter

        recentAppsGrid.post {
            val gridH = recentAppsGrid.height
            val padTop = recentAppsGrid.paddingTop
            val padBottom = recentAppsGrid.paddingBottom
            val gap = 0
            val rows = 3
            val h = ((gridH - padTop - padBottom - (rows - 1) * gap) / rows.toFloat()).roundToInt()
            if (h > 0) {
                recentAppsAdapter.setTileHeight(h)
                recentAppsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun refreshRecentTasks() {
        val tasks = recentTasksRepository.getRecentTasks(30).filter { it.packageName != packageName }
        val tiles = tasks.map { task ->
            RecentTaskTile(
                taskId = task.taskId,
                packageName = task.packageName,
                appLabel = task.label,
                userId = task.userId
            )
        }
        runOnUiThread {
            recentAppsAdapter.updateTiles(tiles)
        }
    }

    private fun closeTask(tile: RecentTaskTile) {
        recentTasksRepository.removeTask(tile.taskId)
        recentAppsAdapter.removeTile(tile.taskId)
    }

    private fun resumeTask(tile: RecentTaskTile) {
        val started = recentTasksRepository.startTaskFromRecents(tile.taskId)
        if (!started) {
            try {
                val intent = packageManager.getLaunchIntentForPackage(tile.packageName)
                if (intent != null) startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to launch app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initKillAll() {
        killAllButton.setOnClickListener {
            val tiles = (0 until recentAppsAdapter.itemCount).map { recentAppsAdapter.getTileAt(it) }
            if (tiles.isEmpty()) {
                Toast.makeText(this, "No apps to kill", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Kill All Apps")
                .setMessage("This will force-stop ${tiles.size} app(s). Continue?")
                .setPositiveButton("Kill All") { _, _ ->
                    var killedCount = 0
                    for (tile in tiles) {
                        if (recentTasksRepository.forceStopPackage(tile.packageName)) {
                            killedCount++
                        }
                        recentTasksRepository.removeTask(tile.taskId)
                    }
                    recentTasksRepository.removeAllVisibleRecentTasks()
                    recentAppsAdapter.clearAll()
                    Toast.makeText(this, "Killed $killedCount app(s)", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ============ SETTINGS ============

    private fun initSettings() {
        findViewById<View>(R.id.settingsButton)!!.setOnClickListener { showSettingsDialog() }
    }

    private fun showSettingsDialog() {
        val items = arrayOf(
            "Manage Permissions",
            "Set Wallpaper",
            "Battery Settings",
            "Notification Access"
        )

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Home Settings")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openAppPermissions()
                    1 -> openWallpaperPicker()
                    2 -> startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    3 -> openNotificationAccess()
                }
            }
            .show()
    }

    private fun openAppPermissions() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openNotificationAccess() {
        try {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open notification settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWallpaperPicker() {
        try {
            val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                startActivity(Intent.createChooser(intent, "Select wallpaper"))
            } catch (e2: Exception) {
                Toast.makeText(this, "Wallpaper picker not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============ TASK LISTENER ============

    private fun registerTaskListener() {
        if (taskListenerRegistration == null) {
            taskListenerRegistration = recentTasksRepository.registerTaskChangeListener {
                runOnUiThread { refreshRecentTasks() }
            }
        }
    }

    private fun unregisterTaskListener() {
        taskListenerRegistration?.unregister()
        taskListenerRegistration = null
    }

    // ============ NOTIFICATIONS ============

    private fun initNotifications() {
        notificationContainer = findViewById<LinearLayout>(R.id.notificationIcons)!!
        notificationPlaceholder = findViewById<TextView>(R.id.notificationPlaceholder)!!
        notificationScroll = findViewById<View>(R.id.notificationScroll)!!

        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val ourComponent = "$packageName/.service.NotificationListener"
        val isEnabled = enabledListeners?.contains(ourComponent) == true
        Log.d("NotifDiag", "enabled_notification_listeners=$enabledListeners")
        Log.d("NotifDiag", "ourComponent=$ourComponent isEnabled=$isEnabled")
    }

    private fun updateNotificationIcons() {
        val packages = NotificationListener.getActivePackages()
        notificationContainer.removeAllViews()

        if (!NotificationListener.isConnected() && packages.isEmpty()) {
            notificationPlaceholder.visibility = View.VISIBLE
            notificationPlaceholder.text = "Notifications off — tap ⚙ to enable"
            notificationPlaceholder.setOnClickListener {
                openNotificationAccess()
            }
            return
        }

        notificationPlaceholder.visibility = if (packages.isEmpty()) View.VISIBLE else View.GONE
        notificationPlaceholder.text = getString(R.string.no_notifications)
        notificationPlaceholder.setOnClickListener(null)

        val currentExpandedPackage = expandedPackage
        if (currentExpandedPackage != null && packages.containsKey(currentExpandedPackage)) {
            showExpandedNotifications(currentExpandedPackage)
            return
        }
        expandedPackage = null
        expandedContainer = null

        for ((pkg, count) in packages) {
            val iconView = createNotificationIcon(pkg, count)
            notificationContainer.addView(iconView)
        }
    }

    private fun createNotificationIcon(pkg: String, count: Int): View {
        val iconSize = (36 * resources.displayMetrics.density).toInt()
        val frame = LinearLayout(this)
        frame.layoutParams = LinearLayout.LayoutParams(iconSize + 8, iconSize + 8)
        frame.gravity = Gravity.CENTER
        frame.orientation = LinearLayout.VERTICAL
        frame.setOnClickListener {
            expandedPackage = pkg
            updateNotificationIcons()
        }

        val icon = ImageView(this)
        icon.layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
        try {
            icon.setImageDrawable(packageManager.getApplicationIcon(pkg))
        } catch (e: Exception) {
            icon.setImageDrawable(null)
        }

        val badge = TextView(this)
        badge.layoutParams = LinearLayout.LayoutParams(
            (14 * resources.displayMetrics.density).toInt(),
            (14 * resources.displayMetrics.density).toInt()
        )
        badge.gravity = Gravity.CENTER
        badge.text = if (count > 99) "99+" else count.toString()
        badge.setTextColor(android.graphics.Color.WHITE)
        badge.textSize = 8f
        badge.setBackgroundResource(R.drawable.close_button_bg)
        badge.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val badgeContainer = FrameLayout(this)
        badgeContainer.layoutParams = LinearLayout.LayoutParams(iconSize + 8, iconSize + 8)
        badgeContainer.addView(icon)
        val badgeLp = FrameLayout.LayoutParams(
            (16 * resources.displayMetrics.density).toInt(),
            (16 * resources.displayMetrics.density).toInt()
        )
        badgeLp.gravity = Gravity.TOP or Gravity.END
        badgeContainer.addView(badge, badgeLp)
        frame.addView(badgeContainer)

        return frame
    }

    private fun showExpandedNotifications(pkg: String) {
        notificationContainer.removeAllViews()

        val entries = NotificationListener.getNotificationsForPackage(pkg)
        val container = LinearLayout(this)
        container.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        container.orientation = LinearLayout.VERTICAL
        container.setGravity(Gravity.TOP)
        container.setBackgroundColor(android.graphics.Color.parseColor("#CC1A2A4E"))
        container.setPadding(8, 4, 8, 4)

        for (entry in entries) {
            val notifView = createNotificationCard(entry)
            container.addView(notifView)
        }

        notificationContainer.addView(container)
        expandedContainer = container

        notificationScroll.setOnClickListener { v ->
            if (expandedPackage != null) {
                expandedPackage = null
                updateNotificationIcons()
            }
        }
    }

    private fun createNotificationCard(entry: NotificationEntry): View {
        val card = LinearLayout(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        card.orientation = LinearLayout.HORIZONTAL
        card.setPadding(8, 4, 8, 4)
        card.setBackgroundColor(android.graphics.Color.parseColor("#332A4A6E"))

        val icon = ImageView(this)
        icon.layoutParams = LinearLayout.LayoutParams(32, 32)
        icon.setImageDrawable(entry.icon)
        card.addView(icon)

        val textLayout = LinearLayout(this)
        textLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.setPadding(8, 0, 0, 0)

        val titleView = TextView(this)
        titleView.text = entry.title ?: entry.appName
        titleView.setTextColor(android.graphics.Color.WHITE)
        titleView.textSize = 12f
        titleView.maxLines = 1
        textLayout.addView(titleView)

        val bodyView = TextView(this)
        bodyView.text = entry.text ?: ""
        bodyView.setTextColor(android.graphics.Color.parseColor("#AAFFFFFF"))
        bodyView.textSize = 10f
        bodyView.maxLines = 2
        textLayout.addView(bodyView)

        card.addView(textLayout)

        val timeView = TextView(this)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeView.text = sdf.format(Date(entry.timestamp))
        timeView.setTextColor(android.graphics.Color.parseColor("#88FFFFFF"))
        timeView.textSize = 9f
        card.addView(timeView)

        val dismissBtn = TextView(this)
        val btnSize = (28 * resources.displayMetrics.density).toInt()
        dismissBtn.layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
        dismissBtn.text = "✕"
        dismissBtn.setTextColor(android.graphics.Color.parseColor("#AAFFFFFF"))
        dismissBtn.textSize = 12f
        dismissBtn.gravity = Gravity.CENTER
        dismissBtn.setBackgroundResource(R.drawable.close_button_bg)
        dismissBtn.setOnClickListener {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            NotificationListener.dismiss(entry.key, nm)
            updateNotificationIcons()
        }
        card.addView(dismissBtn)

        card.setOnClickListener {
            val intent = entry.contentIntent
            if (intent != null) {
                try {
                    intent.send()
                } catch (e: Exception) {
                    try {
                        startActivity(packageManager.getLaunchIntentForPackage(entry.packageName))
                    } catch (e2: Exception) {}
                }
            }
        }

        return card
    }

    // ============ TODAY SECTION ============

    private fun initToday() {
        todayDate = findViewById<TextView>(R.id.todayDate)!!
        todayEvent = findViewById<TextView>(R.id.todayEvent)!!
        todayTasks = findViewById<TextView>(R.id.todayTasks)!!
        refreshToday()
    }

    private fun refreshToday() {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        todayDate.text = sdf.format(Date())

        try {
            val resolver = contentResolver
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.timeInMillis

            val cursor = resolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART),
                "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                arrayOf(dayStart.toString(), dayEnd.toString()),
                "${CalendarContract.Events.DTSTART} ASC LIMIT 3"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val events = mutableListOf<String>()
                do {
                    val title = cursor.getString(0)
                    val start = cursor.getLong(1)
                    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(start))
                    events.add("$time $title")
                } while (cursor.moveToNext())
                todayEvent.text = events.joinToString("\n")
                cursor.close()
            } else {
                todayEvent.text = "No events today"
            }
        } catch (e: Exception) {
            todayEvent.text = "Calendar access needed"
        }

        try {
            val resolver = contentResolver
            val cursor = resolver.query(
                CalendarContract.Reminders.CONTENT_URI,
                arrayOf(CalendarContract.Reminders.TITLE, CalendarContract.Reminders.MINUTES),
                "${CalendarContract.Reminders.MINUTES} >= 0",
                null,
                "${CalendarContract.Reminders.MINUTES} ASC LIMIT 3"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val tasks = mutableListOf<String>()
                do {
                    val title = cursor.getString(0) ?: "Task"
                    tasks.add("☐ $title")
                } while (cursor.moveToNext())
                todayTasks.text = tasks.joinToString("\n")
                cursor.close()
            } else {
                todayTasks.text = ""
            }
        } catch (e: Exception) {
            todayTasks.text = ""
        }
    }

    // ============ SYSTEM STATS ============

    private fun initStatsBar() {
        statsBar = SystemStatsBar(
            context = this,
            batteryView = findViewById<TextView>(R.id.statBattery)!!,
            ramView = findViewById<TextView>(R.id.statRam)!!,
            cpuView = findViewById<TextView>(R.id.statCpu)!!,
            tempView = findViewById<TextView>(R.id.statTemp)!!,
            storageView = findViewById<TextView>(R.id.statStorage)!!
        )

        findViewById<LinearLayout>(R.id.statusStatsCenter)!!.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        }
    }
}
