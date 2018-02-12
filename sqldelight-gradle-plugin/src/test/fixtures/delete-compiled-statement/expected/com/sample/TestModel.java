package com.sample;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightStatement;
import java.lang.Deprecated;
import java.lang.Override;
import java.lang.String;

public interface TestModel {
  @Deprecated
  String TABLE_NAME = "test";

  @Deprecated
  String SOME_COLUMN = "some_column";

  String CREATE_TABLE = ""
      + "CREATE TABLE test (\n"
      + "  some_column TEXT\n"
      + ")";

  @Nullable
  String some_column();

  interface Creator<T extends TestModel> {
    T create(@Nullable String some_column);
  }

  final class Mapper<T extends TestModel> implements RowMapper<T> {
    private final Factory<T> testModelFactory;

    public Mapper(@NonNull Factory<T> testModelFactory) {
      this.testModelFactory = testModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return testModelFactory.creator.create(
          cursor.isNull(0) ? null : cursor.getString(0)
      );
    }
  }

  final class Factory<T extends TestModel> {
    public final Creator<T> creator;

    public Factory(@NonNull Creator<T> creator) {
      this.creator = creator;
    }
  }

  final class Some_delete extends SqlDelightStatement {
    public Some_delete(@NonNull SupportSQLiteDatabase database) {
      super("test", database.compileStatement(""
              + "DELETE FROM test"));
    }
  }
}
