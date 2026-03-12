package fr.uparis.eude.projet.utilities

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.uparis.eude.projet.R
import fr.uparis.eude.projet.data.Animal
import fr.uparis.eude.projet.data.AnimalActivities
import fr.uparis.eude.projet.data.DefaultActivities
import fr.uparis.eude.projet.data.ProjectDB
import fr.uparis.eude.projet.data.SpecOfA
import fr.uparis.eude.projet.data.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainModel(application: Application) : AndroidViewModel (application) {
    private val dao by lazy { ProjectDB.getDataBase(application).myDao() }
    //TODO : check if those variables are sufficient
    val idAnimal = mutableStateOf(0)
    val idSpecies = mutableStateOf(0)
    val description = mutableStateOf("")
    var activities = mutableStateOf<MutableList<AnimalActivities>>(mutableListOf())
    var defaultActivities = mutableStateOf<MutableList<DefaultActivities>>(mutableListOf())

    val specOfA = mutableStateOf("") //the species of the animal, not something related to the table SpecOfA

    val allAnimalsFlow = dao.loadAllAnimals()
    val allSpeciesFlow = dao.loadAllSpecies()
    //val allSpecOfAFlow = dao.loadAllSpecOfA()
    val allAnimalActivitiesFlow = dao.loadAllAnimalActivities()

    val animalSelected = mutableStateOf<MutableList<Animal?>>(mutableListOf())

    fun remplirSpecies() {
        val species = getApplication<Application>().resources.getStringArray(R.array.species)
        viewModelScope.launch(Dispatchers.IO) {
            for (i in species) {
                dao.insertSpecies(Species(name = i.trim()))
            }
        }
    }

    fun remplirDefaultActivities() {
        val species =
            getApplication<Application>().resources.getStringArray(R.array.species_defaultactivities)
        val descs =
            getApplication<Application>().resources.getStringArray(R.array.description_defaultactivities)
        val dates =
            getApplication<Application>().resources.getStringArray(R.array.date_defaultactivities)
        val listspecies = mutableListOf<String>()
        val listdescs = mutableListOf<String>()
        val listdates = mutableListOf<String>()
        var tmp = 0
        viewModelScope.launch(Dispatchers.IO) {
            for (i in species) {
                listspecies.add(i)
                tmp++
            }
            tmp = 0
            for (i in descs) {
                listdescs.add(i)
                tmp++
            }
            tmp = 0
            for (i in dates) {
                listdates.add(i)
                tmp++
            }
            for (k in 0..<species.size) {
                println("INSERT ${getIdSpeciesByName2(listspecies[k])} ${listdescs[k]}")
                if (getIdSpeciesByName2(listspecies[k]) != 0) { //TODO: trouver une solution plus élégante que ce "if" et une fonction getIdSpeciesByName2
                    dao.insertDefaultActivities(DefaultActivities(idSpecies = getIdSpeciesByName2(listspecies[k]), description = listdescs[k].trim(), date = listdates[k]))
                }
            }
        }
    }

    //Insertions
    private fun checkInsertion(context: Context, result : Long) {
        if (result == -1L){
            Toast.makeText(context, "Insertion failed", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Insertion successful", Toast.LENGTH_SHORT).show()
        }
    }
    fun insertAnimal(context: Context, name : String) {
        viewModelScope.launch(Dispatchers.IO) {
            idAnimal.value = (dao.insertAnimal(Animal(name = name.trim()))).toInt()
            withContext(Dispatchers.Main) {
                checkInsertion(context, idAnimal.value.toLong())
            }
        }
    }
    fun insertSpecies(context: Context, name : String) {
        viewModelScope.launch(Dispatchers.IO) {
            idSpecies.value = (dao.insertSpecies(Species(name = name.trim()))).toInt()
            withContext(Dispatchers.Main) {
                checkInsertion(context, idSpecies.value.toLong())
            }
        }
    }
    fun insertSpecOfA(context: Context, nameAnimal: String, nameSpecies: String) {
        var result = -1L
        viewModelScope.launch(Dispatchers.IO) {
            getIdAnimalByName(nameAnimal)
            getIdSpeciesByName(nameSpecies)
            withContext(Dispatchers.Main) {}
            result = dao.insertSpecOfA(SpecOfA(idAnimal = idAnimal.value, idSpecies = idSpecies.value))
            withContext(Dispatchers.Main) {
                checkInsertion(context, result)
            }
        }
    }

    fun insertAnimalActivities(context: Context, nameAnimal: String, desc: String, date: String) : Long {
        var result = -1L
        viewModelScope.launch(Dispatchers.IO) {
            val id = getIdAnimalByName2(nameAnimal)
            result = dao.insertAnimalActivities(AnimalActivities(idAnimal = id, description = desc.trim(), date = date))
            withContext(Dispatchers.Main) {
                checkInsertion(context, result)
            }
        }
        println("insertAnimalActivities : $result")
        return result
    }

    fun insertDefaultActivities(context: Context, nameSpecies: String, desc: String, date: String) : Long {
        var result = -1L
        viewModelScope.launch(Dispatchers.IO) {
            val id = getIdSpeciesByName2(nameSpecies)
            result = dao.insertDefaultActivities(DefaultActivities(idSpecies = id, description = desc.trim(), date = date))
            withContext(Dispatchers.Main) {
                checkInsertion(context, result)
            }
        }
        return result
    }

    fun insertDefaultActivitiesOfAnimal(context: Context, nameAnimal: String, nameSpecies: String) : Long {
        var result = -1L
        getIdSpeciesByName(nameSpecies)
        getIdAnimalByName(nameAnimal)
        viewModelScope.launch(Dispatchers.IO) {
            val activities = dao.getDefaultActivitiesBySpecies(idSpecies.value).first()
            for (i in activities) {
                result = dao.insertAnimalActivities(AnimalActivities(idAnimal = idAnimal.value, description = i.description.trim(), date = i.date))
                withContext(Dispatchers.Main) {
                    checkInsertion(context, result)
                }
            }
        }
        println("insertDefaultActivities : $result")
        return result
    }

    //Deletions
    //remove an x of the table x using its id
    //TODO : check if there is not a better way of doing this, maybe by using the variables up above
    fun deleteAnimal(idAnimal: Int) : Int {
        var result = -1
        viewModelScope.launch(Dispatchers.IO) {
            result = dao.delete(Animal(idAnimal = idAnimal, name = ""))
        }
        return result
    }

    fun deleteSpecies(idSpecies: Int) : Int {
        var result = -1
        viewModelScope.launch(Dispatchers.IO) {
            result = dao.delete(Species(idSpecies = idSpecies, name = ""))
        }
        return result
    }

    suspend fun deleteSpecOfA(idAnimal: Int, idSpecies: Int) : Int {
        var result = -1
        viewModelScope.launch(Dispatchers.IO) {
            result = dao.delete(SpecOfA(idAnimal = idAnimal, idSpecies = idSpecies))
        }
        return result
    }

    fun deleteAnimalActivities(idActivity: Int) : Int {
        var result = -1
        viewModelScope.launch(Dispatchers.IO) {
            result = dao.delete(AnimalActivities(idActivity = idActivity, idAnimal = 0, description = "", date = ""))
        }
        return result
    }

    fun deleteDefaultActivities(idActivity: Int) : Int {
        var result = -1
        viewModelScope.launch(Dispatchers.IO) {
            result = dao.delete(DefaultActivities(idDefaultActivities = idActivity, idSpecies = 0, description = "", date = ""))
        }
        return result
    }

    fun deleteAnimals(idAnimals: List<Int>) : List<Int> {
        val result = mutableListOf<Int>()
        for (id in idAnimals) {
            result.add(deleteAnimal(id))
        }
        return result
    }

    fun deleteSpeciess(idSpecies: List<Int>) : List<Int> {
        val result = mutableListOf<Int>()
        for (id in idSpecies) {
            result.add(deleteSpecies(id))
        }
        return result
    }

    suspend fun deleteSpecOfA(idAnimals: List<Int>, idSpecies: List<Int>) : List<Int> {
        val result = mutableListOf<Int>()
        for ((ida,ids) in idAnimals.zip(idSpecies)) {
            result.add(deleteSpecOfA(ida, ids))
        }
        return result
    }

    //Update
    fun updateAnimal(idAnimal: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateAnimal(Animal(idAnimal = idAnimal, name = name.trim()))
        }
    }

    fun updateAnimalActivities(idActivity: Int, idAnimal: Int, desc: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateAnimalActivities(AnimalActivities(idActivity = idActivity, idAnimal = idAnimal, description = desc.trim(), date = date))
        }
    }

    fun updateDefaultActivities(idActivity: Int, idSpecies: Int, desc: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateDefaultActivities(DefaultActivities(idDefaultActivities = idActivity, idSpecies = idSpecies, description = desc.trim(), date = date))
        }
    }

    //functions
    fun getIdAnimalByName(name: String){
        viewModelScope.launch(Dispatchers.IO) {
            idAnimal.value = dao.getIdAnimalByName(name.trim())
        }
    }

    fun getIdAnimalByName2(name: String): Int{
        return dao.getIdAnimalByName(name.trim())
    }

    fun getIdSpeciesByName(name: String){
        viewModelScope.launch(Dispatchers.IO) {
            idSpecies.value = dao.getIdSpeciesByName(name.trim())
        }
    }

    fun getIdSpeciesByName2(name: String): Int{
        return dao.getIdSpeciesByName(name.trim())
    }

    suspend fun getSpecOfA(idAnimal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            specOfA.value = dao.getSpecOfA(idAnimal)
        }.join()
    }

    suspend fun getDefaultActivitiesById(idSpecies: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            defaultActivities.value = dao.getDefaultActivitiesBySpecies(idSpecies).first()
        }.join()
    }

    suspend fun getAnimalActivitiesById(idAnimal: Int){
        viewModelScope.launch(Dispatchers.IO) {
            activities.value = dao.getAnimalActivitiesById(idAnimal).first()
        }.join()
    }

}