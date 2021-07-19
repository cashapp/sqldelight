package com.squareup.sqldelight.adapter.primitive

import com.squareup.sqldelight.ColumnAdapter

object DoubleColumnAdapter : ColumnAdapter<Double, Long> {
  override fun decode(databaseValue: Long): Double = databaseValue.toDouble()

  override fun encode(value: Double): Long = value.toLong()
}
