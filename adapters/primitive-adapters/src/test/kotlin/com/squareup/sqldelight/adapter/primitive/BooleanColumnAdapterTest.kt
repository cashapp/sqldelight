package com.squareup.sqldelight.adapter.primitive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BooleanColumnAdapterTest {
  @Test fun decode() {
    assertThat(BooleanColumnAdapter.decode(0L)).isFalse()
    assertThat(BooleanColumnAdapter.decode(1L)).isTrue()
    assertThat(BooleanColumnAdapter.decode(2L)).isFalse()
  }

  @Test fun encode() {
    assertThat(BooleanColumnAdapter.encode(false)).isEqualTo(0)
    assertThat(BooleanColumnAdapter.encode(true)).isEqualTo(1)
  }
}
