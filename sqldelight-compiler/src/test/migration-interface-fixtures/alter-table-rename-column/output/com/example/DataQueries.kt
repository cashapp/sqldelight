package com.example

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
import kotlin.Unit

public class DataQueries(
  private val driver: JdbcDriver,
  private val testAdapter: Test.Adapter,
) : TransacterImpl(driver) {
  public fun insertWhole(test: Test): Unit {
    driver.execute(-2118611703, """
        |INSERT INTO test
        |VALUES (?, ?)
        """.trimMargin(), 2) {
          check(this is JdbcPreparedStatement)
          bindString(0, test.third)
          bindString(1, test.second?.let { testAdapter.secondAdapter.encode(it) })
        }
    notifyQueries(-2118611703) { emit ->
      emit("test")
    }
  }
}
