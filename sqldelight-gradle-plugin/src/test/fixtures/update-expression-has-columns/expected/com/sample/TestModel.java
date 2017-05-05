package com.sample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightCompiledStatement;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;

public interface TestModel {
  String TABLE_NAME = "test";

  String _ID = "_id";

  String ID_LESS_THAN_FOUR = "id_less_than_four";

  String CREATE_TABLE = ""
      + "CREATE TABLE test (\n"
      + "  _id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
      + "  id_less_than_four INTEGER DEFAULT 0\n"
      + ")";

  String SOME_UPDATE = ""
      + "UPDATE test\n"
      + "SET id_less_than_four = _id IN (1, 2, 3)";

  @Nullable
  Long _id();

  @Nullable
  Long id_less_than_four();

  interface Creator<T extends TestModel> {
    T create(@Nullable Long _id, @Nullable Long id_less_than_four);
  }

  final class Mapper<T extends TestModel> implements RowMapper<T> {
    private final Factory<T> testModelFactory;

    public Mapper(Factory<T> testModelFactory) {
      this.testModelFactory = testModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return testModelFactory.creator.create(
          cursor.isNull(0) ? null : cursor.getLong(0),
          cursor.isNull(1) ? null : cursor.getLong(1)
      );
    }
  }

  final class Factory<T extends TestModel> {
    public final Creator<T> creator;

    public Factory(Creator<T> creator) {
      this.creator = creator;
    }
  }

  final class Some_update extends SqlDelightCompiledStatement.Update {
    public Some_update(SQLiteDatabase database) {
      super("test", database.compileStatement(""
              + "UPDATE test\n"
              + "SET id_less_than_four = _id IN (1, 2, 3)"));
    }
  }
}
