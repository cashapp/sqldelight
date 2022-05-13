package app.cash.sqldelight.async

import app.cash.sqldelight.async.SqlDriverAdapter.Companion.asAsyncSqlDriver
import app.cash.sqldelight.async.db.AsyncSqlDriver
import app.cash.sqldelight.async.db.AsyncSqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

class TransacterAdapterTest {
  private val schema = object : AsyncSqlSchema {
    override val version = 1
    override suspend fun create(driver: AsyncSqlDriver) {}
    override suspend fun migrate(
            driver: AsyncSqlDriver,
            oldVersion: Int,
            newVersion: Int
    ) {
    }
  }

  private fun runTest(block: suspend (AsyncSqlDriver, AsyncTransacter) -> Unit) = kotlinx.coroutines.test.runTest {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    val asyncDriver = driver.asAsyncSqlDriver().also { schema.create(it) }
    val transacter = object : AsyncTransacterImpl(asyncDriver) {}
    block(asyncDriver, transacter)

    asyncDriver.close()
  }

  @Test fun afterCommit_runs_after_transaction_commits() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
      assertEquals(0, counter)
    }

    assertEquals(1, counter)
  }

  @Test fun afterCommit_does_not_run_after_transaction_rollbacks() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
      assertEquals(0, counter)
      rollback()
    }

    assertEquals(0, counter)
  }

  @Test fun afterCommit_runs_after_enclosing_transaction_commits() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
      assertEquals(0, counter)

      transaction {
        afterCommit { counter++ }
        assertEquals(0, counter)
      }

      assertEquals(0, counter)
    }

    assertEquals(2, counter)
  }

  @Test fun afterCommit_does_not_run_in_nested_transaction_when_enclosing_rolls_back() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
      assertEquals(0, counter)

      transaction {
        afterCommit { counter++ }
      }

      rollback()
    }

    assertEquals(0, counter)
  }

  @Test fun afterCommit_does_not_run_in_nested_transaction_when_nested_rolls_back() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
      assertEquals(0, counter)

      transaction {
        afterCommit { counter++ }
        rollback()
      }

      throw AssertionError()
    }

    assertEquals(0, counter)
  }

  @Test fun afterRollback_no_ops_if_the_transaction_never_rolls_back() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterRollback { counter++ }
    }

    assertEquals(0, counter)
  }

  @Test fun afterRollback_runs_after_a_rollback_occurs() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterRollback { counter++ }
      rollback()
    }

    assertEquals(1, counter)
  }

  @Test fun afterRollback_runs_after_an_inner_transaction_rolls_back() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterRollback { counter++ }
      transaction {
        rollback()
      }
      throw AssertionError()
    }

    assertEquals(1, counter)
  }

  @Test fun afterRollback_runs_in_an_inner_transaction_when_the_outer_transaction_rolls_back() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      transaction {
        afterRollback { counter++ }
      }
      rollback()
    }

    assertEquals(1, counter)
  }

  @Test fun transactions_close_themselves_out_properly() = runTest { _, transacter ->
    var counter = 0
    transacter.transaction {
      afterCommit { counter++ }
    }

    transacter.transaction {
      afterCommit { counter++ }
    }

    assertEquals(2, counter)
  }

  @Test fun setting_no_enclosing_fails_if_there_is_a_currently_running_transaction() = runTest { _, transacter ->
    transacter.transaction(noEnclosing = true) {
      assertFailsWith<IllegalStateException> {
        transacter.transaction(noEnclosing = true) {
          throw AssertionError()
        }
      }
    }
  }

  @Test fun an_exception_thrown_in_postRollback_function_is_combined_with_the_exception_in_the_main_body() = runTest { _, transacter ->
    class ExceptionA : RuntimeException()
    class ExceptionB : RuntimeException()
    try {
      transacter.transaction {
        afterRollback {
          throw ExceptionA()
        }
        throw ExceptionB()
      }
      fail("Should have thrown!")
    } catch (e: Throwable) {
      assertTrue("Exception thrown in body not in message($e)") { e.toString().contains("ExceptionA") }
      assertTrue("Exception thrown in rollback not in message($e)") { e.toString().contains("ExceptionB") }
    }
  }
}
