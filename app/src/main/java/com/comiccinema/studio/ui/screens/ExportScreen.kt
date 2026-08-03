package com.comiccinema.studio.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(navController: NavController, projectId: String) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf("جاهز للإنتاج...") }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("إنتاج الفيديو") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = exportProgress,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isExporting) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isExporting = true
                        exportProgress = "جاري دمج الكوميك مع الصوت..."
                        
                        // مسار افتراضي لملف الإخراج (فيديو)
                        val outputFile = File(context.cacheDir, "comic_video_${System.currentTimeMillis()}.mp4")
                        
                        // أمر FFmpeg وهمي للتبسيط (بيعمل فيديو مدته 5 ثواني بخلفية سوداء)
                        // في التطبيق الحقيقي: هندمج الصورة المحددة مع ملف الـ Audio اللي تم إنشاؤه
                        val command = "-f lavfi -i color=c=black:s=1280x720:d=5 -c:v mpeg4 ${outputFile.absolutePath}"
                        
                        FFmpegKit.executeAsync(command) { session ->
                            val returnCode = session.returnCode
                            if (ReturnCode.isSuccess(returnCode)) {
                                val encodedPath = Uri.encode(outputFile.absolutePath)
                                navController.navigate("share/$encodedPath") {
                                    popUpTo("capture") // تنظيف الـ Backstack
                                }
                            } else {
                                isExporting = false
                                exportProgress = "حدث خطأ أثناء الإنتاج!"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("ابدأ صناعة الفيديو", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
