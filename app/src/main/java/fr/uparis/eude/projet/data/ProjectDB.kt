package fr.uparis.eude.projet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Reminder : to empty the database, increment or decrement the version number
@Database(entities = [Animal::class, Species::class, SpecOfA::class, AnimalActivities::class, DefaultActivities::class], version = 9)
abstract class ProjectDB : RoomDatabase() {
    abstract fun myDao() : MyDao
    //TODO : check if we have to make a companion object for each class or not
    //Gemini told me no, lesson n°6 page 24 told me otherwise, so I'm not sure
    companion object {
        @Volatile
        private var instance : ProjectDB? = null
        fun getDataBase(context: Context) : ProjectDB {
            if (instance != null) return instance!!
            val db = Room.databaseBuilder(
                context.applicationContext,
                ProjectDB::class.java,
                "ProjectDB"
            ).fallbackToDestructiveMigration().build()
            instance = db
            return instance!!
        }
    }
}