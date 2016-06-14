package com.sample;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import com.squareup.sqldelight.RowMapper;
import java.lang.Override;
import java.lang.String;

public interface TestModel {
  String TABLE_NAME = "test";

  String STUFF = "STUFF";

  String MYSTUFF = "mySTUFF";

  String LOWERCASE_STUFF = "lowercase_stuff";

  String MYOTHERSTUFF = "myOtherStuff";

  String CREATE_TABLE = ""
      + "CREATE TABLE test (\n"
      + "  STUFF TEXT NOT NULL,\n"
      + "  mySTUFF TEXT NOT NULL,\n"
      + "  lowercase_stuff TEXT NOT NULL,\n"
      + "  myOtherStuff TEXT NOT NULL\n"
      + ")";

  String SOME_SELECT = ""
      + "SELECT mySTUFF, myOtherStuff\n"
      + "FROM test";

  @NonNull
  String STUFF();

  @NonNull
  String mySTUFF();

  @NonNull
  String lowercase_stuff();

  @NonNull
  String myOtherStuff();

  interface Some_selectModel {
    String mySTUFF();

    String myOtherStuff();
  }

  interface Some_selectCreator<T extends Some_selectModel> {
    T create(String mySTUFF, String myOtherStuff);
  }

  final class Some_selectMapper<T extends Some_selectModel> implements RowMapper<T> {
    private final Some_selectCreator<T> creator;

    private Some_selectMapper(Some_selectCreator<T> creator) {
      this.creator = creator;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          cursor.getString(0),
          cursor.getString(1)
      );
    }
  }

  interface Creator<T extends TestModel> {
    T create(String STUFF, String mySTUFF, String lowercase_stuff, String myOtherStuff);
  }

  final class Mapper<T extends TestModel> implements RowMapper<T> {
    private final Factory<T> testModelFactory;

    public Mapper(Factory<T> testModelFactory) {
      this.testModelFactory = testModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return testModelFactory.creator.create(
          cursor.getString(0),
          cursor.getString(1),
          cursor.getString(2),
          cursor.getString(3)
      );
    }
  }

  class Marshal<T extends Marshal<T>> {
    protected ContentValues contentValues = new ContentValues();

    public Marshal() {
    }

    public Marshal(TestModel copy) {
      this.STUFF(copy.STUFF());
      this.mySTUFF(copy.mySTUFF());
      this.lowercase_stuff(copy.lowercase_stuff());
      this.myOtherStuff(copy.myOtherStuff());
    }

    public final ContentValues asContentValues() {
      return contentValues;
    }

    public T STUFF(String STUFF_) {
      contentValues.put(STUFF, STUFF_);
      return (T) this;
    }

    public T mySTUFF(String mySTUFF) {
      contentValues.put(MYSTUFF, mySTUFF);
      return (T) this;
    }

    public T lowercase_stuff(String lowercase_stuff) {
      contentValues.put(LOWERCASE_STUFF, lowercase_stuff);
      return (T) this;
    }

    public T myOtherStuff(String myOtherStuff) {
      contentValues.put(MYOTHERSTUFF, myOtherStuff);
      return (T) this;
    }
  }

  final class Factory<T extends TestModel> {
    public final Creator<T> creator;

    public Factory(Creator<T> creator) {
      this.creator = creator;
    }

    public <R extends Some_selectModel> Some_selectMapper<R> some_selectMapper(Some_selectCreator<R> creator) {
      return new Some_selectMapper<R>(creator);
    }
  }
}
