package fr.uparis.eude.projet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDao {
    //specifying the return type will let us check if the insertion was successful
    //Insertions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpecies(species: Species):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecOfA(specOfA: SpecOfA):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimalActivities(animalActivities: AnimalActivities):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultActivities(defaultActivities: DefaultActivities):Long

    //Deletions
    @Delete
    suspend fun delete(animal: Animal):Int

    @Delete
    suspend fun delete(species: Species):Int

    @Delete
    suspend fun delete(specOfA: SpecOfA):Int

    @Delete
    suspend fun delete(animalActivities: AnimalActivities):Int

    @Delete
    suspend fun delete(defaultActivities: DefaultActivities):Int

    //Query functions
    //TODO : only add the ones we currently need
    @Query("SELECT * FROM Animal")
    fun loadAllAnimals() : Flow<List<Animal>>

    @Query("SELECT * FROM Species")
    fun loadAllSpecies() : Flow<List<Species>>

    @Query("SELECT * FROM SpecOfA")
    fun loadAllSpecOfA() : Flow<List<SpecOfA>>

    @Query("SELECT * FROM AnimalActivities")
    fun loadAllAnimalActivities() : Flow<List<AnimalActivities>>

    @Query("SELECT * FROM DefaultActivities")
    fun loadAllDefaultActivities() : Flow<List<DefaultActivities>>

    @Query("UPDATE Animal SET name = :name WHERE idAnimal = :idAnimal")
    fun updateAnimalName(idAnimal: Int, name: String)

    @Query("SELECT idAnimal FROM Animal WHERE name = :name")
    fun getIdAnimalByName(name: String) : Int

    @Query("SELECT idSpecies FROM Species WHERE name = :name")
    fun getIdSpeciesByName(name: String) : Int

    @Query(
            "SELECT Species.name " +
            "FROM Species " +
            "JOIN SpecOfA ON Species.idSpecies = SpecOfA.idSpecies " +
            "WHERE SpecOfA.idAnimal = :idAnimal")
    fun getSpecOfA(idAnimal: Int) : String

    @Query("SELECT * FROM DefaultActivities WHERE idSpecies = :idSpecies")
    fun getDefaultActivitiesBySpecies(idSpecies: Int) : Flow<MutableList<DefaultActivities>>

    @Query("SELECT * FROM AnimalActivities WHERE idAnimal = :idAnimal")
    fun getAnimalActivitiesById(idAnimal: Int) : Flow<MutableList<AnimalActivities>>

    //Update functions
    @Update
    suspend fun updateAnimal(animal: Animal)

    @Update
    suspend fun updateSpecies(species: Species)

    @Update
    suspend fun updateSpecOfA(specOfA: SpecOfA)

    @Update
    suspend fun updateAnimalActivities(animalActivities: AnimalActivities)

    @Update
    suspend fun updateDefaultActivities(defaultActivities: DefaultActivities)
}