package com.example.storagedemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.storagedemo.ui.theme.StorageDemoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

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

val Context.isWritePermissionEnabled: Boolean
    get() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

fun Context.writeToFile(filename: String, contents: String) {
    val file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)
    println(file.path)
    file.writeText(contents)
}

fun Context.readFromFile(filename: String): String {
    val file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)
    return file.readText()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
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
//    val externalPermissionState =
//        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            try {
//                if (externalPermissionState.hasPermission) {
                    context.writeToFile(filename, contents)
                    Toast.makeText(context, "Written to file $filename", Toast.LENGTH_SHORT).show()
//                } else {
//                    externalPermissionState.launchPermissionRequest()
//                }
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