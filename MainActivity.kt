package com.takue.kai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechReady = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        setContent {
            KaiHome(
                onListen = {
                    val granted = ContextCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) startListening()
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onSpeak = { speak(it) },
                onOpenApp = { openApp(it) }
            )
        }
    }

    private fun startListening() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            runOnUiThread { /* UI reports unavailable through status in the next version */ }
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "I'm listening…")
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (words.isNotBlank()) {
                    val answer = makeReply(words)
                    runOnUiThread {
                        speak(answer)
                        // The Compose screen receives spoken results in a later iteration.
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    private fun makeReply(text: String): String {
        val q = text.lowercase(Locale.getDefault())
        return when {
            "hello" in q || "hi" in q -> "Hey! Kai is here. What can I do for you?"
            "your name" in q -> "I'm Kai, your personal assistant."
            "time" in q -> "The time is " + java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(java.util.Date())
            "date" in q -> "Today is " + java.text.SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(java.util.Date())
            else -> "I heard you say: $text. I'm still learning. More AI features are coming."
        }
    }

    private fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "KAI_REPLY")
    }

    private fun openApp(name: String) {
        val intent = packageManager.getLaunchIntentForPackage(name) ?: return
        startActivity(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun KaiHome(
    onListen: () -> Unit,
    onSpeak: (String) -> Unit,
    onOpenApp: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("I'm Kai. Tap the microphone and talk to me.") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            background = Color(0xFF080808),
            surface = Color(0xFF171717),
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF080808)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(30.dp))
                Text("KAI", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("YOUR PERSONAL AI", fontSize = 11.sp, letterSpacing = 4.sp, color = Color.Gray)
                Spacer(Modifier.height(54.dp))
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0xFF202020), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(145.dp)
                            .background(Color(0xFF303030), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("K", fontSize = 76.sp, fontWeight = FontWeight.Light, color = Color.White)
                    }
                }
                Spacer(Modifier.height(30.dp))
                Text("Ready when you are.", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text(reply, color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = onListen,
                    modifier = Modifier.size(78.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("🎙", fontSize = 30.sp)
                }
                Spacer(Modifier.height(10.dp))
                Text("TAP TO TALK", color = Color.Gray, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type a message…", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            reply = "You said: $message"
                            onSpeak(reply)
                            message = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("SEND")
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "KAI AI • EARLY BUILD",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
