package it.diab.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

import it.diab.db.dao.GlucoseDao
import it.diab.db.dao.InsulinDao
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.SingletonHolder

@Database(entities = [(Glucose::class), (Insulin::class)], version = 1)
abstract class AppDatabase protected constructor() : RoomDatabase() {

    abstract fun glucose(): GlucoseDao
    abstract fun insulin(): InsulinDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, "diab_database")
                .build()
    })
} 
