package com.example.storagedemo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import com.example.storagedemo.ui.theme.StorageDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StorageDemoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var result by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        result = it
    }
    var contents by remember {
        mutableStateOf(
            """
            Hello
            Contents of the file
        """.trimIndent()
        )
    }
    val scope = rememberCoroutineScope()
    var filename by remember {
        mutableStateOf("hello.txt")
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(
            value = contents,
            onValueChange = { contents = it },
            label = { Text(text = "Contents") },
            placeholder = {
                Text(text = "Type the contents")
            },
        )
        TextField(
            value = filename,
            onValueChange = { filename = it },
            singleLine = true,
            label = { Text(text = "Filename") },
            placeholder = {
                Text(text = "Type the filename")
            },
        )
        Button(onClick = {
            scope.launch {
                try {
                    result?.let {
                        context.writeToFile(it, filename, contents)
                        Toast
                            .makeText(context, "Written to file $filename", Toast.LENGTH_SHORT)
                            .show()
                    } ?: run {
                        Toast
                            .makeText(context, "Folder is not yet selected", Toast.LENGTH_SHORT)
                            .show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(text = "Write to file")
        }
        Button(onClick = {
            scope.launch {
                try {
                    result?.let {
                        contents = context.readFromFile(it, filename)
                        Toast.makeText(context, "Read from file $filename", Toast.LENGTH_SHORT)
                            .show()
                    } ?: run {
                        Toast.makeText(context, "Folder is not yet selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(text = "Read from file")
        }
        Button(onClick = {
            launcher.launch(result)
        }) {
            Text(text = if (result == null) "Select Folder" else "Folder: ${result!!.path}")
        }
    }
}

suspend fun Context.writeToFile(uri: Uri, filename: String, contents: String) {
    val documentTree = DocumentFile.fromTreeUri(this, uri) ?: run {
        throw Exception("Cannot get tree")
    }
    val documentFile =
        documentTree.findFile(filename) ?: documentTree.createFile("plain/text", filename) ?: run {
            throw Exception("Cannot create file")
        }
    withContext(Dispatchers.IO) {
        contentResolver.openOutputStream(documentFile.uri, "wt")?.use { outputStream ->
            outputStream.bufferedWriter().use {
                it.write(contents)
            }
        }
    }
}

suspend fun Context.readFromFile(uri: Uri, filename: String): String {
    val documentTree = DocumentFile.fromTreeUri(this, uri) ?: run {
        throw Exception("Cannot get tree")
    }
    val documentFile =
        documentTree.findFile(filename) ?: documentTree.createFile("plain/text", filename) ?: run {
            throw Exception("Cannot create file")
        }
    return withContext(Dispatchers.IO) {
        contentResolver.openInputStream(documentFile.uri)?.use { inputStream ->
            inputStream.bufferedReader().use {
                it.readLines().joinToString("\n")
            }
        } ?: ""
    }
}
