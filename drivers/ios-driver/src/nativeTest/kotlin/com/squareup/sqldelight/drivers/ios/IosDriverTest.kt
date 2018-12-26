package com.squareup.sqldelight.drivers.ios

import co.touchlab.sqliter.NativeFileContext.deleteDatabase
import com.squareup.sqldelight.db.SqlDatabase
import com.squareup.sqldelight.db.SqlPreparedStatement.Type.SELECT
import com.squareup.sqldelight.driver.test.DriverTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IosDriverTest : DriverTest() {
  override fun setupDatabase(schema: SqlDatabase.Schema): SqlDatabase {
    val name = "testdb"
    deleteDatabase(name)
    return NativeSqlDatabase(schema, name)
  }

  // TODO: https://github.com/JetBrains/kotlin-native/issues/2328

  @BeforeTest fun setup2() {
    super.setup()
  }

  @AfterTest fun tearDown2() {
    super.tearDown()
  }

  // Sanity check of the driver.
  @Test fun basicTest() {
    val cursor = database.getConnection().prepareStatement(0, "SELECT 1", SELECT, 0).executeQuery()
    cursor.next()
    assertEquals(1, cursor.getLong(0))
    cursor.close()
  }

  @Test fun `insert can run multiple times2`() {
    super.`insert can run multiple times`()
  }

  @Test fun `query can run multiple times2`() {
    super.`query can run multiple times`()
  }

  @Test fun `SqlResultSet getters return null if the column values are NULL2`() {
    super.`SqlResultSet getters return null if the column values are NULL`()
  }
}