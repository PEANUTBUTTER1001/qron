package com.peanutbutter1001.qron.ui.result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanResultActivity : ComponentActivity() {
    
    private val viewModel: ScanResultViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val result by viewModel.uiState.collectAsState()
            
            MaterialTheme {
                ModalBottomSheet(
                    onDismissRequest = { finish() }
                ) {
                    result?.let { qrData ->
                        ResultContent(
                            qrResult = qrData,
                            onOpenLink = {
                                if (qrData.type == QRType.URL) {
                                    openUrl(qrData.rawValue)
                                }
                            },
                            onCopy = {
                                copyToClipboard(qrData.rawValue)
                            },
                            onShare = {
                                shareText(qrData.rawValue)
                            }
                        )
                    } ?: run {
                        // Loading or not found state
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "열 수 없는 링크입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "공유하기"))
    }
}

@Composable
fun ResultContent(
    qrResult: QRResult,
    onOpenLink: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(qrResult.type.name, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(qrResult.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(qrResult.rawValue, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (qrResult.type == QRType.URL) {
                Button(onClick = onOpenLink, modifier = Modifier.weight(1f)) {
                    Text("Open Link")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            OutlinedButton(onClick = onCopy, modifier = if (qrResult.type != QRType.URL) Modifier.weight(1f) else Modifier) {
                Text("Copy")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onShare, modifier = if (qrResult.type != QRType.URL) Modifier.weight(1f) else Modifier) {
                Text("Share")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
