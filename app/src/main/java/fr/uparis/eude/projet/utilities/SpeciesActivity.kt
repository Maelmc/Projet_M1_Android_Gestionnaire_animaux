package fr.uparis.eude.projet.utilities

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.uparis.eude.projet.R
import fr.uparis.eude.projet.data.Animal
import fr.uparis.eude.projet.data.AnimalActivities
import fr.uparis.eude.projet.data.DefaultActivities
import fr.uparis.eude.projet.data.Species
import fr.uparis.eude.projet.launchAnimalIdentityActivity
import kotlinx.coroutines.flow.first
import java.util.Locale

class SpeciesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeciesScreen()
        }
    }
}

@Composable
fun SpeciesScreen(model : MainModel = viewModel()) {

    val context = LocalContext.current
    val intent = (context as SpeciesActivity).intent
    val name by remember { mutableStateOf(intent.getStringExtra("name"))}
    val idSpecies by remember { mutableStateOf(intent.getIntExtra("idSpecies", 0))}
    val speciesNames by remember { mutableStateOf(intent.getStringArrayExtra("speciesNames"))}

    var addDefaultActivityBool by remember { mutableStateOf(false) }
    if (addDefaultActivityBool) {
        AddDefaultActivity(name!!, model, context, fin = { addDefaultActivityBool = false })
    }

    var editDefaultActivityBool by remember { mutableStateOf(false) }
    val activityToEdit by remember { mutableStateOf<MutableList<DefaultActivities>>(mutableListOf()) }
    if (editDefaultActivityBool) {
        EditDefaultActivity(activityToEdit.first(), model, context, fin = { editDefaultActivityBool = false; activityToEdit.clear() })
    }

    LaunchedEffect(key1=idSpecies){
        (model::getDefaultActivitiesById)(idSpecies)
    }
    val activities by model.defaultActivities
    activities.sortBy { it.date } //TODO: trouver un meilleur moyen de trier, là 11h est avant 9h

    Column (
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Species Details")
        Text(text = "Name : $name") //TODO : update name (and animalNames) when using editAnimal, right now it's just what's in the intent
        ShowDefaultActivities(activities,name!!,model,context, onClick = { item -> activityToEdit.add(item); editDefaultActivityBool = true })
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { addDefaultActivityBool = true }) { Text("Add Activity") }
            Button(onClick = { context.finish() }) { Text("Go back") }
        }
    }
}

@Composable
fun AddDefaultActivity(name: String, model : MainModel, context : Context, fin : () -> Unit) {
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Format de l'heure : HH:MM
                date = String.format("%02d:%02d", hourOfDay, minute)
            },
            12, // Heure par défaut
            0,  // Minute par défaut
            true // Format 24h
        )
    }
    AlertDialog(
        onDismissRequest = fin,
        confirmButton = {
            Button(onClick = {
                when {
                    desc == "" -> Toast.makeText(context, "Give a description to the activity", Toast.LENGTH_SHORT).show()
                    date == "" -> Toast.makeText(context, "Give a date to the activity", Toast.LENGTH_SHORT).show()
                    else -> {
                        (model::insertDefaultActivities)(context,name,desc,date)
                        fin()
                    }
                }
            }) { Text("Add") }
        },
        dismissButton = { Button(onClick = fin) { Text("Dismiss") } },
        text = {
            Column {
                TextField(
                    value = desc,
                    onValueChange = { desc = it.trim() },
                    label = { Text("Description :") }
                )
                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Select date")}
                Text("Date: $date")
            }
        }
    )
}

@Composable
fun EditDefaultActivity(item: DefaultActivities, model : MainModel, context : Context, fin : () -> Unit) {
    var desc by remember { mutableStateOf(item.description) }
    var date by remember { mutableStateOf(item.date) }
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Format de l'heure : HH:MM
                date = String.format("%02d:%02d", hourOfDay, minute)
            },
            12, // Heure par défaut
            0,  // Minute par défaut
            true // Format 24h
        )
    }
    AlertDialog(
        onDismissRequest = fin,
        confirmButton = {
            Button(onClick = {
                when {
                    desc == "" -> Toast.makeText(context, "Give a description to the activity", Toast.LENGTH_SHORT).show()
                    date == "" -> Toast.makeText(context, "Give a date to the activity", Toast.LENGTH_SHORT).show()
                    else -> {
                        (model::updateDefaultActivities)(item.idDefaultActivities,item.idSpecies,desc,date)
                        fin()
                    }
                }
            }) { Text("Edit") }
        },
        dismissButton = { Button(onClick = fin) { Text("Dismiss") } },
        text = {
            Column {
                TextField(
                    value = desc,
                    onValueChange = { desc = it.trim() },
                    label = { Text("Description :") }
                )
                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Select date")}
                Text("Date: $date")
                Button(onClick = {
                    (model::deleteDefaultActivities)(item.idDefaultActivities)
                    fin()
                }) { Text("Delete") }
            }
        }
    )
}

@Composable
fun DefaultActivityItem(index: Int, item: DefaultActivities, onClick: (DefaultActivities) -> Unit) {
    Card (
        onClick = {
            onClick(item)
        },
        colors = CardDefaults.cardColors(
            containerColor = when{
                index%2==0 -> colorResource(R.color.teal_200)
                else -> colorResource(R.color.purple_200)
            }
        )
    ) {
        Row {
            Text(item.description)
            Text(" à " + item.date)
        }
    }
}

@Composable
fun ShowDefaultActivities(activitiesList: MutableList<DefaultActivities>, name: String, model: MainModel, context: Context, onClick: (DefaultActivities) -> Unit) {
    LazyColumn (modifier = Modifier.fillMaxHeight(0.5f))
    { itemsIndexed(activitiesList) { index, item -> DefaultActivityItem(index, item, onClick) } }
}