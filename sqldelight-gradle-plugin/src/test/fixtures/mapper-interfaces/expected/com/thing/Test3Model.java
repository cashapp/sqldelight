package com.thing;

import android.database.Cursor;
import android.support.annotation.NonNull;
import com.sample.Test1Model;
import com.squareup.sqldelight.RowMapper;
import com.test.Test2Model;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Date;

public interface Test3Model {
  String JOIN_TABLES = ""
      + "SELECT *\n"
      + "FROM test1\n"
      + "JOIN test2";

  String ONE_TABLE = ""
      + "SELECT *\n"
      + "FROM test1";

  String TABLES_AND_VALUE = ""
      + "SELECT test1.*, count(*), table_alias.*\n"
      + "FROM test2 AS table_alias\n"
      + "JOIN test1";

  String CUSTOM_VALUE = ""
      + "SELECT test2.*, test1.*, test1.date\n"
      + "FROM test1\n"
      + "JOIN test2";

  String ALIASED_CUSTOM_VALUE = ""
      + "SELECT test2.*, test1.*, test1.date AS created_date\n"
      + "FROM test1\n"
      + "JOIN test2";

  String ALIASED_TABLES = ""
      + "SELECT sender.*, recipient.*, test2.*\n"
      + "FROM test1 AS sender\n"
      + "JOIN test1 AS recipient\n"
      + "JOIN test2";

  String SINGLE_VALUE = ""
      + "SELECT count(_id)\n"
      + "FROM test1";

  interface Join_tablesModel<T1 extends Test1Model, T3 extends Test2Model> {
    T1 test1();

    T3 test2();
  }

  interface Join_tablesCreator<T1 extends Test1Model, T3 extends Test2Model, T extends Join_tablesModel<T1, T3>> {
    T create(T1 test1, T3 test2);
  }

  final class Join_tablesMapper<T1 extends Test1Model, T3 extends Test2Model, T extends Join_tablesModel<T1, T3>> implements RowMapper<T> {
    private final Join_tablesCreator<T1, T3, T> creator;

    private final Test1Model.Factory<T1> test1ModelFactory;

    private final Test2Model.Factory<T3> test2ModelFactory;

