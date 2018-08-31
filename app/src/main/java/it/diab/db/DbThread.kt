package it.diab.db

import java.util.concurrent.Callable
import java.util.concurrent.Executors

private val DB_EXECUTOR = Executors.newSingleThreadExecutor()

/**
 * Run blocks without returning a value on a db-dedicated thread
 */
fun runOnDbThread(operations: () -> Unit) {
    DB_EXECUTOR.submit { operations() }
}

/**
 * Run blocks returning a value on a db-dedicated thread
 */
fun <T> runOnDbThread(operations: () -> T): T {
    val result = DB_EXECUTOR.submit(Callable<T> { operations() })
    return result.get()
}

/**
 * Run blocks on a db-dedicated thread without returning a value,
 * then execute another block on the caller's thread
 */
fun <T> runOnDbThread(operations: () -> T, onCompleted: () -> Unit) {
    val task = DB_EXECUTOR.submit(Callable <T> { operations() })
    task.get().also { onCompleted() }
}


/**
 * Run blocks on a db-dedicated thread returning a value that's passed
 * to another block running on the caller's thread,
 * which eventually returns a value
 */
@Suppress("unused")
fun <T, R> runOnDbThread(operations: () -> T, onCompleted: (T) -> R): R {
    val task = DB_EXECUTOR.submit(Callable <T> { operations() })
    return onCompleted(task.get())
}




object DbThread {

    fun shutDown() {
        if (!DB_EXECUTOR.isShutdown) {
            DB_EXECUTOR.shutdown()
        }
    }
}
