package com.squareup.sqldelight.drivers.native

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext.deleteDatabase
import com.squareup.sqldelight.driver.test.TransacterTest

class NativeTransacterTest : TransacterTest() {
  override fun setupDatabase(schema: SqlSchema): SqlDriver {
    val name = "testdb"
    deleteDatabase(name)
    return NativeSqliteDriver(schema, name)
  }
}
