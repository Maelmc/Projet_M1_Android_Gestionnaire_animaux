package fr.uparis.eude.projet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.uparis.eude.projet.data.Animal
import fr.uparis.eude.projet.data.Species
import fr.uparis.eude.projet.ui.theme.ProjetTheme
import fr.uparis.eude.projet.utilities.AnimalIdentityActivity
import fr.uparis.eude.projet.utilities.MainModel
import fr.uparis.eude.projet.utilities.SpeciesActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MyTopBar() },

                ) {
                    innerPadding ->
                    MyScreen(
                        modifier = Modifier.padding(innerPadding),
                        model = viewModel()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar() = CenterAlignedTopAppBar(title = { Text(text = "Animals", style = MaterialTheme.typography.headlineMedium) } )

@Composable
fun MyBottomBar(navController: NavHostController) = NavigationBar {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBarItem(
        selected = currentRoute == "home" ,
        onClick = { navController.navigate("home") {launchSingleTop = true} } ,
        icon = { Icon(Icons.Default.Home,"Maison")}
    )
    NavigationBarItem(
        selected = currentRoute == "species" ,
        onClick = { navController.navigate("species") } ,
        icon = { Icon(Icons.Default.Menu,"Espèces")}
    )
    NavigationBarItem(
        selected = currentRoute == "settings" ,
        onClick = { navController.navigate("settings")} ,
        icon = { Icon(Icons.Default.Edit,"Paramètres")}
    )
}

@Composable
fun MyScreen(modifier: Modifier = Modifier, model: MainModel = viewModel()) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MyTopBar() },
        bottomBar = { MyBottomBar(navController) }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier) {
            composable("home") { AnimalScreen(
                modifier = Modifier.padding(innerPadding),
                model = viewModel()
                //ask = { requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0) }
            ) }
            composable("species") { SpeciesScreen(
                modifier = Modifier.padding(innerPadding),
                model = viewModel()
                //ask = { requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0) }
            ) }
            composable("settings") { SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                model = viewModel()
                //ask = { requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0) }
            ) }
        }
    }
}

@Composable
fun AnimalScreen(modifier: Modifier = Modifier, model : MainModel) { //, ask : () -> Unit

    val context = LocalContext.current

    val animalsList by model.allAnimalsFlow.collectAsState(listOf())
    val animalNames = animalsList.map { i -> i.name }

    LaunchedEffect(key1=null){
        (model::remplirSpecies)()
        (model::remplirDefaultActivities)()
    }
    val speciesList by model.allSpeciesFlow.collectAsState(listOf())
    var speciesNames = speciesList.map { i -> i.name }
    if (speciesNames.isEmpty()) { speciesNames = listOf("") }

    var addAnimalBool by remember { mutableStateOf(false) }
    if (addAnimalBool) {
        AddAnimal(model, context, animalNames, speciesNames, fin = { addAnimalBool = false})
    }
    Column (
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        ShowAnimals(animalsList, context, animalNames)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(onClick = { addAnimalBool = true }) { Text("Add animal") }
            //Button(onClick = { ask() }) { Text("Ask for notifications") } //marche pas jsp pk, ça marche dans le projet du prof mais pas ici, alors que c'est la même ligne
        }
    }
}

@Composable
fun SpeciesScreen(modifier: Modifier = Modifier, model : MainModel) {
    //TODO: tout
    //Afficher les espèces
    //Cliquer sur une espèce affiche les activités par défaut de l'espèce -> activité ? alertdialog ?
        //On peut ensuite les modifier, en ajouter ou en supprimer
    //Un bouton permet d'ajouter une espèce
    //En gros, très similaire à AnimalScreen
    //similar to AnimalScreen
    //we are going to use a lazycolumn to show the species
    //and we are going to use a button to add a species
    //a button to delete a species
    //a button to edit a species
    val context = LocalContext.current
    val speciesList by model.allSpeciesFlow.collectAsState(listOf())
    var speciesNames = speciesList.map { i -> i.name }
    if (speciesNames.isEmpty()) { speciesNames = listOf("") }
    var addSpeciesBool by remember { mutableStateOf(false) }
    if (addSpeciesBool) {
        AddSpecies(model, context, speciesNames, fin = { addSpeciesBool = false})
    }
    Column (
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        ShowSpecies(speciesList, model, context, speciesNames)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){ Button(onClick = { addSpeciesBool = true }) { Text("Add species") } }
    }

}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, model : MainModel) {
    //TODO: tout
    //Taille de la police
    //Couleur des listes
    //?????
}

