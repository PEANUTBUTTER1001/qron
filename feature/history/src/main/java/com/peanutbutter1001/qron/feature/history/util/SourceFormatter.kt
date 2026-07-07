package com.peanutbutter1001.qron.feature.history.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.peanutbutter1001.qron.domain.model.ScanSource
import com.peanutbutter1001.qron.feature.history.R

@Composable
fun scanSourceToString(source: ScanSource): String {
    return when (source) {
        ScanSource.SCREEN -> stringResource(R.string.scan_source_screen)
        ScanSource.EXTERNAL -> stringResource(R.string.scan_source_external)
    }
}
