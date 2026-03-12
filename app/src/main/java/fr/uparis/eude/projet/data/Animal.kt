package fr.uparis.eude.projet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

//idAnimal must be the Primary key, name must be unique
@Entity(indices = [Index(value = ["name"], unique = true)])
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val idAnimal: Int = 0,
    val name: String
)
