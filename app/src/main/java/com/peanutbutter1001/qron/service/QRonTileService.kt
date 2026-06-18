package com.peanutbutter1001.qron.service

import android.content.Intent
import android.service.quicksettings.TileService
import androidx.annotation.RequiresPermission

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
        
        // 접근성 서비스가 켜져 있는지 확인
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

        // 결과를 띄울 액티비티에 대한 PendingIntent 생성 (백그라운드 실행 권한 위임)
        val resultIntent = Intent(this, com.peanutbutter1001.qron.ui.result.ScanResultActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            resultIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        // AccessibilityService에 캡처 요청 브로드캐스트 전송
        val captureIntent = Intent(ACTION_REQUEST_SCREENSHOT).apply {
            setPackage(packageName)
            putExtra("PENDING_RESULT_INTENT", pendingIntent)
        }
        sendBroadcast(captureIntent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val componentName = android.content.ComponentName(this, QRonAccessibilityService::class.java)
        return enabledServices.contains(componentName.flattenToString())
    }
}
