package com.squareup.sqldelight.core.queries

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.core.compiler.SelectQueryGenerator
import com.squareup.sqldelight.test.util.FixtureCompiler
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SelectQueryTypeTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test fun `query type generates properly`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY
      |);
      |
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE id = ?;
      |""".trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val id: kotlin.Long,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(selectForId, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(${query.id}, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE id = ?
      |  ""${'"'}.trimMargin(), 1) {
      |    bindLong(1, id)
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |""".trimMargin())
  }

  @Test fun `bind arguments are ordered in generated type`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE TABLE data (
      |  id INTEGER NOT NULL,
      |  value TEXT NOT NULL
      |);
      |
      |select:
      |SELECT *
      |FROM data
      |WHERE id = ?2
      |AND value = ?1;
      """.trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val value: kotlin.String,
      |  @kotlin.jvm.JvmField
      |  val id: kotlin.Long,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(select, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(${query.id}, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE id = ?
      |  |AND value = ?
      |  ""${'"'}.trimMargin(), 2) {
      |    bindLong(1, id)
      |    bindString(2, value)
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:select"
      |}
      |""".trimMargin())
  }

  @Test fun `array bind argument`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY
      |);
      |
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE id IN ?;
      |""".trimMargin(), tempFolder)

    val generator = SelectQueryGenerator(file.namedQueries.first())

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val id: kotlin.collections.Collection<kotlin.Long>,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(selectForId, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor {
      |    val idIndexes = createArguments(count = id.size, offset = 1)
      |    return driver.executeQuery(null, ""${'"'}
      |    |SELECT *
      |    |FROM data
      |    |WHERE id IN ${"$"}idIndexes
      |    ""${'"'}.trimMargin(), id.size) {
      |      id.forEachIndexed { index, id ->
      |          bindLong(index + 1, id)
      |          }
      |    }
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |""".trimMargin())
  }

  @Test fun `nullable parameter not escaped`() {
    val file = FixtureCompiler.parseSql("""
       |CREATE TABLE socialFeedItem (
       |  message TEXT,
       |  userId TEXT,
       |  creation_time INTEGER
       |);
       |
       |select_news_list:
       |SELECT * FROM socialFeedItem WHERE message IS NOT NULL AND userId = ? ORDER BY datetime(creation_time) DESC;
       |""".trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
       |private inner class Select_news_listQuery<out T : kotlin.Any>(
       |  @kotlin.jvm.JvmField
       |  val userId: kotlin.String?,
       |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
       |) : com.squareup.sqldelight.Query<T>(select_news_list, mapper) {
       |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(null, ""${'"'}SELECT * FROM socialFeedItem WHERE message IS NOT NULL AND userId ${"$"}{ if (userId == null) "IS" else "=" } ? ORDER BY datetime(creation_time) DESC""${'"'}, 1) {
       |    bindString(1, userId)
       |  }
       |
       |  override fun toString(): kotlin.String = "Test.sq:select_news_list"
       |}
       |""".trimMargin())
  }

  @Test fun `nullable parameter has spaces`() {
    val file = FixtureCompiler.parseSql("""
       |CREATE TABLE IF NOT EXISTS Friend(
       |    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
       |    username TEXT NOT NULL UNIQUE,
       |    userId TEXT
       |);
       |
       |selectData:
       |SELECT _id, username
       |FROM Friend
       |WHERE userId=? OR username=? LIMIT 2;
       |""".trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
       |private inner class SelectDataQuery<out T : kotlin.Any>(
       |  @kotlin.jvm.JvmField
       |  val userId: kotlin.String?,
       |  @kotlin.jvm.JvmField
       |  val username: kotlin.String,
       |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
       |) : com.squareup.sqldelight.Query<T>(selectData, mapper) {
       |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(null, ""${'"'}
       |  |SELECT _id, username
       |  |FROM Friend
       |  |WHERE userId${'$'}{ if (userId == null) " IS " else "=" }? OR username=? LIMIT 2
       |  ""${'"'}.trimMargin(), 2) {
       |    bindString(1, userId)
       |    bindString(2, username)
       |  }
       |
       |  override fun toString(): kotlin.String = "Test.sq:selectData"
       |}
       |""".trimMargin())
  }

  @Test fun `nullable bind parameters`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE TABLE data (
      |  id INTEGER PRIMARY KEY,
      |  val TEXT
      |);
      |
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE val = ?
      |AND val == ?
      |AND val <> ?
      |AND val != ?;
      |""".trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val val_: kotlin.String?,
      |  @kotlin.jvm.JvmField
      |  val val__: kotlin.String?,
      |  @kotlin.jvm.JvmField
      |  val val___: kotlin.String?,
      |  @kotlin.jvm.JvmField
      |  val val____: kotlin.String?,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(selectForId, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(null, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE val ${"$"}{ if (val_ == null) "IS" else "=" } ?
      |  |AND val ${"$"}{ if (val__ == null) "IS" else "==" } ?
      |  |AND val ${"$"}{ if (val___ == null) "IS NOT" else "<>" } ?
      |  |AND val ${"$"}{ if (val____ == null) "IS NOT" else "!=" } ?
      |  ""${'"'}.trimMargin(), 4) {
      |    bindString(1, val_)
      |    bindString(2, val__)
      |    bindString(3, val___)
      |    bindString(4, val____)
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |""".trimMargin())
  }

  @Test fun `synthesized column bind arguments`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE VIRTUAL TABLE data USING fts3 (
      |  content TEXT NOT NULL
      |);
      |
      |selectMatching:
      |SELECT *
      |FROM data
      |WHERE data MATCH ? AND rowid = ?;
      |""".trimMargin(), tempFolder)

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectMatchingQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val data: kotlin.String,
      |  @kotlin.jvm.JvmField
      |  val rowid: kotlin.Long,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(selectMatching, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor = driver.executeQuery(${query.id}, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE data MATCH ? AND rowid = ?
      |  ""${'"'}.trimMargin(), 2) {
      |    bindString(1, data)
      |    bindLong(2, rowid)
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:selectMatching"
      |}
      |""".trimMargin())
  }

  @Test fun `array and named bind arguments are compatible`() {
    val file = FixtureCompiler.parseSql("""
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY,
      |  token TEXT NOT NULL,
      |  name TEXT NOT NULL
      |);
      |
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE token = :token
      |  AND id IN ?
      |  AND (token != :token OR (name = :name OR :name IS NULL))
      |  AND token IN ?;
      |""".trimMargin(), tempFolder)

    val generator = SelectQueryGenerator(file.namedQueries.first())

    assertThat(generator.querySubtype().toString()).isEqualTo("""
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  @kotlin.jvm.JvmField
      |  val token: kotlin.String,
      |  @kotlin.jvm.JvmField
      |  val id: kotlin.collections.Collection<kotlin.Long>,
      |  @kotlin.jvm.JvmField
      |  val name: kotlin.String,
      |  @kotlin.jvm.JvmField
      |  val token_: kotlin.collections.Collection<kotlin.String>,
      |  mapper: (com.squareup.sqldelight.db.SqlCursor) -> T
      |) : com.squareup.sqldelight.Query<T>(selectForId, mapper) {
      |  override fun execute(): com.squareup.sqldelight.db.SqlCursor {
      |    val idIndexes = createArguments(count = id.size, offset = 2)
      |    val token_Indexes = createArguments(count = token_.size, offset = id.size + 5)
      |    return driver.executeQuery(null, ""${'"'}
      |    |SELECT *
      |    |FROM data
      |    |WHERE token = ?
      |    |  AND id IN ${"$"}idIndexes
      |    |  AND (token != ? OR (name = ? OR ? IS NULL))
      |    |  AND token IN ${"$"}token_Indexes
      |    ""${'"'}.trimMargin(), 4 + id.size + token_.size) {
      |      bindString(1, token)
      |      id.forEachIndexed { index, id ->
      |          bindLong(index + 2, id)
      |          }
      |      bindString(id.size + 2, token)
      |      bindString(id.size + 3, name)
      |      bindString(id.size + 4, name)
      |      token_.forEachIndexed { index, token_ ->
      |          bindString(index + id.size + 5, token_)
      |          }
      |    }
      |  }
      |
      |  override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |""".trimMargin())
  }
}
