package fr.uparis.eude.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//Link an activity to an animal
//a quite weak idea, for example : what do we do if we want also an activity to N dogs?
//Add N times this activity in the table? Worth debating
@Entity(
    //primaryKeys = ["idAnimal", "description","date"],
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["idAnimal"],
        childColumns = ["idAnimal"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["idAnimal","description","date"], unique = true)]
)
data class AnimalActivities(
    @PrimaryKey(autoGenerate = true)
    val idActivity: Int = 0,
    val idAnimal: Int,
    val description: String,
    val date: String
)
