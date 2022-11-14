package app.cash.sqldelight.coroutines

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.coroutines.TestDb.Companion.TABLE_EMPLOYEE
import app.cash.sqldelight.coroutines.TestDb.Companion.TABLE_MANAGER
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.internal.Atomic
import app.cash.sqldelight.internal.getValue
import app.cash.sqldelight.internal.setValue

expect suspend fun testDriver(): SqlDriver

class TestDb(
  val db: SqlDriver,
) : TransacterImpl(db) {
  var aliceId: Long by Atomic<Long>(0)
  var bobId: Long by Atomic<Long>(0)
  var eveId: Long by Atomic<Long>(0)

  init {
    db.execute(null, "PRAGMA foreign_keys=ON", emptyList())

    db.execute(null, CREATE_EMPLOYEE, emptyList())
    aliceId = employee(Employee("alice", "Alice Allison"))
    bobId = employee(Employee("bob", "Bob Bobberson"))
    eveId = employee(Employee("eve", "Eve Evenson"))

    db.execute(null, CREATE_MANAGER, emptyList())
    manager(eveId, aliceId)
  }

  fun <T : Any> createQuery(key: String, query: String, mapper: (SqlCursor) -> T): Query<T> {
    return object : Query<T>(mapper) {
      override fun <R> execute(mapper: (SqlCursor) -> R): QueryResult<R> {
        return db.executeQuery(null, query, mapper, emptyList(), null)
      }

      override fun addListener(listener: Listener) {
        db.addListener(listener, arrayOf(key))
      }

      override fun removeListener(listener: Listener) {
        db.removeListener(listener, arrayOf(key))
      }
    }
  }

  fun notify(key: String) {
    db.notifyListeners(arrayOf(key))
  }

  fun close() {
    db.close()
  }

  fun employee(employee: Employee): Long {
    db.execute(
      0,
      """
      |INSERT OR FAIL INTO $TABLE_EMPLOYEE (${Employee.USERNAME}, ${Employee.NAME})
      |VALUES (?, ?)
      |
      """.trimMargin(),
      listOf(54, 57),
    ) {
      bindString(0, employee.username)
      bindString(1, employee.name)
    }
    notify(TABLE_EMPLOYEE)
    // last_insert_rowid is connection-specific, so run it in the transaction thread/connection
    return transactionWithResult {
      val mapper: (SqlCursor) -> Long = {
        it.next()
        it.getLong(0)!!
      }
      db.executeQuery(2, "SELECT last_insert_rowid()", mapper, emptyList()).value
    }
  }

  fun manager(
    employeeId: Long,
    managerId: Long,
  ): Long {
    db.execute(
      1,
      """
      |INSERT OR FAIL INTO $TABLE_MANAGER (${Manager.EMPLOYEE_ID}, ${Manager.MANAGER_ID})
      |VALUES (?, ?)
      |
      """.trimMargin(),
      listOf(62, 65),
    ) {
      bindLong(0, employeeId)
      bindLong(1, managerId)
    }
    notify(TABLE_MANAGER)
    // last_insert_rowid is connection-specific, so run it in the transaction thread/connection
    return transactionWithResult {
      val mapper: (SqlCursor) -> Long = {
        it.next()
        it.getLong(0)!!
      }
      db.executeQuery(2, "SELECT last_insert_rowid()", mapper, emptyList()).value
    }
  }

  companion object {
    const val TABLE_EMPLOYEE = "employee"
    const val TABLE_MANAGER = "manager"

    val CREATE_EMPLOYEE = """
      |CREATE TABLE $TABLE_EMPLOYEE (
      |  ${Employee.ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  ${Employee.USERNAME} TEXT NOT NULL UNIQUE,
      |  ${Employee.NAME} TEXT NOT NULL
      |)
    """.trimMargin()

    val CREATE_MANAGER = """
      |CREATE TABLE $TABLE_MANAGER (
      |  ${Manager.ID} INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  ${Manager.EMPLOYEE_ID} INTEGER NOT NULL UNIQUE REFERENCES $TABLE_EMPLOYEE(${Employee.ID}),
      |  ${Manager.MANAGER_ID} INTEGER NOT NULL REFERENCES $TABLE_EMPLOYEE(${Employee.ID})
      |)
    """.trimMargin()
  }
}

object Manager {
  const val ID = "id"
  const val EMPLOYEE_ID = "employee_id"
  const val MANAGER_ID = "manager_id"

  val SELECT_MANAGER_LIST = """
    |SELECT e.${Employee.NAME}, m.${Employee.NAME}
    |FROM $TABLE_MANAGER AS manager
    |JOIN $TABLE_EMPLOYEE AS e
    |ON manager.$EMPLOYEE_ID = e.${Employee.ID}
    |JOIN $TABLE_EMPLOYEE AS m
    |ON manager.$MANAGER_ID = m.${Employee.ID}
    |
  """.trimMargin()
}

data class Employee(val username: String, val name: String) {
  companion object {
    const val ID = "id"
    const val USERNAME = "username"
    const val NAME = "name"

    const val SELECT_EMPLOYEES = "SELECT $USERNAME, $NAME FROM $TABLE_EMPLOYEE"

    val MAPPER = { cursor: SqlCursor ->
      Employee(cursor.getString(0)!!, cursor.getString(1)!!)
    }
  }
}
