package com.sample;

import android.database.Cursor;
import android.support.annotation.NonNull;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightStatement;
import java.lang.Double;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Collections;

public interface TestModel {
  String TABLE_NAME = "some_table";

  String _ID = "_id";

  String QUANTITY = "quantity";

  String SOME_REAL = "some_real";

  String CREATE_TABLE = ""
      + "CREATE TABLE some_table (\n"
      + "    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
      + "    quantity INTEGER NOT NULL DEFAULT 0,\n"
      + "    some_real REAL NOT NULL DEFAULT 1.0\n"
      + ")";

  long _id();

  int quantity();

  double some_real();

  interface Creator<T extends TestModel> {
    T create(long _id, int quantity, double some_real);
  }

  final class Mapper<T extends TestModel> implements RowMapper<T> {
    private final Factory<T> testModelFactory;

    public Mapper(Factory<T> testModelFactory) {
      this.testModelFactory = testModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return testModelFactory.creator.create(
          cursor.getLong(0),
          cursor.getInt(1),
          cursor.getDouble(2)
      );
    }
  }

  final class Factory<T extends TestModel> {
    public final Creator<T> creator;

    public Factory(Creator<T> creator) {
      this.creator = creator;
    }

    public SqlDelightStatement get_sum() {
      return new SqlDelightStatement(""
          + "SELECT sum(quantity)\n"
          + "FROM some_table",
          new String[0], Collections.<String>singleton("some_table"));
    }

    public SqlDelightStatement get_rounded() {
      return new SqlDelightStatement(""
          + "SELECT round(some_real)\n"
          + "FROM some_table",
          new String[0], Collections.<String>singleton("some_table"));
    }

    public SqlDelightStatement get_rounded_arg() {
      return new SqlDelightStatement(""
          + "SELECT round(some_real, 1)\n"
          + "FROM some_table",
          new String[0], Collections.<String>singleton("some_table"));
    }

    public RowMapper<Long> get_sumMapper() {
      return new RowMapper<Long>() {
        @Override
        public Long map(Cursor cursor) {
          return cursor.getLong(0);
        }
      };
    }

    public RowMapper<Long> get_roundedMapper() {
      return new RowMapper<Long>() {
        @Override
        public Long map(Cursor cursor) {
          return cursor.getLong(0);
        }
      };
    }

    public RowMapper<Double> get_rounded_argMapper() {
      return new RowMapper<Double>() {
        @Override
        public Double map(Cursor cursor) {
          return cursor.getDouble(0);
        }
      };
    }
  }
}
