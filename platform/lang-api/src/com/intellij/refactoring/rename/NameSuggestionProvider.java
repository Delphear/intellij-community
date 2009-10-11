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

package com.intellij.refactoring.rename;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public interface NameSuggestionProvider {
  ExtensionPointName<NameSuggestionProvider> EP_NAME = ExtensionPointName.create("com.intellij.nameSuggestionProvider");

  @Nullable
  SuggestedNameInfo getSuggestedNames(PsiElement element, PsiElement nameSuggestionContext, List<String> result);

  @Nullable
  Collection<LookupElement> completeName(PsiElement element, final PsiElement nameSuggestionContext, final String prefix);
}
