package com.elroi.biblereminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class VersePayload(
    val status: String?,
    val reference: String?,
    val text: String?,
    val theme: String?
)

interface BibleApiService {
    @GET("api/verse?mode=daily&lang=ta")
    suspend fun getDailyVerse(): VersePayload
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F9FA)
                ) {
                    NativeVerseScreen()
                }
            }
        }
    }
}

@Composable
fun NativeVerseScreen() {
    var verseText by remember { mutableStateOf("வேத வசனம் ஏற்றப்படுகிறது...") }
    var verseRef by remember { mutableStateOf("") }
    var verseTheme by remember { mutableStateOf("இன்றைய வசனம்") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val api = remember {
        Retrofit.Builder()
            .baseUrl("https://bible-reminder-app.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = withContext(Dispatchers.IO) { api.getDailyVerse() }
                verseText = res.text ?: "வசனம் கிடைக்கவில்லை"
                verseRef = res.reference ?: ""
                verseTheme = res.theme ?: "இன்றைய வசனம்"
            } catch (e: Exception) {
                verseText = "இணைய இணைப்பை சரிபார்க்கவும்."
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = verseTheme,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6750A4)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF6750A4))
                } else {
                    Text(
                        text = "“$verseText”",
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = verseRef,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7D5260)
                    )
                }
            }
        }
    }
}