package com.ps4_mwpm

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import android.Manifest

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 1234
    private val viewModel: VoiceNoteViewModel by viewModels {
        VoiceNoteViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.voiceNotes.postValue(viewModel.getVoiceNotes())

        setContent {
            VoiceNoteApp(viewModel)
        }
        checkAndRequestPermissions()
    }
    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        val hasRecordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (hasRecordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        val hasReadMediaAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
        if (hasReadMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        val hasWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val hasModifyAudioSettingsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)
        if (hasModifyAudioSettingsPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

@Composable
fun VoiceNoteApp(viewModel: VoiceNoteViewModel) {
    val isRecording = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                if (isRecording.value) {
                    viewModel.stopRecording()
                    val newFilePath = viewModel.currentRecordingPath ?: return@Button
                    val newVoiceNote = VoiceNote(
                        id = System.currentTimeMillis(),
                        title = "Notatka_${System.currentTimeMillis()}",
                        filePath = newFilePath,
                        dateCreated = System.currentTimeMillis()
                    )
                    viewModel.saveVoiceNote(newVoiceNote)
                    viewModel.voiceNotes.value = viewModel.getVoiceNotes()
                } else {
                    val newFileName = "Notatka_${System.currentTimeMillis()}.3gp"
                    val storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    val newFilePath = File(storageDirectory, newFileName).absolutePath
                    viewModel.startRecording(newFilePath)
                }
                isRecording.value = !isRecording.value
            }) {
                Text(if (isRecording.value) "Stop" else "Nagraj")
            }
        }
        VoiceNotesList(viewModel)
    }
}
