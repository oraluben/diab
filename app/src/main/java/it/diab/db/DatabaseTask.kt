package it.diab.db

import android.os.AsyncTask

abstract class DatabaseTask<I, O>(protected var mDatabase: AppDatabase) : AsyncTask<I, Void, O>()
