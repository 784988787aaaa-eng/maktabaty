package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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

        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val userTypographyPrefs by viewModel.userTypographyPreferences.collectAsStateWithLifecycle()
            MyApplicationTheme(
                darkTheme = isDarkTheme,
                typographyProfile = if (userTypographyPrefs.useCustomTypography) userTypographyPrefs.profile else null
            ) {
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
            viewModel.openExternalUriImmediately(dataUri)
        } else if (action == Intent.ACTION_SEND) {
            val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (streamUri != null) {
                viewModel.openExternalUriImmediately(streamUri)
            }
        }
    }
}

@Composable
fun AppNavRoot(viewModel: LibraryViewModel) {
    val activeBook by viewModel.activeBook.collectAsStateWithLifecycle()
    val activeBookmarks by viewModel.activeBookmarks.collectAsStateWithLifecycle()
    val isOpeningExternalFile by viewModel.isOpeningExternalFile.collectAsStateWithLifecycle()
    val userTypographyPrefs by viewModel.userTypographyPreferences.collectAsStateWithLifecycle()

    val currentBook = activeBook
    if (currentBook != null) {
        ReaderScreen(
            book = currentBook,
            bookmarks = activeBookmarks,
            userTypographyPreferences = userTypographyPrefs,
            onUpdateTypographyPreferences = { viewModel.updateUserTypographyPreferences(it) },
            onBack = { viewModel.closeReader() },
            onSaveProgress = { progress, scrollY, currentPage, actualTotalPages ->
                viewModel.saveProgress(currentBook.id, progress, scrollY, currentPage, actualTotalPages)
            },
            onAddBookmark = { title, scrollY, progress, excerpt ->
                viewModel.addBookmark(title, scrollY, progress, excerpt)
            },
            onDeleteBookmark = { bookmark ->
                viewModel.deleteBookmark(bookmark)
            }
        )
    } else if (isOpeningExternalFile) {
        // Clean full-screen reader backdrop during the ~10ms stream copy so LibraryScreen never flashes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
