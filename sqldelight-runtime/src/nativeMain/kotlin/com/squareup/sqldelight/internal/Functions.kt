package com.squareup.sqldelight.internal

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import co.touchlab.stately.collections.frozenCopyOnWriteList
import com.squareup.sqldelight.Query

actual fun copyOnWriteList(): MutableList<Query<*>> {
  return frozenCopyOnWriteList()
}

internal actual class QueryLock {
  internal val lock = Lock()
}

internal actual inline fun <T> QueryLock.withLock(block: () -> T) = lock.withLock(block)