    private Join_tablesMapper(Join_tablesCreator<T1, T3, T> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T3> test2ModelFactory) {
      this.creator = creator;
      this.test1ModelFactory = test1ModelFactory;
      this.test2ModelFactory = test2ModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          test1ModelFactory.creator.create(
              cursor.isNull(0) ? null : cursor.getLong(0),
              cursor.isNull(1) ? null : test1ModelFactory.dateAdapter.map(cursor, 1)
          ),
          test2ModelFactory.creator.create(
              cursor.isNull(2) ? null : cursor.getLong(2)
          )
      );
    }
  }

  interface Tables_and_valueModel<T1 extends Test1Model, T4 extends Test2Model> {
    T1 test1();

    long count();

    T4 table_alias();
  }

  interface Tables_and_valueCreator<T1 extends Test1Model, T4 extends Test2Model, T extends Tables_and_valueModel<T1, T4>> {
    T create(T1 test1, long count, T4 table_alias);
  }

  final class Tables_and_valueMapper<T1 extends Test1Model, T4 extends Test2Model, T extends Tables_and_valueModel<T1, T4>> implements RowMapper<T> {
    private final Tables_and_valueCreator<T1, T4, T> creator;

    private final Test1Model.Factory<T1> test1ModelFactory;

    private final Test2Model.Factory<T4> test2ModelFactory;

    private Tables_and_valueMapper(Tables_and_valueCreator<T1, T4, T> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T4> test2ModelFactory) {
      this.creator = creator;
      this.test1ModelFactory = test1ModelFactory;
      this.test2ModelFactory = test2ModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          test1ModelFactory.creator.create(
              cursor.isNull(0) ? null : cursor.getLong(0),
              cursor.isNull(1) ? null : test1ModelFactory.dateAdapter.map(cursor, 1)
          ),
          cursor.isNull(2) ? null : cursor.getLong(2),
          test2ModelFactory.creator.create(
              cursor.isNull(3) ? null : cursor.getLong(3)
          )
      );
    }
  }

  interface Custom_valueModel<T2 extends Test1Model, T1 extends Test2Model> {
    T1 test2();

    T2 test1();

    Date date();
  }

  interface Custom_valueCreator<T2 extends Test1Model, T1 extends Test2Model, T extends Custom_valueModel<T2, T1>> {
    T create(T1 test2, T2 test1, Date date);
  }

  final class Custom_valueMapper<T2 extends Test1Model, T1 extends Test2Model, T extends Custom_valueModel<T2, T1>> implements RowMapper<T> {
    private final Custom_valueCreator<T2, T1, T> creator;

    private final Test2Model.Factory<T1> test2ModelFactory;

    private final Test1Model.Factory<T2> test1ModelFactory;

    private Custom_valueMapper(Custom_valueCreator<T2, T1, T> creator, Test2Model.Factory<T1> test2ModelFactory, Test1Model.Factory<T2> test1ModelFactory) {
      this.creator = creator;
      this.test2ModelFactory = test2ModelFactory;
      this.test1ModelFactory = test1ModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          test2ModelFactory.creator.create(
              cursor.isNull(0) ? null : cursor.getLong(0)
          ),
          test1ModelFactory.creator.create(
              cursor.isNull(1) ? null : cursor.getLong(1),
              cursor.isNull(2) ? null : test1ModelFactory.dateAdapter.map(cursor, 2)
          ),
          cursor.isNull(3) ? null : test1ModelFactory.dateAdapter.map(cursor, 3)
      );
    }
  }

  interface Aliased_custom_valueModel<T2 extends Test1Model, T1 extends Test2Model> {
    T1 test2();

    T2 test1();

    Date created_date();
  }

  interface Aliased_custom_valueCreator<T2 extends Test1Model, T1 extends Test2Model, T extends Aliased_custom_valueModel<T2, T1>> {
    T create(T1 test2, T2 test1, Date created_date);
  }

  final class Aliased_custom_valueMapper<T2 extends Test1Model, T1 extends Test2Model, T extends Aliased_custom_valueModel<T2, T1>> implements RowMapper<T> {
    private final Aliased_custom_valueCreator<T2, T1, T> creator;

    private final Test2Model.Factory<T1> test2ModelFactory;

    private final Test1Model.Factory<T2> test1ModelFactory;

    private Aliased_custom_valueMapper(Aliased_custom_valueCreator<T2, T1, T> creator, Test2Model.Factory<T1> test2ModelFactory, Test1Model.Factory<T2> test1ModelFactory) {
      this.creator = creator;
      this.test2ModelFactory = test2ModelFactory;
      this.test1ModelFactory = test1ModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          test2ModelFactory.creator.create(
              cursor.isNull(0) ? null : cursor.getLong(0)
          ),
          test1ModelFactory.creator.create(
              cursor.isNull(1) ? null : cursor.getLong(1),
              cursor.isNull(2) ? null : test1ModelFactory.dateAdapter.map(cursor, 2)
          ),
          cursor.isNull(3) ? null : test1ModelFactory.dateAdapter.map(cursor, 3)
      );
    }
  }

  interface Aliased_tablesModel<T1 extends Test1Model, T5 extends Test2Model> {
    T1 sender();

    T1 recipient();

    T5 test2();
  }

  interface Aliased_tablesCreator<T1 extends Test1Model, T5 extends Test2Model, T extends Aliased_tablesModel<T1, T5>> {
    T create(T1 sender, T1 recipient, T5 test2);
  }

  final class Aliased_tablesMapper<T1 extends Test1Model, T5 extends Test2Model, T extends Aliased_tablesModel<T1, T5>> implements RowMapper<T> {
    private final Aliased_tablesCreator<T1, T5, T> creator;

    private final Test1Model.Factory<T1> test1ModelFactory;

    private final Test2Model.Factory<T5> test2ModelFactory;

    private Aliased_tablesMapper(Aliased_tablesCreator<T1, T5, T> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T5> test2ModelFactory) {
      this.creator = creator;
      this.test1ModelFactory = test1ModelFactory;
      this.test2ModelFactory = test2ModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          test1ModelFactory.creator.create(
              cursor.isNull(0) ? null : cursor.getLong(0),
              cursor.isNull(1) ? null : test1ModelFactory.dateAdapter.map(cursor, 1)
          ),
          test1ModelFactory.creator.create(
              cursor.isNull(2) ? null : cursor.getLong(2),
              cursor.isNull(3) ? null : test1ModelFactory.dateAdapter.map(cursor, 3)
          ),
          test2ModelFactory.creator.create(
              cursor.isNull(4) ? null : cursor.getLong(4)
          )
      );
    }
  }

  final class Factory {
    public Factory() {
    }

    public <T1 extends Test1Model, T3 extends Test2Model, R extends Join_tablesModel<T1, T3>> Join_tablesMapper<T1, T3, R> join_tablesMapper(Join_tablesCreator<T1, T3, R> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T3> test2ModelFactory) {
      return new Join_tablesMapper<T1, T3, R>(creator, test1ModelFactory, test2ModelFactory);
    }

    public <T1 extends Test1Model> Test1Model.Mapper<T1> one_tableMapper(Test1Model.Factory<T1> test1ModelFactory) {
      return new Test1Model.Mapper<T1>(test1ModelFactory);
    }

    public <T1 extends Test1Model, T4 extends Test2Model, R extends Tables_and_valueModel<T1, T4>> Tables_and_valueMapper<T1, T4, R> tables_and_valueMapper(Tables_and_valueCreator<T1, T4, R> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T4> test2ModelFactory) {
      return new Tables_and_valueMapper<T1, T4, R>(creator, test1ModelFactory, test2ModelFactory);
    }

    public <T2 extends Test1Model, T1 extends Test2Model, R extends Custom_valueModel<T2, T1>> Custom_valueMapper<T2, T1, R> custom_valueMapper(Custom_valueCreator<T2, T1, R> creator, Test2Model.Factory<T1> test2ModelFactory, Test1Model.Factory<T2> test1ModelFactory) {
      return new Custom_valueMapper<T2, T1, R>(creator, test2ModelFactory, test1ModelFactory);
    }

    public <T2 extends Test1Model, T1 extends Test2Model, R extends Aliased_custom_valueModel<T2, T1>> Aliased_custom_valueMapper<T2, T1, R> aliased_custom_valueMapper(Aliased_custom_valueCreator<T2, T1, R> creator, Test2Model.Factory<T1> test2ModelFactory, Test1Model.Factory<T2> test1ModelFactory) {
      return new Aliased_custom_valueMapper<T2, T1, R>(creator, test2ModelFactory, test1ModelFactory);
    }

    public <T1 extends Test1Model, T5 extends Test2Model, R extends Aliased_tablesModel<T1, T5>> Aliased_tablesMapper<T1, T5, R> aliased_tablesMapper(Aliased_tablesCreator<T1, T5, R> creator, Test1Model.Factory<T1> test1ModelFactory, Test2Model.Factory<T5> test2ModelFactory) {
      return new Aliased_tablesMapper<T1, T5, R>(creator, test1ModelFactory, test2ModelFactory);
    }

    public RowMapper<Long> single_valueMapper() {
      return new RowMapper<Long>() {
        @Override
        public Long map(Cursor cursor) {
          return cursor.isNull(0) ? null : cursor.getLong(0);
        }
      };
    }
  }
}
