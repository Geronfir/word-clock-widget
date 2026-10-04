package com.geronfir.wordclock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geronfir.wordclock.R
import com.geronfir.wordclock.ui.theme.WordClockTheme

/**
 * Display settings screen — placeholder.
 *
 * Will host language, format, style, colour and font-size controls for the
 * widget. Until then it renders a "coming soon" panel so the navigation
 * skeleton is complete and testable.
 */
@Composable
fun DisplayScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.nav_display),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Coming soon — widget display settings live here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DisplayScreenPreview() {
    WordClockTheme {
        DisplayScreen()
    }
}
