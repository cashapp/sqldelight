package com.example

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class GroupQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun selectAll(): Query<Long> = Query(165688501, arrayOf("group"), driver, "Group.sq",
      "selectAll", "SELECT `index` FROM `group`") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectFromTable2(mapper: (something: String?, nice: String?) -> T): Query<T>
      = Query(-620576550, arrayOf("myftstable2"), driver, "Group.sq", "selectFromTable2", """
  |SELECT *
  |FROM myftstable2
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0),
      cursor.getString(1)
    )
  }

  public fun selectFromTable2(): Query<Myftstable2> = selectFromTable2 { something, nice ->
    Myftstable2(
      something,
      nice
    )
  }
}
