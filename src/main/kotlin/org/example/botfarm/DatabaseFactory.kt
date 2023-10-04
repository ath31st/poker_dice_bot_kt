package org.example.botfarm

import kotlinx.coroutines.Dispatchers
import org.example.botfarm.entity.Players
import org.example.botfarm.entity.Results
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The `DatabaseFactory` object is responsible for initializing and managing the SQLite database used by the poker bot.
 */
object DatabaseFactory {
    /**
     * Initializes the SQLite database and creates necessary tables if they don't exist.
     */
    fun init() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcURL = "jdbc:sqlite:./poker_dice_kt.db"
        val database = Database.connect(jdbcURL, driverClassName)

        transaction(database) {
            SchemaUtils.create(Players)
            SchemaUtils.create(Results)
        }
    }

    /**
     * Executes a database query block in a coroutine context.
     *
     * @param block The database query block to execute.
     * @return The result of the database query.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
