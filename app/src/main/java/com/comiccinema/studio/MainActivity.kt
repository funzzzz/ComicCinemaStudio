package com.comiccinema.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.comiccinema.studio.ui.screens.CaptureScreen
import com.comiccinema.studio.ui.screens.EditorScreen
import com.comiccinema.studio.ui.screens.ExportScreen
import com.comiccinema.studio.ui.screens.ShareScreen
import com.comiccinema.studio.ui.theme.ComicCinemaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComicCinemaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "capture") {
                        composable("capture") { CaptureScreen(navController) }
                        composable("editor/{imageUri}") { backStackEntry ->
                            EditorScreen(
                                navController,
                                backStackEntry.arguments?.getString("imageUri") ?: ""
                            )
                        }
                        composable("export/{projectId}") { backStackEntry ->
                            ExportScreen(
                                navController,
                                backStackEntry.arguments?.getString("projectId") ?: ""
                            )
                        }
                        composable("share/{videoPath}") { backStackEntry ->
                            ShareScreen(
                                navController,
                                backStackEntry.arguments?.getString("videoPath") ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}
