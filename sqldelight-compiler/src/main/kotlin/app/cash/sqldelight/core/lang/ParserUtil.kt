package app.cash.sqldelight.core.lang

import app.cash.sqldelight.core.SqlDelightProjectService
import app.cash.sqldelight.core.SqldelightParserUtil
import app.cash.sqldelight.core.lang.psi.FunctionExprMixin
import app.cash.sqldelight.dialect.api.SqlDelightDialect
import com.alecstrong.sql.psi.core.SqlParserUtil
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.openapi.project.Project

internal class ParserUtil {
  private var dialect: Class<out SqlDelightDialect>? = null

  fun initializeDialect(project: Project) {
    val newDialect = SqlDelightProjectService.getInstance(project).dialect
    if (newDialect::class.java != dialect) {
      SqlParserUtil.reset()
      SqldelightParserUtil.reset()

      newDialect.setup()
      SqldelightParserUtil.overrideSqlParser()
      dialect = newDialect::class.java

      val currentElementCreation = SqldelightParserUtil.createElement
      SqldelightParserUtil.createElement = {
        when (it.elementType) {
          SqlTypes.FUNCTION_EXPR -> FunctionExprMixin(it)
          else -> currentElementCreation(it)
        }
      }
    }
  }
}
