package com.curseforge.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.curseforge.mobile.ui.AppRoot
import com.curseforge.mobile.ui.theme.CurseForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurseForgeTheme {
                Surface {
                    AppRoot(applicationContext)
                }
            }
        }
    }
}
