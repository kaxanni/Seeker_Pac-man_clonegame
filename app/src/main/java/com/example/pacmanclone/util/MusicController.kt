package com.example.pacmanclone.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object MusicController {
    var volume by mutableStateOf(0.5f)
    var isMuted by mutableStateOf(false)
}
