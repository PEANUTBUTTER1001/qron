package com.peanutbutter1001.qron.feature.scanner

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview as UiPreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 스캐너 화면의 stateless 계층.
 * ViewModel/Navigation/Intent 를 직접 참조하지 않고 콜백만 받는다.
 * 카메라 프리뷰(AndroidView)는 side-effect라 이 계층에 두되, 상태는 전부 호이스팅한다.
 */
@Composable
fun ScannerScreen(
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onFrameAnalyzed: (ImageProxy) -> Unit,
    onResume: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // 결과 화면(destination)에서 돌아왔을 때 스캔 재개.
    // NavHost 안에서는 LocalLifecycleOwner가 해당 destination의 back stack entry이므로
    // 복귀 시 ON_RESUME이 발생한다.
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(onFrameAnalyzed = onFrameAnalyzed)
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("카메라 권한이 필요합니다.", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRequestPermission) { Text("권한 허용하기") }
                }
            }
        }

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "QR 코드를 사각형 안에 맞춰주세요",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .padding(bottom = 50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("현재 화면 스캔 안내 (접근성 권한)")
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(onFrameAnalyzed: (ImageProxy) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            onFrameAnalyzed(imageProxy)
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("ScannerScreen", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@UiPreview
@Composable
private fun ScannerScreenNoPermissionPreview() {
    ScannerScreen(
        hasCameraPermission = false,
        onRequestPermission = {},
        onFrameAnalyzed = {},
        onResume = {},
        onOpenAccessibilitySettings = {}
    )
}
