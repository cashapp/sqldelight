package com.sample;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.ColumnAdapter;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightQuery;
import com.squareup.sqldelight.internal.TableSet;
import java.lang.Deprecated;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Calendar;

public interface TeamModel {
  @Deprecated
  String TABLE_NAME = "team";

  @Deprecated
  String _ID = "_id";

  @Deprecated
  String NAME = "name";

  @Deprecated
  String FOUNDED = "founded";

  @Deprecated
  String COACH = "coach";

  @Deprecated
  String CAPTAIN = "captain";

  @Deprecated
  String WON_CUP = "won_cup";

  String CREATE_TABLE = ""
      + "CREATE TABLE team (\n"
      + "  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
      + "  name TEXT NOT NULL UNIQUE,\n"
      + "  founded TEXT NOT NULL,\n"
      + "  coach TEXT NOT NULL,\n"
      + "  captain INTEGER,\n"
      + "  won_cup INTEGER NOT NULL DEFAULT 0,\n"
      + "  FOREIGN KEY(captain) REFERENCES hockey_player(_id)\n"
      + ")";

  long _id();

  @NonNull
  String name();

  @NonNull
  Calendar founded();

  @NonNull
  String coach();

  @Nullable
  Long captain();

  boolean won_cup();

  interface Creator<T extends TeamModel> {
    T create(long _id, @NonNull String name, @NonNull Calendar founded, @NonNull String coach,
        @Nullable Long captain, boolean won_cup);
  }

  final class Mapper<T extends TeamModel> implements RowMapper<T> {
    private final Factory<T> teamModelFactory;

    public Mapper(@NonNull Factory<T> teamModelFactory) {
      this.teamModelFactory = teamModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return teamModelFactory.creator.create(
          cursor.getLong(0),
          cursor.getString(1),
          teamModelFactory.foundedAdapter.decode(cursor.getString(2)),
          cursor.getString(3),
          cursor.isNull(4) ? null : cursor.getLong(4),
          cursor.getInt(5) == 1
      );
    }
  }

  final class Factory<T extends TeamModel> {
    public final Creator<T> creator;

    public final ColumnAdapter<Calendar, String> foundedAdapter;

    public Factory(@NonNull Creator<T> creator,
        @NonNull ColumnAdapter<Calendar, String> foundedAdapter) {
      this.creator = creator;
      this.foundedAdapter = foundedAdapter;
    }

    @NonNull
    public SqlDelightQuery select_all() {
      return new SqlDelightQuery(""
          + "SELECT *\n"
          + "FROM team",
          new TableSet("team"));
    }

    @NonNull
    public Mapper<T> select_allMapper() {
      return new Mapper<T>(this);
    }
  }
}
