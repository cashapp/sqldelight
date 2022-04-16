package app.cash.sqldelight.core.compiler.model

import app.cash.sqldelight.dialect.api.QueryWithResults
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryColumn
import com.alecstrong.sql.psi.core.psi.Queryable
import com.alecstrong.sql.psi.core.psi.SqlAnnotatedElement
import com.alecstrong.sql.psi.core.psi.SqlCompoundSelectStmt
import com.alecstrong.sql.psi.core.psi.SqlCteTableName
import com.intellij.psi.util.PsiTreeUtil

class SelectQueryable(
  override val select: SqlCompoundSelectStmt,
  override var statement: SqlAnnotatedElement = select,
) : QueryWithResults {

  /**
   * If this query is a pure select from a table (virtual or otherwise), this returns the LazyQuery
   * which points to that table (Pure meaning it has exactly the same columns in the same order).
   */
  override val pureTable: NamedElement? by lazy {
    fun List<QueryColumn>.flattenCompounded(): List<QueryColumn> {
      return map { column ->
        if (column.compounded.none { it.element != column.element || it.nullable != column.nullable }) {
          column.copy(compounded = emptyList())
        } else {
          column
        }
      }
    }

    val pureColumns = select.queryExposed().singleOrNull()?.columns?.flattenCompounded()

    // First check to see if its just the table we're observing directly.
    val tablesSelected = select.selectStmtList.flatMap {
      it.joinClause?.tableOrSubqueryList?.mapNotNull { tableOrSubquery ->
        val resolvedTable = tableOrSubquery.tableName?.reference?.resolve() ?: return@mapNotNull null
        PsiTreeUtil.getParentOfType(resolvedTable, Queryable::class.java)?.tableExposed()
      }.orEmpty()
    }
    tablesSelected.forEach {
      if (it.query.columns.flattenCompounded() == pureColumns) {
        return@lazy it.tableName
      }
    }

    return@lazy select.tablesAvailable(select).firstOrNull {
      (it.tableName.parent !is SqlCteTableName) &&
        it.query.columns.flattenCompounded() == pureColumns
    }?.tableName
  }
}
