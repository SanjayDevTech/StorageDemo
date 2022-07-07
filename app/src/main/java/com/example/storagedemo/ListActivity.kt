package com.example.storagedemo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.storagedemo.ui.theme.StorageDemoTheme
import java.io.File

class ListActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filePath = intent.getStringExtra("file_path")?.dropLastWhile { it == '/' } ?: run {
            finish()
            return
        }
        setContent {
            StorageDemoTheme {
                var currentFile by remember {
                    mutableStateOf(File(filePath))
                }
                val files by remember {
                    derivedStateOf { currentFile.listFiles() ?: emptyArray() }
                }

                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(text = "List Files")
                            }, navigationIcon = {
                                IconButton(onClick = {
                                    if (currentFile.path == filePath) {
                                        onBackPressed()
                                    } else {
                                        val parentFile = currentFile.parentFile!!
                                        currentFile = parentFile
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                                        contentDescription = "Back"
                                    )
                                }
                            })
                    }
                ) { paddingValues ->
                    LazyColumn(modifier = Modifier.padding(paddingValues)) {
                        item {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(4.dp)) {
                                Text(text = currentFile.path)
                            }
                        }
                        items(files, key = {it.path}) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (file.isDirectory) currentFile = file
                                    }
                                    .padding(12.dp),
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