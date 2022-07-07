package com.example.storagedemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.storagedemo.ui.theme.StorageDemoTheme
import java.io.File

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
                            actions = {
                                IconButton(onClick = {
                                    val intent = Intent(ctx, ListActivity::class.java)
                                    intent.putExtra("file_path", applicationContext.filesDir.path)
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
                ) {
                    MainScreen(it)
                }
            }
        }
    }
}

fun Context.writeToFile(filename: String, contents: String) {
    val file =
        File(applicationContext.filesDir + "Folder", filename)
    file.writeText(contents)
}

fun Context.readFromFile(filename: String): String {
    val file =
        File(applicationContext.filesDir + "Folder", filename)
    return file.readText()
}

private operator fun File.plus(other: String): File {

    return File(this, other).apply {
        if (!exists()) mkdir()
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues) {
    var contents by remember {
        mutableStateOf(
            """
            Hello
            Contents of the file
        """.trimIndent()
        )
    }
    var filename by remember {
        mutableStateOf("hello.txt")
    }
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(paddingValues).fillMaxSize(),
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
            try {
                context.writeToFile(filename, contents)
                Toast.makeText(context, "Written to file $filename", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Write to file")
        }
        Button(onClick = {
            contents = ""
            try {
                contents = context.readFromFile(filename)
                Toast.makeText(context, "Read from file $filename", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Read from file")
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