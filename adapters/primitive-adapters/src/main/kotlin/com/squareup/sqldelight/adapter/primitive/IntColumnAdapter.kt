package com.squareup.sqldelight.adapter.primitive

import com.squareup.sqldelight.ColumnAdapter

object IntColumnAdapter : ColumnAdapter<Int, Long> {
  override fun decode(databaseValue: Long): Int = databaseValue.toInt()

  override fun encode(value: Int): Long = value.toLong()
}
