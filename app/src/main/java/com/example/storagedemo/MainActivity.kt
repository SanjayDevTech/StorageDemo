package com.example.storagedemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.storagedemo.ui.theme.StorageDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
                            actions = {
                                IconButton(onClick = {
                                    val intent = Intent(ctx, ListActivity::class.java)
                                    intent.putExtra(
                                        "file_path",
                                        Environment.getExternalStorageDirectory().path
                                    )
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
                    MainScreen(paddingValues)
                }
            }
        }
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    var contents by remember {
        mutableStateOf(
            """
            Hello
            Contents of the file
        """.trimIndent()
        )
    }
    val scope = rememberCoroutineScope()
    fun checkIfGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    var isGranted: Boolean by remember {
        mutableStateOf(checkIfGranted())
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            isGranted = checkIfGranted()
        }
    val manageIntentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isGranted = checkIfGranted()
        }


    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manageIntentLauncher.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.fromParts("package", context.packageName, null)
            })
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        }
    }

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
            if (!isGranted) return@Button
            scope.launch {
                try {
                    writeToFile(Environment.getExternalStorageDirectory(), filename, contents)
                    Toast.makeText(context, "Written to file $filename", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(text = "Write to file")
        }
        Button(onClick = {
            if (!isGranted) return@Button
            scope.launch {
                try {
                    contents = readFromFile(Environment.getExternalStorageDirectory(), filename)
                    Toast.makeText(context, "Read from file $filename", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(text = "Read from file")
        }
        Button(onClick = {
            requestPermission()
        }) {
            Text(text = if (isGranted) "Permission granted" else "Permission not granted")
        }
    }
}

suspend fun writeToFile(file: File, filename: String, contents: String) {
    withContext(Dispatchers.IO) {
        File(file, filename).writeText(contents)
    }
}

suspend fun readFromFile(file: File, filename: String): String {
    return withContext(Dispatchers.IO) {
        File(file, filename).readText()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StorageDemoTheme {
        MainScreen()
    }
}