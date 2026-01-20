package com.example.ui.util

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview


/**
 * Preview annotation for render the composable in light and dark mode
 */
@Preview(
    name = "Light Mode Phone",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=360dp,height=640dp,dpi=480",
    backgroundColor = 0xFFFFFFFFL
)
@Preview(
    name = "Dark Mode Phone",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=360dp,height=640dp,dpi=480",
    backgroundColor = 0xFF000000L
)
annotation class LightDarkPreviews()


