/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.sqldelight.rx2

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlCursor
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

internal class RecordingObserver : DisposableObserver<Query<*>>() {

  val events: BlockingDeque<Any> = LinkedBlockingDeque()

  override fun onComplete() {
    events.add(COMPLETED)
  }

  override fun onError(e: Throwable) {
    events.add(e)
  }

  override fun onNext(value: Query<*>) {
    events.add(value.execute())
  }

  fun takeEvent(): Any {
    return events.removeFirst() ?: throw AssertionError("No items.")
  }

  fun assertResultSet(): ResultSetAssert {
    val event = takeEvent()
    assertThat(event).isInstanceOf(SqlCursor::class.java)
    return ResultSetAssert(event as SqlCursor)
  }

  fun assertErrorContains(expected: String) {
    val event = takeEvent()
    assertThat(event).isInstanceOf(Throwable::class.java)
    assertThat((event as Throwable).message).contains(expected)
  }

  fun assertIsCompleted() {
    val event = takeEvent()
    assertThat(event).isEqualTo(COMPLETED)
  }

  fun assertNoMoreEvents() {
    assertThat(events).isEmpty()
  }

  internal class ResultSetAssert(private val cursor: SqlCursor) {
    private var row = 0

    fun hasRow(vararg values: Any): ResultSetAssert {
      assertWithMessage("row ${row + 1} exists")
        .that(cursor.next())
        .isTrue()
      row += 1
      for (i in values.indices) {
        assertWithMessage("row $row column '$i'")
          .that(cursor.getString(i))
          .isEqualTo(values[i])
      }
      return this
    }

    fun isExhausted() {
      if (cursor.next()) {
        throw AssertionError("Expected no more rows but was")
      }
      cursor.close()
    }
  }

  companion object {
    private const val COMPLETED = "<completed>"
  }
}
