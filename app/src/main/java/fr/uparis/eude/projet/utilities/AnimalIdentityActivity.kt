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
import fr.uparis.eude.projet.launchAnimalIdentityActivity
import kotlinx.coroutines.flow.first
import java.util.Locale

class AnimalIdentityActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimalIdentityScreen()
        }
    }
}

@Composable
fun AnimalIdentityScreen(model : MainModel = viewModel()) {

    val context = LocalContext.current
    val intent = (context as AnimalIdentityActivity).intent
    val name by remember { mutableStateOf(intent.getStringExtra("name"))}
    val idAnimal by remember { mutableStateOf(intent.getIntExtra("idAnimal", 0))}
    val animalNames by remember { mutableStateOf(intent.getStringArrayExtra("animalNames"))}

    var editAnimalBool by remember { mutableStateOf(false) }
    if (editAnimalBool) {
        EditAnimal(idAnimal, model, context, fin = { editAnimalBool = false }, animalNames!!)
    }

    var deleteAnimalBool by remember { mutableStateOf(false) }
    if (deleteAnimalBool) {
        deleteAnimal(idAnimal, model)
        context.finish()
    }

    var addActivityBool by remember { mutableStateOf(false) }
    if (addActivityBool) {
        AddActivity(name!!, model, context, fin = { addActivityBool = false })
    }

    var editActivityBool by remember { mutableStateOf(false) }
    val activityToEdit by remember { mutableStateOf<MutableList<AnimalActivities>>(mutableListOf()) }
    if (editActivityBool) {
        EditActivity(activityToEdit.first(), model, context, fin = { editActivityBool = false; activityToEdit.clear() })
    }

    LaunchedEffect(key1=idAnimal){
        (model::getSpecOfA)(idAnimal)
        (model::getAnimalActivitiesById)(idAnimal)
    }
    val specOfA by model.specOfA
    val activities by model.activities
    activities.sortBy { it.date } //TODO: trouver un meilleur moyen de trier, là 11h est avant 9h

    Column (
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Animal Identity")
        Text(text = "Name : $name") //TODO : update name (and animalNames) when using editAnimal, right now it's just what's in the intent
        Text(text = "Species : $specOfA")
        ShowActivities(activities,name!!,model,context, onClick = { item -> activityToEdit.add(item); editActivityBool = true })
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { editAnimalBool = true }) { Text("Edit Animal") }
            Button(onClick = { deleteAnimalBool = true }) { Text("Delete Animal") }
        }
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { addActivityBool = true }) { Text("Add Activity") }
            Button(onClick = { context.finish() }) { Text("Go back") }
        }
    }
}

@Composable
fun EditAnimal(idAnimal: Int, model : MainModel, context : Context, fin : () -> Unit, animalNames: Array<String>) {
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = fin,
        confirmButton = {
            Button(onClick = {
                when {
                    newName == "" ->
                        Toast.makeText(context, "Give a new name to the animal", Toast.LENGTH_SHORT).show()
                    animalNames.contains(newName) ->
                        Toast.makeText(context, "There is already an animal with this name", Toast.LENGTH_SHORT).show()
                    else -> {
                        (model::updateAnimal)(idAnimal, newName.trim())
                        fin()
                    }
                }
            }) { Text("Edit") }
        },
        dismissButton = { Button(onClick = fin) { Text("Dismiss") } },
        text = {
            Column {
                TextField(
                    value = newName,
                    onValueChange = { newName = it.trim() },
                    label = { Text("Nom :") }
                )
            }
        }
    )
}

@Composable
fun deleteAnimal(idAnimal: Int, model : MainModel) = (model::deleteAnimal)(idAnimal)

@Composable
fun AddActivity(name: String, model : MainModel, context : Context, fin : () -> Unit) {
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
                        (model::insertAnimalActivities)(context,name,desc,date)
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
fun EditActivity(item: AnimalActivities, model : MainModel, context : Context, fin : () -> Unit) {
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
                        (model::updateAnimalActivities)(item.idActivity,item.idAnimal,desc,date)
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
                    (model::deleteAnimalActivities)(item.idActivity)
                    fin()
                }) { Text("Delete") }
            }
        }
    )
}

@Composable
fun ActivityItem(index: Int, item: AnimalActivities, onClick: (AnimalActivities) -> Unit) {
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
fun ShowActivities(activitiesList: List<AnimalActivities>, name: String, model: MainModel, context: Context, onClick: (AnimalActivities) -> Unit) {
    LazyColumn (modifier = Modifier.fillMaxHeight(0.5f))
    { itemsIndexed(activitiesList) { index, item -> ActivityItem(index, item, onClick) } }
}