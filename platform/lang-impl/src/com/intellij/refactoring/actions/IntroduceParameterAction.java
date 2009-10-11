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

/*
 * Created by IntelliJ IDEA.
 * User: dsl
 * Date: 06.05.2002
 * Time: 14:03:43
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.refactoring.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageRefactoringSupport;

public class IntroduceParameterAction extends BaseRefactoringAction {
  protected boolean isAvailableInEditorOnly() {
    return true;
  }

  protected boolean isEnabledOnElements(PsiElement[] elements) {
    return false;
  }

  protected RefactoringActionHandler getHandler(DataContext dataContext) {
    final Language language = LangDataKeys.LANGUAGE.getData(dataContext);
    if (language != null) {
      return LanguageRefactoringSupport.INSTANCE.forLanguage(language).getIntroduceParameterHandler();
    }

    return null;
  }

  protected boolean isAvailableForLanguage(Language language) {
    return LanguageRefactoringSupport.INSTANCE.forLanguage(language).getIntroduceParameterHandler() != null;
  }
}
