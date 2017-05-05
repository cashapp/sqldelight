package com.test;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.ColumnAdapter;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightStatement;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface UserModel {
  String TABLE_NAME = "users";

  String ID = "id";

  String FIRST_NAME = "first_name";

  String MIDDLE_INITIAL = "middle_initial";

  String LAST_NAME = "last_name";

  String AGE = "age";

  String GENDER = "gender";

  String SOME_GENERIC = "some_generic";

  String SOME_LIST = "some_list";

  String GENDER2 = "gender2";

  String FULL_USER = "full_user";

  String SUCH_LIST = "such_list";

  String CREATE_TABLE = ""
      + "CREATE TABLE users (\n"
      + "  id INTEGER PRIMARY KEY NOT NULL,\n"
      + "  first_name TEXT NOT NULL,\n"
      + "  middle_initial TEXT,\n"
      + "  last_name TEXT NOT NULL,\n"
      + "  age INTEGER NOT NULL DEFAULT 0,\n"
      + "  gender TEXT NOT NULL,\n"
      + "  some_generic BLOB,\n"
      + "  some_list BLOB,\n"
      + "  gender2 TEXT,\n"
      + "  full_user BLOB,\n"
      + "  such_list BLOB\n"
      + ")";

  long id();

  @NonNull
  String first_name();

  @Nullable
  String middle_initial();

  @NonNull
  String last_name();

  int age();

  @NonNull
  User.Gender gender();

  @Nullable
  Map<List<Integer>, Float> some_generic();

  @Nullable
  List<Map<List<List<Integer>>, List<Integer>>> some_list();

  @Nullable
  User.Gender gender2();

  @Nullable
  User full_user();

  @Nullable
  List<List<List<List<String>>>> such_list();

  interface Creator<T extends UserModel> {
    T create(long id, @NonNull String first_name, @Nullable String middle_initial,
        @NonNull String last_name, int age, @NonNull User.Gender gender,
        @Nullable Map<List<Integer>, Float> some_generic,
        @Nullable List<Map<List<List<Integer>>, List<Integer>>> some_list,
        @Nullable User.Gender gender2, @Nullable User full_user,
        @Nullable List<List<List<List<String>>>> such_list);
  }

  final class Mapper<T extends UserModel> implements RowMapper<T> {
    private final Factory<T> userModelFactory;

    public Mapper(Factory<T> userModelFactory) {
      this.userModelFactory = userModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return userModelFactory.creator.create(
          cursor.getLong(0),
          cursor.getString(1),
          cursor.isNull(2) ? null : cursor.getString(2),
          cursor.getString(3),
          cursor.getInt(4),
          userModelFactory.genderAdapter.decode(cursor.getString(5)),
          cursor.isNull(6) ? null : userModelFactory.some_genericAdapter.decode(cursor.getBlob(6)),
          cursor.isNull(7) ? null : userModelFactory.some_listAdapter.decode(cursor.getBlob(7)),
          cursor.isNull(8) ? null : userModelFactory.gender2Adapter.decode(cursor.getString(8)),
          cursor.isNull(9) ? null : userModelFactory.full_userAdapter.decode(cursor.getBlob(9)),
          cursor.isNull(10) ? null : userModelFactory.such_listAdapter.decode(cursor.getBlob(10))
      );
    }
  }

  final class Factory<T extends UserModel> {
    public final Creator<T> creator;

    public final ColumnAdapter<User.Gender, String> genderAdapter;

    public final ColumnAdapter<Map<List<Integer>, Float>, byte[]> some_genericAdapter;

    public final ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>, byte[]> some_listAdapter;

    public final ColumnAdapter<User.Gender, String> gender2Adapter;

    public final ColumnAdapter<User, byte[]> full_userAdapter;

    public final ColumnAdapter<List<List<List<List<String>>>>, byte[]> such_listAdapter;

    public Factory(Creator<T> creator, ColumnAdapter<User.Gender, String> genderAdapter,
        ColumnAdapter<Map<List<Integer>, Float>, byte[]> some_genericAdapter,
        ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>, byte[]> some_listAdapter,
        ColumnAdapter<User.Gender, String> gender2Adapter,
        ColumnAdapter<User, byte[]> full_userAdapter,
        ColumnAdapter<List<List<List<List<String>>>>, byte[]> such_listAdapter) {
      this.creator = creator;
      this.genderAdapter = genderAdapter;
      this.some_genericAdapter = some_genericAdapter;
      this.some_listAdapter = some_listAdapter;
      this.gender2Adapter = gender2Adapter;
      this.full_userAdapter = full_userAdapter;
      this.such_listAdapter = such_listAdapter;
    }

    public SqlDelightStatement females() {
      return new SqlDelightStatement(""
          + "SELECT *\n"
          + "FROM users\n"
          + "WHERE gender = 'FEMALE'",
          new String[0], Collections.<String>singleton("users"));
    }

    public Mapper<T> femalesMapper() {
      return new Mapper<T>(this);
    }
  }
}
