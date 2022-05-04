package app.cash.sqldelight.core.queries

import app.cash.sqldelight.core.TestDialect
import app.cash.sqldelight.core.compiler.SelectQueryGenerator
import app.cash.sqldelight.core.dialects.intType
import app.cash.sqldelight.core.dialects.textType
import app.cash.sqldelight.dialects.postgresql.PostgreSqlDialect
import app.cash.sqldelight.test.util.FixtureCompiler
import com.google.common.truth.Truth.assertThat
import com.squareup.burst.BurstJUnit4
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(BurstJUnit4::class)
class AsyncSelectQueryTypeTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test fun `returning clause correctly generates an async query function`(dialect: TestDialect) {
    assumeTrue(dialect in listOf(TestDialect.POSTGRESQL, TestDialect.SQLITE_3_35))
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
      |""".trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.customResultTypeFunction().toString()).isEqualTo(
      """
      |public suspend fun <T : kotlin.Any> insertReturning(mapper: (val1: kotlin.String?, val2: kotlin.String?) -> T): app.cash.sqldelight.async.AsyncExecutableQuery<T> = app.cash.sqldelight.async.AsyncQuery(${query.id}, driver, "Test.sq", "insertReturning", ""${'"'}
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
      |""".trimMargin()
    )
  }

  @Test fun `returning clause in an update correctly generates an async query function`(dialect: TestDialect) {
    assumeTrue(dialect in listOf(TestDialect.POSTGRESQL, TestDialect.SQLITE_3_35))
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
      |""".trimMargin(),
      tempFolder,
      dialect = PostgreSqlDialect(),
      generateAsync = true
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.customResultTypeFunction().toString()).isEqualTo(
      """
      |public suspend fun <T : kotlin.Any> update(
      |  firstname: kotlin.String,
      |  lastname: kotlin.String,
      |  id: kotlin.Int,
      |  mapper: (
      |    id: kotlin.Int,
      |    firstname: kotlin.String,
      |    lastname: kotlin.String,
      |  ) -> T,
      |): app.cash.sqldelight.async.AsyncExecutableQuery<T> = UpdateQuery(firstname, lastname, id) { cursor ->
      |  check(cursor is app.cash.sqldelight.driver.r2dbc.R2dbcCursor)
      |  mapper(
      |    cursor.getLong(0)!!.toInt(),
      |    cursor.getString(1)!!,
      |    cursor.getString(2)!!
      |  )
      |}
      |""".trimMargin()
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
      |""".trimMargin(),
      tempFolder,
      generateAsync = true
    )

    val query = file.namedQueries.first()
    val generator = SelectQueryGenerator(query)

    assertThat(generator.querySubtype().toString()).isEqualTo(
      """
      |private inner class SelectForIdQuery<out T : kotlin.Any>(
      |  public val id: kotlin.Long,
      |  mapper: (app.cash.sqldelight.async.db.AsyncSqlCursor) -> T,
      |) : app.cash.sqldelight.async.AsyncQuery<T>(mapper) {
      |  public override fun addListener(listener: app.cash.sqldelight.async.AsyncQuery.Listener): kotlin.Unit {
      |    driver.addListener(listener, arrayOf("data"))
      |  }
      |
      |  public override fun removeListener(listener: app.cash.sqldelight.async.AsyncQuery.Listener): kotlin.Unit {
      |    driver.removeListener(listener, arrayOf("data"))
      |  }
      |
      |  public override suspend fun <R> execute(mapper: (app.cash.sqldelight.async.db.AsyncSqlCursor) -> R): R = driver.executeQuery(${query.id}, ""${'"'}
      |  |SELECT *
      |  |FROM data
      |  |WHERE id = ?
      |  ""${'"'}.trimMargin(), mapper, 1) {
      |    bindLong(1, id)
      |  }
      |
      |  public override fun toString(): kotlin.String = "Test.sq:selectForId"
      |}
      |""".trimMargin()
    )
  }
}
