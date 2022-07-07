package com.example.storagedemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.storagedemo.ui.theme.StorageDemoTheme

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
                                    intent.putExtra("file_path", "")
                                    ctx.startActivity(intent)
                                }) {
                                    Icon(painter = painterResource(id = R.drawable.ic_baseline_view_list_24), contentDescription = "List Files")
                                }
                            }
                        )
                    }
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StorageDemoTheme {
        MainScreen()
    }
}