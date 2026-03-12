package fr.uparis.eude.projet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

//idSpecies must be the Primary key, name must be unique
@Entity(indices = [Index(value = ["name"], unique = true)])
data class Species(
    @PrimaryKey(autoGenerate = true)
    val idSpecies: Int = 0,
    val name: String
)
