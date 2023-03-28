package app.cash.sqldelight.async.coroutines

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlinx.coroutines.runBlocking

fun SqlSchema.synchronous() = object : SqlSchema by this {
  override fun create(driver: SqlDriver) = QueryResult.Value(
    runBlocking {
      this@synchronous.create(driver).await()
    }
  )

  override fun migrate(
    driver: SqlDriver,
    oldVersion: Int,
    newVersion: Int,
    vararg callbacks: AfterVersion
  ) = QueryResult.Value(
    runBlocking { this@synchronous.migrate(driver, oldVersion, newVersion, *callbacks).await() }
  )
}