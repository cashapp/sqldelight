package com.squareup.sqldelight.adapter.primitive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FloatColumnAdapterTest {
  @Test fun decode() {
    assertThat(FloatColumnAdapter.decode(10)).isEqualTo(10.0f)
  }

  @Test fun encode() {
    assertThat(FloatColumnAdapter.encode(10.7f)).isEqualTo(10)
  }
}
