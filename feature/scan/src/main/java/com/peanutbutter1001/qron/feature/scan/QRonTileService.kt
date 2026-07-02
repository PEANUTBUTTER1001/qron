package com.peanutbutter1001.qron.feature.scan

import android.content.Intent
import android.service.quicksettings.TileService

class QRonTileService : TileService() {
    companion object {
        const val ACTION_REQUEST_SCREENSHOT = "com.peanutbutter1001.qron.action.REQUEST_SCREENSHOT"
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.state = android.service.quicksettings.Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }

    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()

        if (!isAccessibilityServiceEnabled()) {
            android.widget.Toast.makeText(this, "화면 캡처를 위해 '접근성 권한'을 켜주세요.", android.widget.Toast.LENGTH_LONG).show()
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                val pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE)
                startActivityAndCollapse(pendingIntent)
            } else {
                startActivityAndCollapse(intent)
            }
            return
        }

        android.widget.Toast.makeText(this, "화면 스캔 중...", android.widget.Toast.LENGTH_SHORT).show()

        val resultIntent = Intent().apply {
            setClassName(this@QRonTileService, "com.peanutbutter1001.qron.feature.result.ScanResultActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        val captureIntent = Intent(ACTION_REQUEST_SCREENSHOT).apply {
            setPackage(packageName)
            putExtra("PENDING_RESULT_INTENT", pendingIntent)
        }
        sendBroadcast(captureIntent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val componentName = android.content.ComponentName(this, QRonAccessibilityService::class.java)
        return enabledServices.contains(componentName.flattenToString())
    }
}
