package com.ps4_mwpm

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class VoiceNoteViewModel(private val application: Application) : ViewModel() {
    val voiceNotes: MutableLiveData<List<VoiceNote>> = MutableLiveData()
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val context get() = application.applicationContext
    var currentRecordingPath: String? = null
    val isPlaying = MutableLiveData<Boolean>(false)
    private var currentPlayingPath: String? = null
    private val audioManager: AudioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun startRecording(filePath: String) {
        currentRecordingPath = filePath
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun playRecording(filePath: String) {
        if (mediaPlayer?.isPlaying == true) {
            stopPlaying()
        }
        setMaxVolume()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            this.setOnCompletionListener {
                this@VoiceNoteViewModel.isPlaying.value = false
                updateNoteState(filePath, false)
            }
        }
        currentPlayingPath = filePath
        updateNoteState(filePath, true)
    }

    fun setMaxVolume() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
    }

    fun updateNoteState(filePath: String, isPlaying: Boolean) {
        val notes = voiceNotes.value?.map { note ->
            if (note.filePath == filePath) {
                note.copy(isPlaying = isPlaying)
            } else {
                note.copy(isPlaying = false)
            }
        }
        voiceNotes.value = notes
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (currentPlayingPath != null) {
            updateNoteState(currentPlayingPath!!, false)
            currentPlayingPath = null
        }
    }

    fun saveVoiceNote(note: VoiceNote) {
    }

    fun getVoiceNotes(): List<VoiceNote> {
        val voiceNotes = mutableListOf<VoiceNote>()
        val storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val files = storageDirectory?.listFiles { _, name -> name.endsWith(".3gp") }

        files?.forEach { file ->
            val title = file.nameWithoutExtension
            val dateCreated = file.lastModified()
            val filePath = file.absolutePath
            voiceNotes.add(VoiceNote(System.currentTimeMillis(), title, filePath, dateCreated))
        }
        return voiceNotes
    }

    fun refreshVoiceNotes() {
        voiceNotes.value = getVoiceNotes()
    }

    fun deleteVoiceNote(note: VoiceNote) {
        val file = File(note.filePath)
        file.delete()
        refreshVoiceNotes()
    }
}