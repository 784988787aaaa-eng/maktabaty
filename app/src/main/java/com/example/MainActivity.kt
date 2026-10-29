package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LibraryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavRoot(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val dataUri = intent.data

        if (action == Intent.ACTION_VIEW && dataUri != null) {
            viewModel.importBook(dataUri) { imported ->
                if (imported != null) {
                    viewModel.openBook(imported)
                }
            }
        } else if (action == Intent.ACTION_SEND) {
            val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (streamUri != null) {
                viewModel.importBook(streamUri) { imported ->
                    if (imported != null) {
                        viewModel.openBook(imported)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavRoot(viewModel: LibraryViewModel) {
    val activeBook by viewModel.activeBook.collectAsStateWithLifecycle()
    val activeBookmarks by viewModel.activeBookmarks.collectAsStateWithLifecycle()

    val currentBook = activeBook
    if (currentBook != null) {
        ReaderScreen(
            book = currentBook,
            bookmarks = activeBookmarks,
            onBack = { viewModel.closeReader() },
            onSaveProgress = { progress, scrollY, currentPage ->
                viewModel.saveProgress(currentBook.id, progress, scrollY, currentPage)
            },
            onAddBookmark = { title, scrollY, progress, excerpt ->
                viewModel.addBookmark(title, scrollY, progress, excerpt)
            },
            onDeleteBookmark = { bookmark ->
                viewModel.deleteBookmark(bookmark)
            }
        )
    } else {
        LibraryScreen(
            viewModel = viewModel,
            onOpenBook = { book ->
                viewModel.openBook(book)
            }
        )
    }
}
