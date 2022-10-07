package app.cash.sqldelight.core.queries.async

import app.cash.sqldelight.core.TestDialect
import app.cash.sqldelight.core.compiler.ExecuteQueryGenerator
import app.cash.sqldelight.core.compiler.SelectQueryGenerator
import app.cash.sqldelight.core.dialects.intType
import app.cash.sqldelight.core.dialects.textType
import app.cash.sqldelight.dialects.postgresql.PostgreSqlDialect
import app.cash.sqldelight.test.util.FixtureCompiler
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AsyncSelectQueryTypeTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test fun `returning clause correctly generates an async query function`() {
    val file = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  val1 TEXT,
      |  val2 TEXT
      |);
      |
      |insertReturning:
      |INSERT INTO data
      |VALUES ('sup', 'dude')
      |RETURNING *;
      |
      """.trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true,
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.customResultTypeFunction().toString()).isEqualTo(
      """
      |public fun <T : kotlin.Any> insertReturning(mapper: (val1: kotlin.String?, val2: kotlin.String?) -> T): app.cash.sqldelight.ExecutableQuery<T> = app.cash.sqldelight.Query(${query.id}, driver, "Test.sq", "insertReturning", ""${'"'}
      ||INSERT INTO data
      ||VALUES ('sup', 'dude')
      ||RETURNING *
      |""${'"'}.trimMargin()) { cursor ->
      |  check(cursor is app.cash.sqldelight.driver.r2dbc.R2dbcCursor)
      |  mapper(
      |    cursor.getString(0),
      |    cursor.getString(1)
      |  )
      |}
      |
      """.trimMargin(),
    )
  }

  @Test fun `returning clause in an update correctly generates an async query function`() {
    val dialect = TestDialect.POSTGRESQL
    val file = FixtureCompiler.parseSql(
      """
      |CREATE TABLE IF NOT EXISTS users(
      |    id ${dialect.intType} PRIMARY KEY,
      |    firstname ${dialect.textType} NOT NULL,
      |    lastname ${dialect.textType} NOT NULL
      |);
      |
      |update:
      |UPDATE users SET
      |    firstname = :firstname,
      |    lastname = :lastname
      |WHERE id = :id
      |RETURNING id, firstname, lastname;
      |
      """.trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true,
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.customResultTypeFunction().toString()).isEqualTo(
      """
      |public fun <T : kotlin.Any> update(
      |  firstname: kotlin.String,
      |  lastname: kotlin.String,
      |  id: kotlin.Int,
      |  mapper: (
      |    id: kotlin.Int,
      |    firstname: kotlin.String,
      |    lastname: kotlin.String,
      |  ) -> T,
      |): app.cash.sqldelight.ExecutableQuery<T> = UpdateQuery(firstname, lastname, id) { cursor ->
      |  check(cursor is app.cash.sqldelight.driver.r2dbc.R2dbcCursor)
      |  mapper(
      |    cursor.getLong(0)!!.toInt(),
      |    cursor.getString(1)!!,
      |    cursor.getString(2)!!
      |  )
      |}
      |
      """.trimMargin(),
    )
  }

  @Test fun `async query type generates properly`() {
    val file = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY
      |);
      |
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE id = ?;
      |
      """.trimMargin(),
      tempFolder,
      generateAsync = true,
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo(
      """
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  public val id: kotlin.Long,
      |  mapper: (app.cash.sqldelight.db.SqlCursor) -> T,
      |) : app.cash.sqldelight.Query<T>(mapper) {
      |  public override fun addListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
      |    driver.addListener(listener, arrayOf("data"))
      |  }
      |
      |  public override fun removeListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
      |    driver.removeListener(listener, arrayOf("data"))
      |  }
      |
      |  public override fun <R> execute(mapper: (app.cash.sqldelight.db.SqlCursor) -> R): app.cash.sqldelight.db.QueryResult<R> = driver.executeQuery(${query.id}, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE id = ?
      |  ""${'"'}.trimMargin(), mapper, 1) {
      |    bindLong(0, id)
      |  }
      |
      |  public override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `grouped statements same parameter`() {
    val file = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  value INTEGER NOT NULL
      |);
      |
      |insertTwice {
      |  INSERT INTO data (value)
      |  VALUES (:value)
      |  ;
      |  INSERT INTO data (value)
      |  VALUES (:value)
      |  ;
      |}
      |
      """.trimMargin(),
      tempFolder,
      generateAsync = true,
    )

    val query = file.namedExecutes.first()
    val generator = ExecuteQueryGenerator(query)

    assertThat(generator.function().toString()).isEqualTo(
      """
      |public suspend fun insertTwice(`value`: kotlin.Long): kotlin.Unit {
      |  transaction {
      |    driver.execute(${query.idForIndex(0)}, ""${'"'}
      |        |INSERT INTO data (value)
      |        |  VALUES (?)
      |        ""${'"'}.trimMargin(), 1) {
      |          bindLong(0, value)
      |        }.await()
      |    driver.execute(${query.idForIndex(1)}, ""${'"'}
      |        |INSERT INTO data (value)
      |        |  VALUES (?)
      |        ""${'"'}.trimMargin(), 1) {
      |          bindLong(0, value)
      |        }.await()
      |  }
      |  notifyQueries(-609468782) { emit ->
      |    emit("data")
      |  }
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `grouped statement with result`() {
    val file = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  value INTEGER NOT NULL
      |);
      |
      |insertAndReturn {
      |  INSERT INTO data (value)
      |  VALUES (?1);
      |
      |  SELECT value
      |  FROM data
      |  WHERE id = last_insert_rowid();
      |}
      |
      """.trimMargin(),
      tempFolder,
      generateAsync = true,
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo(
      """
      |private inner class InsertAndReturnQuery<out T : kotlin.Any>(
      |  public val value_: kotlin.Long,
      |  mapper: (app.cash.sqldelight.db.SqlCursor) -> T,
      |) : app.cash.sqldelight.ExecutableQuery<T>(mapper) {
      |  public override fun <R> execute(mapper: (app.cash.sqldelight.db.SqlCursor) -> R): app.cash.sqldelight.db.QueryResult<R> = app.cash.sqldelight.db.QueryResult.AsyncValue {
      |    transactionWithResult {
      |      driver.execute(${query.idForIndex(0)}, ""${'"'}
      |          |INSERT INTO data (value)
      |          |  VALUES (?)
      |          ""${'"'}.trimMargin(), 1) {
      |            bindLong(0, value_)
      |          }.await()
      |      driver.executeQuery(${query.idForIndex(1)}, ""${'"'}
      |          |SELECT value
      |          |  FROM data
      |          |  WHERE id = last_insert_rowid()
      |          ""${'"'}.trimMargin(), mapper, 0)
      |    }
      |  }
      |
      |  public override fun toString(): kotlin.String = "Test.sq:insertAndReturn"
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `IN in async postgresql queries uses numbered indices`() {
    val result = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  id INTEGER PRIMARY KEY,
      |  value TEXT
      |);
      |
      |selectForMultipleIds:
      |SELECT *
      |FROM data
      |WHERE id IN ?;
      """.trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true,
    )

    val query = result.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo(
      """
        |private inner class SelectForMultipleIdsQuery<out T : kotlin.Any>(
        |  public val id: kotlin.collections.Collection<kotlin.Int>,
        |  mapper: (app.cash.sqldelight.db.SqlCursor) -> T,
        |) : app.cash.sqldelight.Query<T>(mapper) {
        |  public override fun addListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
        |    driver.addListener(listener, arrayOf("data"))
        |  }
        |
        |  public override fun removeListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
        |    driver.removeListener(listener, arrayOf("data"))
        |  }
        |
        |  public override fun <R> execute(mapper: (app.cash.sqldelight.db.SqlCursor) -> R): app.cash.sqldelight.db.QueryResult<R> {
        |    val idIndexes0 = createNumberedArguments(offset = 0, count = id.size)
        |    return driver.executeQuery(null, ""${'"'}
        |        |SELECT *
        |        |FROM data
        |        |WHERE id IN ${"$"}idIndexes0
        |        ""${'"'}.trimMargin(), mapper, id.size) {
        |          check(this is app.cash.sqldelight.driver.r2dbc.R2dbcPreparedStatement)
        |          id.forEachIndexed { index, id_ ->
        |            bindLong(index, id_.toLong())
        |          }
        |        }
        |  }
        |
        |  public override fun toString(): kotlin.String = "Test.sq:selectForMultipleIds"
        |}
        |
      """.trimMargin(),
    )
  }

  @Test
  fun `offset calculation for IN in async postgresql queries is fine`() {
    val result = FixtureCompiler.parseSql(
      """
    |CREATE TABLE data (
    |  id INTEGER PRIMARY KEY,
    |  value TEXT
    |);
    |
    |selectForValueAndMultipleIds:
    |SELECT *
    |FROM data
    |WHERE value = ? AND id IN ?;
      """.trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true,
    )

    val query = result.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo(
      """
      |private inner class SelectForValueAndMultipleIdsQuery<out T : kotlin.Any>(
      |  public val value_: kotlin.String?,
      |  public val id: kotlin.collections.Collection<kotlin.Int>,
      |  mapper: (app.cash.sqldelight.db.SqlCursor) -> T,
      |) : app.cash.sqldelight.Query<T>(mapper) {
      |  public override fun addListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
      |    driver.addListener(listener, arrayOf("data"))
      |  }
      |
      |  public override fun removeListener(listener: app.cash.sqldelight.Query.Listener): kotlin.Unit {
      |    driver.removeListener(listener, arrayOf("data"))
      |  }
      |
      |  public override fun <R> execute(mapper: (app.cash.sqldelight.db.SqlCursor) -> R): app.cash.sqldelight.db.QueryResult<R> {
      |    val idIndexes0 = createNumberedArguments(offset = 1, count = id.size)
      |    return driver.executeQuery(null, ""${'"'}
      |        |SELECT *
      |        |FROM data
      |        |WHERE value ${"$"}{ if (value_ == null) "IS" else "=" } $1 AND id IN ${"$"}idIndexes0
      |        ""${'"'}.trimMargin(), mapper, 1 + id.size) {
      |          check(this is app.cash.sqldelight.driver.r2dbc.R2dbcPreparedStatement)
      |          bindString(0, value_)
      |          id.forEachIndexed { index, id_ ->
      |            bindLong(index + 1, id_.toLong())
      |          }
      |        }
      |  }
      |
      |  public override fun toString(): kotlin.String = "Test.sq:selectForValueAndMultipleIds"
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `offset calculation for IN in async postgresql transactions with same list multiple times is fine`() {
    val result = FixtureCompiler.parseSql(
      """
    |CREATE TABLE data (
    |  id INTEGER PRIMARY KEY,
    |  value TEXT
    |);
    |
    |deleteByValueAndMultipleIdsInTransaction {
    |  DELETE FROM data WHERE value= ? AND id IN :id;
    |  DELETE FROM data WHERE id IN :id;
    |}
      """.trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true,
    )

    val query = result.namedExecutes.first()
    val generator = ExecuteQueryGenerator(query)

    assertThat(generator.function().toString()).isEqualTo(
      """
      |public suspend fun deleteByValueAndMultipleIdsInTransaction(value_: kotlin.String?, id: kotlin.collections.Collection<kotlin.Int>): kotlin.Unit {
      |  transaction {
      |    val idIndexes0 = createNumberedArguments(offset = 1, count = id.size)
      |    driver.execute(null, ""${'"'}DELETE FROM data WHERE value${"$"}{ if (value_ == null) " IS" else "=" } ${'$'}1 AND id IN ${"$"}idIndexes0""${'"'}, 1 + id.size) {
      |          check(this is app.cash.sqldelight.driver.r2dbc.R2dbcPreparedStatement)
      |          bindString(0, value_)
      |          id.forEachIndexed { index, id_ ->
      |            bindLong(index + 1, id_.toLong())
      |          }
      |        }.await()
      |    val idIndexes1 = createNumberedArguments(offset = 0, count = id.size)
      |    driver.execute(null, ""${'"'}DELETE FROM data WHERE id IN ${"$"}idIndexes1""${'"'}, id.size) {
      |          check(this is app.cash.sqldelight.driver.r2dbc.R2dbcPreparedStatement)
      |          id.forEachIndexed { index, id_ ->
      |            bindLong(index, id_.toLong())
      |          }
      |        }.await()
      |  }
      |  notifyQueries(1231275350) { emit ->
      |    emit("data")
      |  }
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `IN in async sqlite transactions does not use numbered indices`() {
    val result = FixtureCompiler.parseSql(
      """
      |CREATE TABLE data (
      |  id INTEGER PRIMARY KEY,
      |  value TEXT
      |);
      |
      |deleteByValueAndMultipleIdsInTransaction {
      |  DELETE FROM data WHERE value= ? AND id IN :id;
      |  DELETE FROM data WHERE id IN :id;
      |}
      """.trimMargin(),
      tempFolder,
      generateAsync = true,
    )

    val query = result.namedExecutes.first()
    val generator = ExecuteQueryGenerator(query)

    assertThat(generator.function().toString()).isEqualTo(
      """
      |public suspend fun deleteByValueAndMultipleIdsInTransaction(value_: kotlin.String?, id: kotlin.collections.Collection<kotlin.Long>): kotlin.Unit {
      |  transaction {
      |    val idIndexes = createArguments(count = id.size)
      |    driver.execute(null, ""${'"'}DELETE FROM data WHERE value${"$"}{ if (value_ == null) " IS" else "=" } ? AND id IN ${"$"}idIndexes""${'"'}, 1 + id.size) {
      |          bindString(0, value_)
      |          id.forEachIndexed { index, id_ ->
      |            bindLong(index + 1, id_)
      |          }
      |        }.await()
      |    driver.execute(null, ""${'"'}DELETE FROM data WHERE id IN ${"$"}idIndexes""${'"'}, id.size) {
      |          id.forEachIndexed { index, id_ ->
      |            bindLong(index, id_)
      |          }
      |        }.await()
      |  }
      |  notifyQueries(1231275350) { emit ->
      |    emit("data")
      |  }
      |}
      |
      """.trimMargin(),
    )
  }
}
