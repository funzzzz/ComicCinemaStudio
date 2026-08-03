package com.comiccinema.studio.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(navController: NavController, imageUri: String) {
    val context = LocalContext.current
    val decodedUri = Uri.parse(Uri.decode(imageUri))
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detectedText by remember { mutableStateOf<List<String>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var audioFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ar", "SA")
                tts?.setSpeechRate(0.85f)
            }
        }
    }
    
    LaunchedEffect(decodedUri) {
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(decodedUri, "r")
            parcelFileDescriptor?.let {
                bitmap = BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                it.close()
            }
            
            bitmap?.let { bmp ->
                isProcessing = true
                val image = InputImage.fromBitmap(bmp, 0)
                val recognizer = TextRecognition.getClient(ArabicTextRecognizerOptions.Builder().build())
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        detectedText = visionText.textBlocks.map { it.text }
                        isProcessing = false
                    }
                    .addOnFailureListener {
                        isProcessing = false
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استوديو المونتاج") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate("export/${System.currentTimeMillis()}")
                    }) {
                        Text("التالي")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            bitmap?.let { bmp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "الكوميكس",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("الحوارات المكتشفة:", style = MaterialTheme.typography.titleMedium)
            
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(detectedText) { text ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(
                                    onClick = {
                                        val utteranceId = UUID.randomUUID().toString()
                                        val audioFile = File(context.cacheDir, "tts_$utteranceId.wav")
                                        
                                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                            override fun onStart(id: String?) {}
                                            override fun onDone(id: String?) {
                                                audioFiles = audioFiles + audioFile
                                            }
                                            override fun onError(id: String?) {}
                                        })
                                        
                                        tts?.synthesizeToFile(text, null, audioFile, utteranceId)
                                    }
                                ) {
                                    Icon(Icons.Default.VolumeUp, "استمع")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
