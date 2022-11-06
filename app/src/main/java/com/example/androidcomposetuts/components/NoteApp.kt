package com.example.androidcomposetuts.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NotesAppModule {
    @Singleton
    @Provides
    fun provideNotesDao(notesDatabase: NotesDatabase): NotesDatabaseDao = notesDatabase.noteDao()

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): NotesDatabase
            = Room
        .databaseBuilder(
            context,
            NotesDatabase::class.java,
            "notes_db",
        )
        .fallbackToDestructiveMigration()
        .build()
}

@Entity(tableName = "vighnesh_notes")
data class Note internal constructor(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "note_title")
    val title: String,

    @ColumnInfo()
    val description: String,

    @ColumnInfo()
    val entryDate: Date = Date.from(Instant.now()),
)

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, UUIDConverter::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NotesDatabaseDao
}

@Dao
interface NotesDatabaseDao {
    @Query("SELECT * FROM vighnesh_notes")
    fun getNotes(): Flow<List<Note>>

    @Query("SELECT * FROM vighnesh_notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(note: Note)

    @Query("DELETE FROM vighnesh_notes")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(note: Note)
}

class NotesRepository @Inject constructor(
    private val notesDatabaseDao: NotesDatabaseDao,
) {
    suspend fun addNote(note: Note) = notesDatabaseDao.insert(note)
    suspend fun updateNote(note: Note) = notesDatabaseDao.update(note)
    suspend fun deleteNote(note: Note) = notesDatabaseDao.delete(note)
    suspend fun deleteAllNotes() = notesDatabaseDao.deleteAll()
    suspend fun getNotes(): Flow<List<Note>> = notesDatabaseDao
        .getNotes()
        .flowOn(Dispatchers.IO)
        .conflate()
}

@Preview(showBackground = true)
@Composable
fun NoteApp() {
    val notesViewModel = viewModel<NoteViewModel>()

    NoteScreen(
        notes = notesViewModel.notes.collectAsState().value,
        onAddNote = { note -> notesViewModel.addNote(note) },
        onRemoveNote = { note -> notesViewModel.deleteNote(note) },
    )
}

@Composable
private fun NoteScreen(
    notes: List<Note>,
    onAddNote: (Note) -> Unit,
    onRemoveNote: (Note) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(6.dp)) {
        TopAppBar(
            modifier = Modifier,
            title = {
                Text(text = "JetNote")
            },
            actions = {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                )
            },
            backgroundColor = Color(0xFFDADFE3)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NoteAppTextField(
                modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
                value = title,
                label = "Title",
                onValueChange = {
                    if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                        title = it
                    }
                },
            )

            NoteAppTextField(
                modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
                value = description,
                label = "Add a note",
                onValueChange = {
                    if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                        description = it
                    }
                },
            )

            NoteButton(
                text = "Save",
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        val newNote = Note(
                            title = title,
                            description = description,
                        )
                        onAddNote(newNote)

                        title = ""
                        description = ""

                        Toast
                            .makeText(
                                context,
                                "Note Added",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    }
                },
            )
        }

        Divider(modifier = Modifier.padding(10.dp))

        LazyColumn {
            items(notes) { note ->
                NoteRow(
                    note = note,
                    onNoteClicked = { onRemoveNote(note) }
                )
            }
        }
    }
}

@Composable
private fun NoteRow(
    modifier: Modifier = Modifier,
    note: Note,
    onNoteClicked: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(topEnd = 33.dp, bottomStart = 33.dp))
            .fillMaxWidth(),
        color = Color(0xFFDFE6EB),
        elevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .clickable { onNoteClicked() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.subtitle2,
            )
            Text(
                text = note.description,
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                text = formatDate(note.entryDate.time),
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .getNotes()
                .distinctUntilChanged()
                .collect { repositoryNotes ->
                    if (repositoryNotes.isNotEmpty()) {
                        _notes.value = repositoryNotes
                    }
                }
        }
    }

    fun addNote(note: Note) = viewModelScope.launch { repository.addNote(note) }
    fun updateNote(note: Note) = viewModelScope.launch { repository.updateNote(note) }
    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note) }
    fun deleteAllNotes() = viewModelScope.launch { repository.deleteAllNotes() }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NoteAppTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
    onImeAction: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
        ),
        maxLines = maxLines,
        label = { Text(text = label) },
        keyboardActions = KeyboardActions(
            onDone = {
                onImeAction()
                keyboardController?.hide()
            },
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
        ),
    )
}

@Composable
private fun NoteButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

private class NotesDataSource {
    fun loadNotes(): List<Note> {
        return listOf(
            Note(
                title = "A good day",
                description = "We went on a vacation by the lake",
            ),
            Note(
                title = "Android Compose",
                description = "Working on Android Compose course today",
            ),
            Note(
                title = "Keep at it...",
                description = "Sometimes things just happen",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family today",
            ),
            Note(
                title = "A movie day",
                description = "Watching a movie with family",
            ),
        )
    }
}

private class DateConverter {
    @TypeConverter
    fun timeStampFromDate(date: Date): Long {
        return date.time

    }
    @TypeConverter
    fun dateFromTimestamp(timestamp: Long): Date? {
        return Date(timestamp)
    }
}

private class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String? {
        return uuid.toString()
    }

    @TypeConverter
    fun uuidFromString(string: String?): UUID? {
        return UUID.fromString(string)
    }
}

private fun formatDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat(
        "EEE, d MMM hh:mm aaa",
        Locale.getDefault(),
    )
    return format.format(date)
}
