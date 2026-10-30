package com.example.data.importer

import android.content.Context
import com.example.data.model.Book

object SampleBooksProvider {

    const val DEFAULT_MASTERPIECE_ID = "khayma_fi_alqalb_masterpiece"
    const val LEGACY_ISLAMIC_ID = "islamic_sakina_masterpiece"

    fun isDefaultOrSampleBookId(idOrFolder: String): Boolean {
        return idOrFolder.startsWith("sample_") ||
            idOrFolder.startsWith("islamic_sakina") ||
            idOrFolder.startsWith("khayma_fi_alqalb")
    }

    @Suppress("UNUSED_PARAMETER")
    fun generateSampleBooks(context: Context): List<Book> {
        return emptyList()
    }
}
