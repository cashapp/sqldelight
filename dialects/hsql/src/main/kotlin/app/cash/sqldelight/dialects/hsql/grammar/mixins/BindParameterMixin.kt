package app.cash.sqldelight.dialects.hsql.grammar.mixins

import app.cash.sqldelight.dialect.grammar.mixins.BindParameterMixin
import com.intellij.lang.ASTNode

abstract class BindParameterMixin(node: ASTNode) : BindParameterMixin(node) {
  override val replaceWith: String = if (text == "DEFAULT") text else "?"
  override val isDefault = text == "DEFAULT"
}
