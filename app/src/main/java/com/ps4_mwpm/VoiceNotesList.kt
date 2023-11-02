package com.ps4_mwpm

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.util.Date

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VoiceNotesList(viewModel: VoiceNoteViewModel) {
    val notes by viewModel.voiceNotes.observeAsState(emptyList())

    if (notes.isEmpty()) {
        Text("Lista jest pusta", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    } else {
        LazyColumn {
            items(notes.size) { index ->
                val note = notes[index]
                ListItem(
                    text = { Text(note.title) },
                    secondaryText = { Text(Date(note.dateCreated).toString()) },
                    trailing = {
                        Row {
                            IconButton(onClick = {
                                if (note.isPlaying) {
                                    viewModel.stopPlaying()
                                } else {
                                    viewModel.playRecording(note.filePath)
                                }
                            }) {
                                Icon(
                                    if (note.isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = if (note.isPlaying) "Stop" else "Odtwarzaj"
                                )
                            }
                            IconButton(onClick = { viewModel.deleteVoiceNote(note) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń")
                            }
                        }
                    }
                )
            }
        }
    }
}


