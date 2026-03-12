package fr.uparis.eude.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//Links an activity to a species
@Entity(
    //primaryKeys = ["idSpecies", "description", "date"],
    foreignKeys = [ForeignKey(
        entity = Species::class,
        parentColumns = ["idSpecies"],
        childColumns = ["idSpecies"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["idSpecies","description","date"], unique = true)]
)

data class DefaultActivities(
    @PrimaryKey(autoGenerate = true)
    val idDefaultActivities: Int = 0,
    val idSpecies: Int,
    val description: String,
    val date: String
)