@Composable
fun AddAnimal(model : MainModel, context: Context, animalNames: List<String>, speciesNames: List<String>,fin : () -> Unit) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Dog") }
    AlertDialog(
        onDismissRequest = fin,
        confirmButton = {
            Button(onClick = {
                when {
                    name == "" -> Toast.makeText(context, "The animal needs a name", Toast.LENGTH_SHORT).show()
                    animalNames.contains(name) -> Toast.makeText(context, "There is already an animal with this name", Toast.LENGTH_SHORT).show()
                    else -> {
                        (model::insertAnimal)(context, name)
                        (model::insertSpecOfA)(context, name, species)
                        (model::insertDefaultActivitiesOfAnimal)(context,name,species)
                        fin()
                    }
                }
            }){ Text("Confirm") }
        }, dismissButton = {
            Button(onClick = fin ) { Text("Dismiss") }
        }, text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {name = it.trim()},
                    label = { Text("Nom :") }
                )
                DropDownBasic(
                    list = speciesNames,
                    onItemSelected = { selectedSpecies -> species = selectedSpecies }
                )
            }
        }
    )
}
@Composable
fun AddSpecies(model : MainModel, context: Context, speciesNames: List<String>, fin : () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = fin,
        confirmButton = {
            Button(onClick = {
                when {
                    name == "" -> Toast.makeText(context, "The species needs a name", Toast.LENGTH_SHORT).show()
                    speciesNames.contains(name) -> Toast.makeText(context, "There is already a species with this name", Toast.LENGTH_SHORT).show()
                    else -> {
                        (model::insertSpecies)(context, name)
                        fin()
                    }
                }
            }){ Text("Confirm") }
        }, dismissButton = {
            Button(onClick = fin ) { Text("Dismiss") }
        }, text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {name = it.trim()},
                    label = { Text("Nom :") }
                )
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownBasic(list: List<String>, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var choose by remember { mutableStateOf(list.firstOrNull() ?: "") }
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(PrimaryNotEditable, true),
                readOnly = true,
                value = choose,
                onValueChange = { },
                label =  { Text("Choice")},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            )  {
                list.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            choose = it
                            expanded = false
                            onItemSelected(it)
                        }
                    )
                }
            }
        }
    }
}

fun launchAnimalIdentityActivity(context: Context, animal : Animal, animalNames : List<String>) {
    val intent = Intent(context, AnimalIdentityActivity::class.java)
    intent.putExtra("name", animal.name)
    intent.putExtra("idAnimal", animal.idAnimal)
    intent.putExtra("animalNames", Array(animalNames.size) { animalNames[it] })
    context.startActivity(intent)
}

fun launchSpeciesActivity(context: Context, species : Species, speciesNames : List<String>) {
    val intent = Intent(context, SpeciesActivity::class.java)
    intent.putExtra("name", species.name)
    intent.putExtra("idSpecies", species.idSpecies)
    intent.putExtra("speciesNames", Array(speciesNames.size) { speciesNames[it] })
    context.startActivity(intent)
}

@Composable
fun AnimalItem(index: Int, item: Animal, context: Context, animalNames: List<String>) {
    Card (
        onClick = {
            launchAnimalIdentityActivity(context, item, animalNames)
        },
        colors = CardDefaults.cardColors(
            containerColor = when{
                index%2==0 -> colorResource(R.color.teal_200)
                else -> colorResource(R.color.purple_200)
            }
        )
    ) {
        Row {
            Text("idAnimal : " + item.idAnimal.toString())
            Text(", name : " + item.name)
        }
    }
}
@Composable
fun SpeciesItem(index: Int, item: Species, deleteOne: () -> Unit, context: Context, speciesNames: List<String>) {
    Card (
        onClick = {
            launchSpeciesActivity(context, item, speciesNames)
        },
        colors = CardDefaults.cardColors(
            containerColor = when{
                index%2==0 -> colorResource(R.color.teal_200)
                else -> colorResource(R.color.purple_200)
            }
        )
    ) {
        Row {
            Text("idSpecies : " + item.idSpecies.toString())
            Text(", name : " + item.name)
            IconButton(onClick = { deleteOne() }) { Icon(Icons.Sharp.Delete, contentDescription = "delete") }
        }
    }
}
@Composable
fun ShowAnimals(animalsList: List<Animal>, context: Context, animalNames: List<String>) {
    LazyColumn (modifier = Modifier.fillMaxHeight(0.5f))
    { itemsIndexed(animalsList) { index, item -> AnimalItem(index = index, item = item, context = context, animalNames = animalNames) } }
}
@Composable
fun ShowSpecies(speciesList: List<Species>, model : MainModel, context: Context, speciesNames: List<String>) {
    LazyColumn (modifier = Modifier.fillMaxHeight(0.5f))
    { itemsIndexed(speciesList) { index, item -> SpeciesItem(index = index, item = item, deleteOne = { (model::deleteSpecies)(item.idSpecies) }, context, speciesNames) } }
}
