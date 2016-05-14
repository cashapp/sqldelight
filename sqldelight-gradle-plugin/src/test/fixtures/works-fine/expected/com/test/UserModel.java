package com.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.ColumnAdapter;
import com.squareup.sqldelight.RowMapper;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
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

  String FEMALES = ""
      + "SELECT *\n"
      + "FROM users\n"
      + "WHERE gender = 'FEMALE'";

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

  final class Mapper<T extends UserModel> implements RowMapper<T> {
    private final Creator<T> creator;

    private final ColumnAdapter<User.Gender> genderAdapter;

    private final ColumnAdapter<Map<List<Integer>, Float>> some_genericAdapter;

    private final ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>> some_listAdapter;

    private final ColumnAdapter<User.Gender> gender2Adapter;

    private final ColumnAdapter<User> full_userAdapter;

    private final ColumnAdapter<List<List<List<List<String>>>>> such_listAdapter;

    protected Mapper(Creator<T> creator, ColumnAdapter<User.Gender> genderAdapter, ColumnAdapter<Map<List<Integer>, Float>> some_genericAdapter, ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>> some_listAdapter, ColumnAdapter<User.Gender> gender2Adapter, ColumnAdapter<User> full_userAdapter, ColumnAdapter<List<List<List<List<String>>>>> such_listAdapter) {
      this.creator = creator;
      this.genderAdapter = genderAdapter;
      this.some_genericAdapter = some_genericAdapter;
      this.some_listAdapter = some_listAdapter;
      this.gender2Adapter = gender2Adapter;
      this.full_userAdapter = full_userAdapter;
      this.such_listAdapter = such_listAdapter;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          cursor.getLong(cursor.getColumnIndex(ID)),
          cursor.getString(cursor.getColumnIndex(FIRST_NAME)),
          cursor.isNull(cursor.getColumnIndex(MIDDLE_INITIAL)) ? null : cursor.getString(cursor.getColumnIndex(MIDDLE_INITIAL)),
          cursor.getString(cursor.getColumnIndex(LAST_NAME)),
          cursor.getInt(cursor.getColumnIndex(AGE)),
          genderAdapter.map(cursor, cursor.getColumnIndex(GENDER)),
          cursor.isNull(cursor.getColumnIndex(SOME_GENERIC)) ? null : some_genericAdapter.map(cursor, cursor.getColumnIndex(SOME_GENERIC)),
          cursor.isNull(cursor.getColumnIndex(SOME_LIST)) ? null : some_listAdapter.map(cursor, cursor.getColumnIndex(SOME_LIST)),
          cursor.isNull(cursor.getColumnIndex(GENDER2)) ? null : gender2Adapter.map(cursor, cursor.getColumnIndex(GENDER2)),
          cursor.isNull(cursor.getColumnIndex(FULL_USER)) ? null : full_userAdapter.map(cursor, cursor.getColumnIndex(FULL_USER)),
          cursor.isNull(cursor.getColumnIndex(SUCH_LIST)) ? null : such_listAdapter.map(cursor, cursor.getColumnIndex(SUCH_LIST))
      );
    }

    public interface Creator<R extends UserModel> {
      R create(long id, String first_name, String middle_initial, String last_name, int age, User.Gender gender, Map<List<Integer>, Float> some_generic, List<Map<List<List<Integer>>, List<Integer>>> some_list, User.Gender gender2, User full_user, List<List<List<List<String>>>> such_list);
    }
  }

  class UserMarshal<T extends UserMarshal<T>> {
    protected ContentValues contentValues = new ContentValues();

    private final ColumnAdapter<User.Gender> genderAdapter;

    private final ColumnAdapter<Map<List<Integer>, Float>> some_genericAdapter;

    private final ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>> some_listAdapter;

    private final ColumnAdapter<User.Gender> gender2Adapter;

    private final ColumnAdapter<User> full_userAdapter;

    private final ColumnAdapter<List<List<List<List<String>>>>> such_listAdapter;

    public UserMarshal(ColumnAdapter<User.Gender> genderAdapter, ColumnAdapter<Map<List<Integer>, Float>> some_genericAdapter, ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>> some_listAdapter, ColumnAdapter<User.Gender> gender2Adapter, ColumnAdapter<User> full_userAdapter, ColumnAdapter<List<List<List<List<String>>>>> such_listAdapter) {
      this.genderAdapter = genderAdapter;
      this.some_genericAdapter = some_genericAdapter;
      this.some_listAdapter = some_listAdapter;
      this.gender2Adapter = gender2Adapter;
      this.full_userAdapter = full_userAdapter;
      this.such_listAdapter = such_listAdapter;
    }

    public UserMarshal(UserModel copy, ColumnAdapter<User.Gender> genderAdapter, ColumnAdapter<Map<List<Integer>, Float>> some_genericAdapter, ColumnAdapter<List<Map<List<List<Integer>>, List<Integer>>>> some_listAdapter, ColumnAdapter<User.Gender> gender2Adapter, ColumnAdapter<User> full_userAdapter, ColumnAdapter<List<List<List<List<String>>>>> such_listAdapter) {
      this.id(copy.id());
      this.first_name(copy.first_name());
      this.middle_initial(copy.middle_initial());
      this.last_name(copy.last_name());
      this.age(copy.age());
      this.genderAdapter = genderAdapter;
      this.gender(copy.gender());
      this.some_genericAdapter = some_genericAdapter;
      this.some_generic(copy.some_generic());
      this.some_listAdapter = some_listAdapter;
      this.some_list(copy.some_list());
      this.gender2Adapter = gender2Adapter;
      this.gender2(copy.gender2());
      this.full_userAdapter = full_userAdapter;
      this.full_user(copy.full_user());
      this.such_listAdapter = such_listAdapter;
      this.such_list(copy.such_list());
    }

    public final ContentValues asContentValues() {
      return contentValues;
    }

    public T id(long id) {
      contentValues.put(ID, id);
      return (T) this;
    }

    public T first_name(String first_name) {
      contentValues.put(FIRST_NAME, first_name);
      return (T) this;
    }

    public T middle_initial(String middle_initial) {
      contentValues.put(MIDDLE_INITIAL, middle_initial);
      return (T) this;
    }

    public T last_name(String last_name) {
      contentValues.put(LAST_NAME, last_name);
      return (T) this;
    }

    public T age(int age) {
      contentValues.put(AGE, age);
      return (T) this;
    }

    public T gender(User.Gender gender) {
      genderAdapter.marshal(contentValues, GENDER, gender);
      return (T) this;
    }

    public T some_generic(Map<List<Integer>, Float> some_generic) {
      some_genericAdapter.marshal(contentValues, SOME_GENERIC, some_generic);
      return (T) this;
    }

    public T some_list(List<Map<List<List<Integer>>, List<Integer>>> some_list) {
      some_listAdapter.marshal(contentValues, SOME_LIST, some_list);
      return (T) this;
    }

    public T gender2(User.Gender gender2) {
      gender2Adapter.marshal(contentValues, GENDER2, gender2);
      return (T) this;
    }

    public T full_user(User full_user) {
      full_userAdapter.marshal(contentValues, FULL_USER, full_user);
      return (T) this;
    }

    public T such_list(List<List<List<List<String>>>> such_list) {
      such_listAdapter.marshal(contentValues, SUCH_LIST, such_list);
      return (T) this;
    }
  }
}
