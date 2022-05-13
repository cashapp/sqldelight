SQLDelight generates typesafe kotlin APIs from your SQL statements. It verifies your schema, statements, and migrations at compile-time and provides IDE features like autocomplete and refactoring which make writing and maintaining SQL simple.

SQLDelight understands your existing SQL schema.

```sql
CREATE TABLE hockey_player (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  number INTEGER NOT NULL
);
```

It generates typesafe code for any labeled SQL statements.

![intro.gif](images/intro.gif)

---

SQLDelight supports a variety of dialects and platforms:

SQLite

* [Android](android_sqlite)
* [Native (iOS, macOS, or Windows)](native_sqlite)
* [JVM](jvm_sqlite)
* [Javascript](js_sqlite)
* [Multiplatform](multiplatform_sqlite)

[MySQL (JVM)](jvm_mysql)

[PostgreSQL (JVM)](jvm_postgresql) (Experimental)

[HSQL/H2 (JVM)](jvm_h2) (Experimental)

## Snapshots

Snapshots of the development version (including the IDE plugin zip) are available in
[Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/com/squareup/sqldelight/). Note that all coordinates are app.cash.sqldelight instead of com.squareup.sqldelight for 2.0.0+ SNAPSHOTs.
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

// build.gradle.kts
plugins {
    id("app.cash.sqldelight") version "SNAPSHOT-VERSION"
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}
```

[Alpha IDE plugins are also available](https://plugins.jetbrains.com/plugin/8191-sqldelight/versions/alpha) by using the alpha channel in IntelliJ: `https://plugins.jetbrains.com/plugins/alpha/com.squareup.sqldelight`

<img width="738" alt="IntelliJ_alpha_channel" src="https://user-images.githubusercontent.com/22521688/168236653-e32deb26-167f-46ce-9277-ea169cbb22d6.png">
