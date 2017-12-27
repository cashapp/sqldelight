package com.squareup.sqldelight.integration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.squareup.sqldelight.SqlDelightStatement;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public final class IntegrationTests {
  private final Context context = InstrumentationRegistry.getContext();
  private final Configuration configuration = Configuration.builder(context)
      .callback(new DatabaseCallback())
      .build();
  private final SupportSQLiteDatabase database = new FrameworkSQLiteOpenHelperFactory()
      .create(configuration)
      .getWritableDatabase();
  private final Person.Seed SEED_PEOPLE = new Person.Seed(database);
  private final Person.Delete_all DELETE_ALL_PEOPLE = new Person.Delete_all(database);
  private final SqliteKeywords.Seed SEED_SQLITE_KEYWORDS = new SqliteKeywords.Seed(database);
  private final SqliteKeywords.Delete_all DELETE_ALL_SQLITE_KEYWORDS = new SqliteKeywords.Delete_all(database);

  @Before public void before() {
    SEED_PEOPLE.program.execute();
    SEED_SQLITE_KEYWORDS.program.execute();
  }

  @After public void after() {
    DELETE_ALL_PEOPLE.program.execute();
    DELETE_ALL_SQLITE_KEYWORDS.program.execute();
  }

  @Test public void indexedArgs() {
    // ?1 is the only arg
    SqlDelightStatement equivalentNames = Person.FACTORY.equivalent_names("Bob");
    Cursor cursor = database.query(equivalentNames.statement, equivalentNames.args);
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    Person person = Person.FACTORY.equivalent_namesMapper().map(cursor);
    assertThat(person).isEqualTo(new AutoValue_Person(4, "Bob", "Bob"));
  }

  @Test public void startIndexAtTwo() {
    // ?2 is the only arg
    SqlDelightStatement equivalentNames = Person.FACTORY.equivalent_names_2("Bob");
    Cursor cursor = database.query(equivalentNames.statement, equivalentNames.args);
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    Person person = Person.FACTORY.equivalent_names_2Mapper().map(cursor);
    assertThat(person).isEqualTo(new AutoValue_Person(4, "Bob", "Bob"));
  }

  @Test public void namedIndexArgs() {
    // :name is the only arg
    SqlDelightStatement equivalentNames = Person.FACTORY.equivalent_names_named("Bob");
    Cursor cursor = database.query(equivalentNames.statement, equivalentNames.args);
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    Person person = Person.FACTORY.equivalent_names_namedMapper().map(cursor);
    assertThat(person).isEqualTo(new AutoValue_Person(4, "Bob", "Bob"));
  }

  @Test public void indexedArgLast() {
    // First arg declared is ?, second arg declared is ?1.
    SqlDelightStatement indexedArgLast = Person.FACTORY.indexed_arg_last("Bob");
    Cursor cursor = database.query(indexedArgLast.statement, indexedArgLast.args);
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    Person person = Person.FACTORY.indexed_arg_lastMapper().map(cursor);
    assertThat(person).isEqualTo(new AutoValue_Person(4, "Bob", "Bob"));
  }

  @Test public void indexedArgLastTwo() {
    // First arg declared is ?, second arg declared is ?2.
    SqlDelightStatement indexedArgLast = Person.FACTORY.indexed_arg_last_2("Alec", "Strong");
    Cursor cursor = database.query(indexedArgLast.statement, indexedArgLast.args);
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    Person person = Person.FACTORY.equivalent_namesMapper().map(cursor);
    assertThat(person).isEqualTo(new AutoValue_Person(1, "Alec", "Strong"));
  }

  @Test public void nameIn() {
    SqlDelightStatement nameIn = Person.FACTORY.name_in(new String[] { "Alec", "Matt", "Jake" });
    Cursor cursor = database.query(nameIn.statement, nameIn.args);
    assertThat(cursor.getCount()).isEqualTo(3);
  }

  @Test public void sqliteKeywordQuery() {
    Cursor cursor = database.query(SqliteKeywords.FACTORY.select_all());
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    SqliteKeywords sqliteKeywords = SqliteKeywords.FACTORY.select_allMapper().map(cursor);
    assertThat(sqliteKeywords).isEqualTo(new AutoValue_SqliteKeywords(1, 10, 20));
  }

  @Test public void sqliteKeywordColumnString() {
    Cursor cursor = database.query(SqliteKeywords.FACTORY.select_all());
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.moveToFirst();
    long where = cursor.getLong(cursor.getColumnIndexOrThrow(SqliteKeywords.WHERE));
    assertThat(where).isEqualTo(10);
  }

  @Test public void compiledStatement() {
    SqliteKeywords.Insert_stmt statement = new SqliteKeywords.Insert_stmt(database);
    statement.bind(11, 21);
    statement.program.executeInsert();
    statement.bind(12, 22);
    statement.program.executeInsert();

    Cursor cursor = database.query(SqliteKeywords.FACTORY.select_all());
    long current = 10;
    while (cursor.moveToNext()) {
      assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SqliteKeywords.WHERE))).isEqualTo(current++);
    }
  }

  @Test public void compiledStatementAcrossThread() throws InterruptedException {
    SqliteKeywords.Insert_stmt statement = new SqliteKeywords.Insert_stmt(database);
    statement.bind(11, 21);
    statement.program.executeInsert();

    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      @Override public void run() {
        synchronized (statement) {
          statement.bind(12, 22);
          statement.program.executeInsert();
          latch.countDown();
        }
      }
    }).start();

    assertTrue(latch.await(10, SECONDS));

    Cursor cursor = database.query(SqliteKeywords.FACTORY.select_all());
    long current = 10;
    while (cursor.moveToNext()) {
      assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SqliteKeywords.WHERE))).isEqualTo(current++);
    }
  }
}
