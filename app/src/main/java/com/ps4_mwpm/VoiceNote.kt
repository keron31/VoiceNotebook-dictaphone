package com.ps4_mwpm

data class VoiceNote(
    val id: Long,
    val title: String,
    val filePath: String,
    val dateCreated: Long,
    var isPlaying: Boolean = false
)
