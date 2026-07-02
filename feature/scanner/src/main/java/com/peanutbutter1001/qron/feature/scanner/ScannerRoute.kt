package com.peanutbutter1001.qron.feature.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 스캐너 화면의 stateful 계층.
 * - ViewModel 보유, 권한 상태/런처 관리
 * - 스캔 성공 이벤트를 수집하여 네비게이션(onNavigateToResult)으로 위임
 * - 접근성 설정 이동 등 플랫폼 동작 수행
 * (기존처럼 ScanResultActivity를 직접 startActivity 하지 않는다.)
 */
@Composable
fun ScannerRoute(
    onNavigateToResult: (Long) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(viewModel) {
        viewModel.scanResultEvent.collect { id -> onNavigateToResult(id) }
    }

    ScannerScreen(
        hasCameraPermission = hasCameraPermission,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onFrameAnalyzed = viewModel::processImageProxy,
        onResume = viewModel::resumeScanning,
        onOpenAccessibilitySettings = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    )
}
