package com.example.notes_app

import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notes_app.ui.theme.Notes_AppTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

data class NoteItem(
    val id: Int,            // (val) "statiska" Variabler
    var title: String,      // (var) Variabla variabler
    var content: String,
    val check: MutableState<Boolean> = mutableStateOf(false)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Notes_AppTheme {
                val navController = rememberNavController()
                val noteList = remember { mutableStateListOf<NoteItem>() }  // "Rådata" med lista av alla NoteItems

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeView(navController,noteList) }        // Säger: Dom här ska finnas i min router   FRÅGA!!!
                    composable("addNote") { AddNoteView(navController, noteList) }
                    composable("editNote/{itemId}") { backStackEntry ->         // Typ som en historik
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()   // Kolla snällt om det finns itemId
                        Log.d("EditNoteView", "itemId: $itemId")
                        val noteItem =noteList.find { it.id == itemId }        // For each item            FRÅGA!!!
                        noteItem?.let {EditNoteView(navController,it)}   // Är denna inte null? Kör denna funktionen
                    }       // Alla dessa är svåra att hitta men dessa finns på Canvas
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)         // Funktionen för att visa Hem skärmen
@Composable
fun HomeView(navController: NavController, noteList: MutableList<NoteItem>) {
    val context = LocalContext.current

    Scaffold(           // Hovra på Scaffold för att se vad som kan användas.
        topBar = {      // En topBar som redan finns i android
            TopAppBar(title = { Text("All notes") })
        },
        floatingActionButton = {        // En knapp som redan finns i android
            FloatingActionButton(onClick = { navController.navigate("addNote") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {  // Laddar in saker steg för steg.
            // Ärver även padding för at tlägga den under topBar:en.
            items(noteList) { item ->       // som en for loop. For each item in noteList.
                ListItem(       // Man kan använda Coumn/Row eller någon annan.
                    // Denna är bäst för dissabilities. Den vet att den är en lista i detta fallet.
                    headlineContent = { Text(item.title) },     // Rubriker, Kan läsas direkt från listan
                    supportingContent = { Text(item.content) }, // Under rubrik, Kan läsas direkt från listan
                    trailingContent = {     // Längst till vänster
                        Row {           // Skapar en rad med (i vårt fall) IconButtons
                            IconButton(
                                onClick = { navController.navigate("editNote/${item.id}") } // Vad knappen ska göra
                            ) {                                                             // I vårt fall: navigera till editTodo
                                // för item nummer item.id
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Note"
                                )   // Hur knappen ska se ut
                            }
                            IconButton(
                                onClick = {
                                    noteList.remove(item)
                                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Note")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteView(navController: NavController, noteList: MutableList<NoteItem>) {
    var title by remember { mutableStateOf("") }        // Skapa nya TOMMA mutablestates
    var subtitle by remember { mutableStateOf("") }
    val isTitleError = title.length < 3 || title.length > 50
    val isSubtitleError = subtitle.length > 120
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Note") },
                navigationIcon = {      // en navigations knapp för att gå tillbaka
                    IconButton(
                        onClick = {
                            noteList.add(
                                NoteItem(
                                    id = noteList.size,
                                    title = title,
                                    content = subtitle
                                )
                            )
                            navController.popBackStack()        // Ta bort det sista objektet (routern)
                        }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )  // En pil tillbaka
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (!isTitleError && !isSubtitleError) {
                    noteList.add(NoteItem(id = noteList.size, title = title, content = subtitle))
                    navController.popBackStack()
                    Toast.makeText(context, "Note added", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Done")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TextField(      // Första värdet i columnen. Hovra för att se hur man kan pimpa knappen
                value = title,
                onValueChange = { title = it },      // Sätt det senaste värdet i "title"
                label = { Text(
                    if (isTitleError) "Title must be between 3 and 50 characters"
                    else "Title"
                ) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(      // Andra värdet i columnen
                // Ändra här för att kunna skriva flera rader!!!
                value = subtitle,
                onValueChange = { subtitle = it },   // Sätt det senaste värdet i "title"
                label = { Text(
                    if (isSubtitleError) "Note must be less than 120 characters"
                    else "Note"
                ) }        // Vad som ska stå innan man skriver något
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteView(navController: NavController, noteItem: NoteItem) {
    var title by remember { mutableStateOf(noteItem.title) }        // Hämtar mutableState
    var subtitle by remember { mutableStateOf(noteItem.content) }   // Hämtar mutableState
    val isTitleError = title.length < 3 || title.length > 50
    val isSubtitleError = subtitle.length > 120
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (!isTitleError && !isSubtitleError) {
                    // Update the original noteItem's state
                    noteItem.title = title
                    noteItem.content = subtitle
                    navController.popBackStack()
                    Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Done")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(
                    if (isTitleError) "Title must be between 3 and 50 characters"
                    else "Title"
                ) },
                isError = isTitleError
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text(
                    if (isSubtitleError) "Note must be less than 120 characters"
                    else "Note"
                ) },
                isError = isSubtitleError
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
