package com.squareup.sqldelight.adapter.primitive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DoubleColumnAdapterTest {
  @Test fun decode() {
    assertThat(DoubleColumnAdapter.decode(10)).isEqualTo(10.0)
  }

  @Test fun encode() {
    assertThat(DoubleColumnAdapter.encode(10.7)).isEqualTo(10)
  }
}
