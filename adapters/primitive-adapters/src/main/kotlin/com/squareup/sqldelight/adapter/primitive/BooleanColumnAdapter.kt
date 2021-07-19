package com.squareup.sqldelight.adapter.primitive

import com.squareup.sqldelight.ColumnAdapter

object BooleanColumnAdapter : ColumnAdapter<Boolean, Long> {
  override fun decode(databaseValue: Long): Boolean = databaseValue == 1L

  override fun encode(value: Boolean): Long = if (value) 1L else 0L
}
