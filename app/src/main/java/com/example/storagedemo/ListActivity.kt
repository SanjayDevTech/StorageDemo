package com.example.storagedemo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.storagedemo.ui.theme.StorageDemoTheme
import java.io.File

class ListActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filePath = intent.getStringExtra("file_path")?.dropLastWhile { it == '/' } ?: run {
            finish()
            return
        }
        setContent {
            StorageDemoTheme {
                var currentFilePair: Pair<String, File> by remember {
                    mutableStateOf(filePath to File(filePath))
                }
                val files by remember {
                    derivedStateOf { currentFilePair.second.listFiles() ?: emptyArray() }
                }

                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(text = currentFilePair.first)
                            }, navigationIcon = {
                                IconButton(onClick = {
                                    if (currentFilePair.first == filePath) {
                                        onBackPressed()
                                    } else {
                                        val parentFile = currentFilePair.second.parentFile!!
                                        currentFilePair = parentFile.absolutePath to parentFile
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                                        contentDescription = "Back"
                                    )
                                }
                            })
                    }
                ) {
                    LazyColumn {
                        items(files) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = if (file.isFile) R.drawable.ic_file else R.drawable.ic_folder),
                                    contentDescription = if (file.isFile) "File" else "Folder"
                                )
                                Text(text = file.name)
                            }
                        }
                    }
                }

            }
        }
    }
}