package app.cash.sqldelight.dialects.postgresql.grammar.mixins

import app.cash.sqldelight.dialects.postgresql.grammar.psi.PostgreSqlContainOperatorExpression
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlBinaryExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlExpr
import com.intellij.lang.ASTNode

/**
 * The "@> <@" contain operators is used by TsVector, Jsonb (not Json), TsRange, TsTzRange
 * The type annotation is performed here for both types
 * For other json operators see JsonExpressionMixin
 */
internal abstract class ContainOperatorExpressionMixin(node: ASTNode) :
  SqlCompositeElementImpl(node),
  SqlBinaryExpr,
  PostgreSqlContainOperatorExpression {

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    val columnType = ((firstChild.firstChild.reference?.resolve() as? SqlColumnName)?.parent as? SqlColumnDef)?.columnType?.typeName?.text
    when (columnType) {
      null, "JSONB", "TSVECTOR", "TSRANGE", "TSTZRANGE" -> super.annotate(annotationHolder)
      "JSON" -> annotationHolder.createErrorAnnotation(firstChild.firstChild, "Left side of jsonb expression must be a jsonb column.")
      else -> annotationHolder.createErrorAnnotation(firstChild.firstChild, "expression must be JSONB, TSVECTOR, TSRANGE, TSTZRANGE.")
    }
    super.annotate(annotationHolder)
  }
  override fun getExprList(): List<SqlExpr> {
    return children.filterIsInstance<SqlExpr>()
  }
}
