package com.squareup.sqldelight.android

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.db.SqlDatabase
import com.squareup.sqldelight.db.SqlPreparedStatement.Type.DELETE
import com.squareup.sqldelight.db.SqlPreparedStatement.Type.INSERT
import com.squareup.sqldelight.db.SqlPreparedStatement.Type.SELECT
import com.squareup.sqldelight.db.use
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DriverTest {
  private lateinit var database: SqlDatabase

  @Before fun setup() {
    val configuration = SupportSQLiteOpenHelper.Configuration.builder(RuntimeEnvironment.application)
        .callback(object : SupportSQLiteOpenHelper.Callback(1) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("""
              |CREATE TABLE test (
              |  id INTEGER PRIMARY KEY,
              |  value TEXT
              |);
            """.trimMargin())
          }

          override fun onUpgrade(
            db: SupportSQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
          ) {
          }
        })
        .build()
    val openHelper = FrameworkSQLiteOpenHelperFactory().create(configuration)
    database = SqlDelightDatabaseHelper(openHelper)
  }

  @After fun tearDown() {
    database.close()
  }

  @Test fun `insert can run multiple times`() {
    val insert = database.getConnection().prepareStatement("INSERT INTO test VALUES (?, ?);", INSERT)
    val query = database.getConnection().prepareStatement("SELECT * FROM test", SELECT)

    query.executeQuery().use {
      assertThat(it.next()).isFalse()
    }

    insert.bindLong(1, 1)
    insert.bindString(2, "Alec")
    assertThat(insert.execute()).isEqualTo(1)

    query.executeQuery().use {
      assertThat(it.next()).isTrue()
      assertThat(it.getLong(0)).isEqualTo(1)
      assertThat(it.getString(1)).isEqualTo("Alec")
    }

    insert.bindLong(1, 2)
    insert.bindString(2, "Jake")
    assertThat(insert.execute()).isEqualTo(2)

    query.executeQuery().use {
      assertThat(it.next()).isTrue()
      assertThat(it.getLong(0)).isEqualTo(1)
      assertThat(it.getString(1)).isEqualTo("Alec")
      assertThat(it.next()).isTrue()
      assertThat(it.getLong(0)).isEqualTo(2)
      assertThat(it.getString(1)).isEqualTo("Jake")
    }

    val delete = database.getConnection().prepareStatement("DELETE FROM test", DELETE)
    assertThat(delete.execute()).isEqualTo(2)

    query.executeQuery().use {
      assertThat(it.next()).isFalse()
    }
  }
}