package com.example.storagedemo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.documentfile.provider.DocumentFile
import com.example.storagedemo.ui.theme.StorageDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            StorageDemoTheme {
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(text = "Storage Demo")
                            },
                        )
                    }
                ) { paddingValues ->
                    MainScreen(paddingValues)
                }
            }
        }
    }
}

suspend fun Context.writeToFile(uri: Uri, contents: String) {
    withContext(Dispatchers.IO) {
        contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
            outputStream.bufferedWriter().use {
                it.write(contents)
            }
        }
    }
}

suspend fun Context.readFromFile(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use {
                it.readLines().joinToString("\n")
            }
        } ?: ""
    }
}

suspend fun Context.deleteFile(uri: Uri): Boolean {
    val documentFile = DocumentFile.fromSingleUri(this, uri)
    return withContext(Dispatchers.IO) {
        return@withContext documentFile?.delete() == true
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues = PaddingValues()) {

    val ctx = LocalContext.current
    var selected: Uri? by remember {
        mutableStateOf(null)
    }

    var contents by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    val launchCreateDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        uri?.let {
            selected = it
        }
    }

    val launchOpenDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selected = it
        }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(value = contents, onValueChange = {contents = it})
        Button(onClick = { launchCreateDocument.launch("hello.txt") }) {
            Text(text = "Create new file")
        }
        Button(onClick = { launchOpenDocument.launch(arrayOf("text/plain")) }) {
            Text(text = "Choose file")
        }
        Button(
            onClick = {
                   scope.launch {
                       try {
                           contents = ctx.readFromFile(selected!!)
                           Toast.makeText(ctx, "Read from file", Toast.LENGTH_SHORT).show()
                       } catch (e: Exception) {
                           Toast.makeText(ctx, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                           e.printStackTrace()
                       }
                   }
            },
            enabled = selected != null,
        ) {
            Text(text = "Read file")
        }
        Button(
            onClick = {
                scope.launch {
                    try {
                        ctx.writeToFile(selected!!, contents)
                        Toast.makeText(ctx, "Written to file", Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            },
            enabled = selected != null,
        ) {
            Text(text = "Write file")
        }
        Button(
            onClick = {
                scope.launch {
                    try {
                        val isDeleted = ctx.deleteFile(selected!!)
                        if (isDeleted) {
                            selected = null
                        } else {

                            Toast.makeText(ctx, "Not deleted", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            },
            enabled = selected != null,
        ) {
            Text(text = "Delete file")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StorageDemoTheme {
        MainScreen()
    }
}