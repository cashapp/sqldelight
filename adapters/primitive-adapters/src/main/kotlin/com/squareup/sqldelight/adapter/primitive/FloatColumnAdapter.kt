package com.squareup.sqldelight.adapter.primitive

import com.squareup.sqldelight.ColumnAdapter

object FloatColumnAdapter : ColumnAdapter<Float, Long> {
  override fun decode(databaseValue: Long): Float = databaseValue.toFloat()

  override fun encode(value: Float): Long = value.toLong()
}
