package com.squareup.sqlite.android

import com.squareup.sqlite.android.model.Column
import com.squareup.sqlite.android.model.ColumnConstraint
import com.squareup.sqlite.android.model.SqlElement
import com.squareup.sqlite.android.model.SqlStmt
import com.squareup.sqlite.android.model.SqlStmt.Replacement
import com.squareup.sqlite.android.model.Table
import java.io.File
import java.util.ArrayList

abstract class TableGenerator<OriginatingType,
    SqliteStatementType : OriginatingType,
    TableType : OriginatingType,
    ColumnType : OriginatingType,
    ConstraintType : OriginatingType>
protected constructor(rootElement: OriginatingType, internal val packageName: String?,
    fileName: String, val outputDirectory: File) : SqlElement<OriginatingType>(rootElement) {
  private val originalFileName: String

  internal val table: Table<OriginatingType>?
  internal val sqliteStatements: MutableList<SqlStmt<OriginatingType>>
  internal val fileDirectory: File
    get() = File(outputDirectory, packageDirectory)

  val generatedFileName: String
    get() = originalFileName + "Model"
  val packageDirectory: String?
    get() = packageName?.replace('.', '/')

  init {
    this.originalFileName =
        if (fileName.endsWith(SqliteCompiler.FILE_EXTENSION)) //
          fileName.substring(0, fileName.length - (SqliteCompiler.FILE_EXTENSION.length + 1))
        else fileName
    this.sqliteStatements = ArrayList<SqlStmt<OriginatingType>>()

    if (packageName == null) {
      table = null
    } else {
      var table: Table<OriginatingType>? = null
      try {
        val tableElement = tableElement(rootElement)
        if (tableElement != null) {
          val replacements = ArrayList<Replacement>()
          table = tableFor(tableElement, packageName, originalFileName, replacements)
          if (table.isKeyValue) {
            sqliteStatements.add(SqlStmt<OriginatingType>("createTable", "" +
                "CREATE TABLE ${tableName(tableElement)} (\n" +
                "  ${SqliteCompiler.KEY_VALUE_KEY_COLUMN} TEXT NOT NULL PRIMARY KEY,\n" +
                "  ${SqliteCompiler.KEY_VALUE_VALUE_COLUMN} BLOB\n" +
                ");", 0, listOf(), tableElement))
          } else {
            sqliteStatements.add(SqlStmt<OriginatingType>("createTable", text(tableElement),
                startOffset(tableElement), replacements, tableElement))
          }
        }

        for (sqlStatementElement in sqlStatementElements(rootElement)) {
          sqliteStatements.add(sqliteStatementFor(sqlStatementElement, arrayListOf()))
        }
      } catch (e: ArrayIndexOutOfBoundsException) {
        // Do nothing, just an easy way to catch a lot of situations where sql is incomplete.
        table = null
      }

      this.table = table
    }
  }

  protected abstract fun sqlStatementElements(
      originatingElement: OriginatingType): Iterable<SqliteStatementType>

  protected abstract fun tableElement(sqlStatementElement: OriginatingType): TableType?
  protected abstract fun identifier(sqlStatementElement: SqliteStatementType): String
  protected abstract fun columnElements(tableElement: TableType): Iterable<ColumnType>
  protected abstract fun tableName(tableElement: TableType): String
  protected abstract fun isKeyValue(tableElement: TableType): Boolean
  protected abstract fun columnName(columnElement: ColumnType): String
  protected abstract fun classLiteral(columnElement: ColumnType): String?
  protected abstract fun typeName(columnElement: ColumnType): String
  protected abstract fun replacementFor(columnElement: ColumnType, type: Column.Type): Replacement
  protected abstract fun constraintElements(columnElement: ColumnType): Iterable<ConstraintType>
  protected abstract fun constraintFor(constraintElement: ConstraintType,
      replacements: List<Replacement>): ColumnConstraint<OriginatingType>?

  protected abstract fun text(sqliteStatementElement: OriginatingType): String
  protected abstract fun startOffset(sqliteStatementElement: OriginatingType): Int

  private fun tableFor(tableElement: TableType, packageName: String,
      fileName: String, replacements: MutableList<Replacement>): Table<OriginatingType> {
    val table = Table<OriginatingType>(packageName, fileName, tableName(tableElement), tableElement,
        isKeyValue(tableElement))
    columnElements(tableElement).forEach { table.columns.add(columnFor(it, replacements)) }
    return table
  }

  private fun columnFor(columnElement: ColumnType,
      replacements: MutableList<Replacement>): Column<OriginatingType> {
    val columnName = columnName(columnElement)
    val type = Column.Type.valueOf(typeName(columnElement))
    val result = when (classLiteral(columnElement)) {
      null -> Column<OriginatingType>(columnName, type, originatingElement = columnElement)
      else -> Column<OriginatingType>(columnName, type, classLiteral(columnElement), columnElement)
    }

    replacements.add(replacementFor(columnElement, type))
    constraintElements(columnElement)
        .map({constraintFor(it, replacements)})
        .filterNotNull()
        .forEach {result.constraints.add(it)}

    return result
  }

  private fun sqliteStatementFor(sqliteStatementElement: SqliteStatementType,
      replacements: List<Replacement>): SqlStmt<OriginatingType> =
    SqlStmt(identifier(sqliteStatementElement), text(sqliteStatementElement),
        startOffset(sqliteStatementElement), replacements, sqliteStatementElement)
}
