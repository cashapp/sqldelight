package com.squareup.sqldelight.internal

import com.squareup.sqldelight.Query

internal typealias Supplier<T> = () -> T

expect fun copyOnWriteList(): MutableList<Query<*>>

internal expect fun <T> copyOnWriteList(): MutableList<T>

internal expect class QueryLock()

internal expect inline fun <T> QueryLock.withLock(block: () -> T): T
