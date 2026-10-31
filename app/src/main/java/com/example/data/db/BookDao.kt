package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // Books
    @Query("SELECT * FROM books ORDER BY lastReadTimestamp DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE shelfId = :shelfId ORDER BY lastReadTimestamp DESC")
    fun getBooksByShelf(shelfId: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY lastReadTimestamp DESC")
    fun getFavoriteBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdOnce(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("UPDATE books SET readingProgress = :progress, scrollY = :scrollY, currentPage = :currentPage, lastReadTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReadingProgress(id: String, progress: Float, scrollY: Int, currentPage: Int, timestamp: Long)

    @Query("UPDATE books SET shelfId = :shelfId WHERE id = :id")
    suspend fun updateBookShelf(id: String, shelfId: String?)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateBookFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE books SET pageCount = :pageCount, tableOfContentsJson = :tocJson WHERE id = :id")
    suspend fun updateBookPageCountAndToc(id: String, pageCount: Int, tocJson: String)

    @Query("UPDATE books SET pageCount = :pageCount WHERE id = :id")
    suspend fun updateBookPageCount(id: String, pageCount: Int)


    @Query("""
        UPDATE books 
        SET title = :title, 
            subtitle = :subtitle,
            author = :author, 
            coverGradientStart = :coverGradientStart,
            coverGradientEnd = :coverGradientEnd,
            bgPrimaryHex = :bgPrimaryHex,
            accentGoldHex = :accentGoldHex,
            accentRoseHex = :accentRoseHex
        WHERE id = :id
    """)
    suspend fun updateBookMetadataAndStyle(
        id: String,
        title: String,
        subtitle: String,
        author: String,
        coverGradientStart: Long,
        coverGradientEnd: Long,
        bgPrimaryHex: String,
        accentGoldHex: String,
        accentRoseHex: String
    )

    // Shelves
    @Query("SELECT * FROM shelves ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllShelves(): Flow<List<Shelf>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelf(shelf: Shelf)

    @Update
    suspend fun updateShelf(shelf: Shelf)

    @Delete
    suspend fun deleteShelf(shelf: Shelf)

    @Query("UPDATE books SET shelfId = NULL WHERE shelfId = :shelfId")
    suspend fun clearShelfFromBooks(shelfId: String)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY scrollY ASC")
    fun getBookmarksForBook(bookId: String): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    // One-shot Snapshot Queries for Cloud Backup & Restore
    @Query("SELECT * FROM books ORDER BY lastReadTimestamp DESC")
    suspend fun getAllBooksOnce(): List<Book>

    @Query("SELECT * FROM shelves ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAllShelvesOnce(): List<Shelf>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt ASC")
    suspend fun getAllBookmarksOnce(): List<Bookmark>
}
