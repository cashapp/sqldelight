package app.cash.sqldelight.core.lang.util

import app.cash.sqldelight.core.compiler.SqlDelightCompiler.allocateName
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.squareup.kotlinpoet.ClassName

internal val SqlCreateTableStmt.interfaceType: ClassName
  get() = ClassName(sqFile().packageName!!, allocateName(tableName).capitalize())
