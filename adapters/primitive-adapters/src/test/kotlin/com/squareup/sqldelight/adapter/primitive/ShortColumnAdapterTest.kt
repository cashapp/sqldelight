package com.squareup.sqldelight.adapter.primitive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShortColumnAdapterTest {
  @Test fun decode() {
    assertThat(ShortColumnAdapter.decode(10)).isEqualTo(10)
  }

  @Test fun encode() {
    assertThat(ShortColumnAdapter.encode(10)).isEqualTo(10)
  }
}
