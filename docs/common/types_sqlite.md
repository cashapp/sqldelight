## SQLite Types

SQLDelight column definitions are identical to regular SQLite column definitions but support an [extra column constraint](#custom-column-types)
which specifies the Kotlin type of the column in the generated interface.

```sql
CREATE TABLE some_types (
  some_long INTEGER,           -- Stored as INTEGER in db, retrieved as Long
  some_double REAL,            -- Stored as REAL in db, retrieved as Double
  some_string TEXT,            -- Stored as TEXT in db, retrieved as String
  some_blob BLOB,              -- Stored as BLOB in db, retrieved as ByteArray
);
```
