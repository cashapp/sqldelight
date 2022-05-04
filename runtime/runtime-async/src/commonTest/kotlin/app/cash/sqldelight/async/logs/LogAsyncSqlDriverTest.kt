package app.cash.sqldelight.async.logs

import app.cash.sqldelight.async.AsyncQuery
import app.cash.sqldelight.async.AsyncTransacter.Transaction
import app.cash.sqldelight.async.AsyncTransacterImpl
import app.cash.sqldelight.async.db.AsyncSqlCursor
import app.cash.sqldelight.async.db.AsyncSqlDriver
import app.cash.sqldelight.async.db.AsyncSqlPreparedStatement
import kotlinx.coroutines.test.runTest
import kotlin.js.JsName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

typealias InsertFunction = suspend (AsyncSqlPreparedStatement.() -> Unit) -> Long

class LogAsyncSqlDriverTest {
  private lateinit var driver: LogAsyncSqlDriver
  private lateinit var transacter: AsyncTransacterImpl
  private val logs = LinkedList<String>()

  @BeforeTest
  fun setup() {
    driver = LogAsyncSqlDriver(FakeSqlDriver()) { log ->
      logs.add(log)
    }
    transacter = object : AsyncTransacterImpl(driver) {}
  }

  @AfterTest
  fun tearDown() {
    driver.close()
    logs.clear()
  }

  @JsName("insertLogsCorrect")
  @Test
  fun `insert logs are correct`() = runTest {
    val insert: InsertFunction = { binders: AsyncSqlPreparedStatement.() -> Unit ->
      driver.execute(2, "INSERT INTO test VALUES (?, ?);", 2, binders)
    }

    insert {
      bindLong(1, 1)
      bindString(2, "Alec")
    }

    insert {}

    assertEquals("EXECUTE\n INSERT INTO test VALUES (?, ?);", logs[0])
    assertEquals(" [1, Alec]", logs[1])
    assertEquals("EXECUTE\n INSERT INTO test VALUES (?, ?);", logs[2])
  }

  @JsName("queryLogsCorrect")
  @Test
  fun `query logs are correct`() = runTest {
    val query: suspend () -> Unit = {
      driver.executeQuery(3, "SELECT * FROM test", {}, 0)
    }

    query()

    assertEquals("QUERY\n SELECT * FROM test", logs[0])
  }

  @JsName("transactionLogsCorrect")
  @Test
  fun `transaction logs are correct`() = runTest {
    transacter.transaction {}
    transacter.transaction { rollback() }
    transacter.transaction {
      val insert: InsertFunction = { binders: AsyncSqlPreparedStatement.() -> Unit ->
        driver.execute(2, "INSERT INTO test VALUES (?, ?);", 2, binders)
      }

      insert {
        bindLong(1, 1)
        bindString(2, "Alec")
      }
    }

    assertEquals("TRANSACTION BEGIN", logs[0])
    assertEquals("TRANSACTION COMMIT", logs[1])
    assertEquals("TRANSACTION BEGIN", logs[2])
    assertEquals("TRANSACTION ROLLBACK", logs[3])
    assertEquals("TRANSACTION BEGIN", logs[4])
    assertEquals("EXECUTE\n INSERT INTO test VALUES (?, ?);", logs[5])
    assertEquals(" [1, Alec]", logs[6])
    assertEquals("TRANSACTION COMMIT", logs[7])
  }
}

class FakeSqlDriver : AsyncSqlDriver {
  override suspend fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: (AsyncSqlCursor) -> R,
    parameters: Int,
    binders: (AsyncSqlPreparedStatement.() -> Unit)?
  ): R {
    return mapper(FakeSqlCursor())
  }

  override suspend fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (AsyncSqlPreparedStatement.() -> Unit)?
  ): Long {
    return 0
  }

  override suspend fun newTransaction(): Transaction {
    return FakeTransaction()
  }

  override fun currentTransaction(): Transaction? {
    return null
  }

  override fun addListener(listener: AsyncQuery.Listener, queryKeys: Array<String>) {
  }

  override fun removeListener(listener: AsyncQuery.Listener, queryKeys: Array<String>) {
  }

  override fun notifyListeners(queryKeys: Array<String>) {
  }

  override fun close() {
  }
}

class FakeSqlCursor : AsyncSqlCursor {
  override fun next(): Boolean {
    return false
  }

  override fun getString(index: Int): String? {
    return null
  }

  override fun getLong(index: Int): Long? {
    return null
  }

  override fun getBytes(index: Int): ByteArray? {
    return null
  }

  override fun getDouble(index: Int): Double? {
    return null
  }

  override fun getBoolean(index: Int): Boolean? {
    return null
  }
}

class FakeTransaction : Transaction() {
  override val enclosingTransaction: Transaction? = null

  override suspend fun endTransaction(successful: Boolean) {
  }
}
