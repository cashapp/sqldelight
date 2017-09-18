/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squareup.sqldelight.folding

import com.squareup.sqldelight.SqlDelightFixtureTestCase

class FoldingTests : SqlDelightFixtureTestCase() {
  override val fixtureDirectory = "folding"

  fun testSingleImport() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/SingleImport.sq")
  }

  fun testMultipleImports() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/MultipleImports.sq")
  }

  fun testCreateTable() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/CreateTable.sq")
  }

  fun testStatements() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/Statements.sq")
  }

  fun testAll() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/Player.sq")
  }

  fun testIncompleteImport() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/IncompleteImport.sq")
  }

  fun testIncompleteCreate() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/IncompleteCreate.sq")
  }

  fun testIncompleteStatements() {
    myFixture.testFolding("$testDataPath/$sqldelightDir/IncompleteStatements.sq")
  }
}
