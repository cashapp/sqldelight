package app.cash.sqldelight.mysql.integration.async

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.driver.r2dbc.R2dbcDriver
import com.google.common.truth.Truth.assertThat
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.Test
import org.testcontainers.containers.MySQLContainer

class MySqlTest {
  private fun runTest(block: suspend (MyDatabase) -> Unit) = kotlinx.coroutines.test.runTest {
    MySQLContainer("mysql:8.0").use { mySqlJdbcContainer ->
      mySqlJdbcContainer.start()
      println(mySqlJdbcContainer.jdbcUrl)
      val factory = with(mySqlJdbcContainer) {
        val mariaDBUrl =
          "r2dbc:mariadb://$username:$password@$host:$firstMappedPort/$databaseName?sslMode=TRUST&tinyInt1isBit=false"
        println(mariaDBUrl)
        ConnectionFactories.get(mariaDBUrl)
      }

      val connection = factory.create().awaitSingle()
      val driver = R2dbcDriver(connection)

      val db = MyDatabase(driver).also { MyDatabase.Schema.awaitCreate(driver) }
      block(db)
    }
  }

  @Test fun simpleSelect() = runTest { database ->
    database.dogQueries.insertDog("Tilda", "Pomeranian")
    assertThat(database.dogQueries.selectDogs().awaitAsOne())
      .isEqualTo(
        Dog(
          name = "Tilda",
          breed = "Pomeranian",
          is_good = true,
        ),
      )
  }

  @Test
  fun simpleSelectWithIn() = runTest { database ->
    with(database) {
      dogQueries.insertDog("Tilda", "Pomeranian")
      dogQueries.insertDog("Tucker", "Portuguese Water Dog")
      dogQueries.insertDog("Cujo", "Pomeranian")
      dogQueries.insertDog("Buddy", "Pomeranian")
      assertThat(
        dogQueries.selectDogsByBreedAndNames(
          breed = "Pomeranian",
          name = listOf("Tilda", "Buddy"),
        ).awaitAsList(),
      )
        .containsExactly(
          Dog(
            name = "Tilda",
            breed = "Pomeranian",
            is_good = true,
          ),
          Dog(
            name = "Buddy",
            breed = "Pomeranian",
            is_good = true,
          ),
        )
    }
  }
}
