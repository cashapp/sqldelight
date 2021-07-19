package com.squareup.sqldelight.adapter.primitive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IntColumnAdapterTest {
  @Test fun decode() {
    assertThat(IntColumnAdapter.decode(10)).isEqualTo(10)
  }

  @Test fun encode() {
    assertThat(IntColumnAdapter.encode(10)).isEqualTo(10)
  }
}
