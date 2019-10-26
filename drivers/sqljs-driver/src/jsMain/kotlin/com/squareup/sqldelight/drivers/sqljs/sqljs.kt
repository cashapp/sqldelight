package com.squareup.sqldelight.drivers.sqljs

import org.khronos.webgl.Uint8Array
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

operator fun InitStatementJsStatic.invoke(): Statement = createInstance(this)
operator fun InitDatabaseJsStatic.invoke(): Database = createInstance(this)
operator fun InitDatabaseJsStatic.invoke(data: Array<Number>): Database = createInstance(this, data)
operator fun InitDatabaseJsStatic.invoke(data: Uint8Array): Database = createInstance(this, data)
operator fun InitSqlJsStatic.invoke(): Promise<SqlJsStatic> = asDynamic()()
operator fun InitSqlJsStatic.invoke(config: Config?): Promise<SqlJsStatic> = asDynamic()(config)

@JsModule("sql.js")
external val initSqlJs: InitSqlJsStatic

@Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
fun createInstance(type: dynamic, vararg args: dynamic): dynamic {
    val argsArray = (listOf(null) + args).toTypedArray()
    return js("new (Function.prototype.bind.apply(type, argsArray))")
}

suspend fun initSql(config: Config? = js("{}")): SqlJsStatic = initSqlJs(config).await()

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}
