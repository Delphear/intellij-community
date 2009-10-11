/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.psi.impl.source.tree.java;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.lang.ASTNode;

public class ImportStatementElement extends ImportStatementBaseElement {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.tree.java.ImportStatementElement");

  public ImportStatementElement() {
    super(IMPORT_STATEMENT);
  }


  public ASTNode findChildByRole(int role) {
    final ASTNode result = super.findChildByRole(role);
    if (result != null) return result;
    switch (role) {
      default:
        return null;
      case ChildRole.IMPORT_REFERENCE:
        return findChildByType(JAVA_CODE_REFERENCE);
    }
  }
}
