package fr.uparis.eude.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

//Such as the example in the lesson n°6
//We can try using Embedded or Relation later for better code

@Entity(
    primaryKeys = ["idAnimal", "idSpecies"],
    foreignKeys =
        [ForeignKey(
            entity = Animal::class,
            parentColumns = ["idAnimal"],
            childColumns = ["idAnimal"],
            onDelete = ForeignKey.CASCADE),
            ForeignKey(
                entity = Species::class,
                parentColumns = ["idSpecies"],
                childColumns = ["idSpecies"],
                onDelete = ForeignKey.CASCADE
            )],
    indices = [Index(value = ["idAnimal"]),Index(value = ["idSpecies"])]
)
data class SpecOfA(
    val idAnimal: Int,
    val idSpecies: Int
)
