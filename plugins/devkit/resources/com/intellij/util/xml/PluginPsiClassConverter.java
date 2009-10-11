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
package com.intellij.util.xml;

import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author peter
*/
public class PluginPsiClassConverter extends PsiClassConverter {
  protected GlobalSearchScope getScope(final GenericDomValue domValue) {
    return GlobalSearchScope.allScope(domValue.getManager().getProject());
  }

  @Override
  protected JavaClassReferenceProvider createClassReferenceProvider(GenericDomValue<PsiClass> genericDomValue,
                                                                    ConvertContext context,
                                                                    ExtendClass extendClass) {
    final JavaClassReferenceProvider provider = super.createClassReferenceProvider(genericDomValue, context, extendClass);
    provider.setOption(JavaClassReferenceProvider.JVM_FORMAT, Boolean.TRUE);
    provider.setOption(JavaClassReferenceProvider.ALLOW_DOLLAR_NAMES, Boolean.TRUE);
    return provider;
  }
}
