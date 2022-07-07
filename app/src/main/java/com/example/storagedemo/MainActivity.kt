package com.example.storagedemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
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
                var fileUri: Uri? = null
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(text = "Storage Demo")
                            },
                            actions = {
                                IconButton(onClick = {
                                    val intent = Intent(ctx, ListActivity::class.java)
                                    intent.putExtra("file_uri", fileUri)
                                    ctx.startActivity(intent)
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_view_list_24),
                                        contentDescription = "List Files"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    MainScreen(paddingValues) {
                        fileUri = it
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues = PaddingValues(), onResult: (Uri?) -> Unit) {
    var result by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        it?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        result = it
        onResult(it)
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
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
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
