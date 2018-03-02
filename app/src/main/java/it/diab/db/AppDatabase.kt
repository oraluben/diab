package it.diab.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context

import it.diab.db.dao.GlucoseDao
import it.diab.db.dao.InsulinDao
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.SingletonHolder

@Database(entities = [(Glucose::class), (Insulin::class)], version = 2)
abstract class AppDatabase protected constructor() : RoomDatabase() {

    abstract fun glucose(): GlucoseDao
    abstract fun insulin(): InsulinDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, "diab_database")
                .addMigrations(Companion.MIGRATION_1_2)
                .build()
    }) {
        /**
         * DB version 2
         *
         * Glucose
         *     Remove "heavyMeal" column [Boolean]
         *     Add "eatLevel" column[Int]
         *
         * Insulin
         *     Add "isBasal" column [Boolean]
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                /* Glucose */
                // Edit the schema by creating a new "cloned" table
                database.execSQL("ALTER TABLE glucose RENAME TO tmp_glucose")
                database.execSQL("DROP INDEX IF EXISTS index_glucose_date_uid")

                database.execSQL("CREATE TABLE glucose (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, value INTEGER NOT NULL, date INTEGER NOT NULL, insulinId0 INTEGER NOT NULL, insulinValue0 REAL NOT NULL, insulinId1 INTEGER NOT NULL, insulinValue1 REAL NOT NULL, eatLevel INTEGER NOT NULL DEFAULT 1)")
                // Copy the content
                database.execSQL("INSERT INTO glucose(uid, value, date, insulinId0, insulinValue0, insulinId1, insulinValue1) SELECT uid, value, date, insulinId0, insulinValue0, insulinId1, insulinValue1 FROM tmp_glucose")
                database.execSQL("CREATE UNIQUE INDEX index_glucose_date_uid ON glucose (date, uid)")
                // Drop old table
                database.execSQL("DROP TABLE tmp_glucose")

                /* Insulin */
                database.execSQL("ALTER TABLE insulin ADD COLUMN isBasal INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}