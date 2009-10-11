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
package com.intellij.xml.util;

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.IdReferenceProvider;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author spleaner
 */
public class XmlRefCountHolder {
  private static final Key<CachedValue<XmlRefCountHolder>> xmlRefCountHolderKey = Key.create("xml ref count holder");

  private final static UserDataCache<CachedValue<XmlRefCountHolder>, XmlFile, Object> CACHE =
    new UserDataCache<CachedValue<XmlRefCountHolder>, XmlFile, Object>() {
      protected CachedValue<XmlRefCountHolder> compute(final XmlFile file, final Object p) {
        return file.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<XmlRefCountHolder>() {
          public Result<XmlRefCountHolder> compute() {
            final XmlRefCountHolder holder = new XmlRefCountHolder();
            final Language language = file.getViewProvider().getBaseLanguage();
            final PsiFile psiFile = file.getViewProvider().getPsi(language);
            psiFile.accept(new IdGatheringRecursiveVisitor(holder));
            return new Result<XmlRefCountHolder>(holder, file);
          }
        }, false);
      }
    };

  private final Map<String, List<Pair<XmlAttributeValue, Boolean>>> myId2AttributeListMap = new HashMap<String, List<Pair<XmlAttributeValue, Boolean>>>();
  private final Set<XmlAttributeValue> myPossiblyDuplicateIds = new HashSet<XmlAttributeValue>();
  private final List<XmlAttributeValue> myIdReferences = new ArrayList<XmlAttributeValue>();
  private final Set<String> myAdditionallyDeclaredIds = new HashSet<String>();
  private final Set<PsiElement> myDoNotValidateParentsList = new HashSet<PsiElement>();

  public static XmlRefCountHolder getInstance(final XmlFile file) {
    return CACHE.get(xmlRefCountHolderKey, file, null).getValue();
  }

  private XmlRefCountHolder() {
  }


  public boolean isDuplicateIdAttributeValue(@NotNull final XmlAttributeValue value) {
    return myPossiblyDuplicateIds.contains(value);
  }

  public boolean isValidatable(@Nullable final PsiElement element) {
    return !myDoNotValidateParentsList.contains(element);
  }

  public boolean hasIdDeclaration(@NotNull final String idRef) {
    return myId2AttributeListMap.get(idRef) != null || myAdditionallyDeclaredIds.contains(idRef);
  }

  public boolean isIdReferenceValue(@NotNull final XmlAttributeValue value) {
    return myIdReferences.contains(value);
  }

  private void registerId(@NotNull final String id, @NotNull final XmlAttributeValue attributeValue, final boolean soft) {
    List<Pair<XmlAttributeValue, Boolean>> list = myId2AttributeListMap.get(id);
    if (list == null) {
      list = new ArrayList<Pair<XmlAttributeValue, Boolean>>();
      myId2AttributeListMap.put(id, list);
    } else if (!soft) {
      // mark as duplicate
      if (list.size() == 1) {
        if (!list.get(0).second.booleanValue()) {
          myPossiblyDuplicateIds.add(list.get(0).first);
          myPossiblyDuplicateIds.add(attributeValue);
        }
      } else {
        myPossiblyDuplicateIds.add(attributeValue);
      }
    }

    list.add(new Pair<XmlAttributeValue, Boolean>(attributeValue, soft));
  }

  private void registerAdditionalId(@NotNull final String id) {
    myAdditionallyDeclaredIds.add(id);
  }

  private void registerIdReference(@NotNull final XmlAttributeValue value) {
    myIdReferences.add(value);
  }

  private void registerOuterLanguageElement(@NotNull final PsiElement element) {
    PsiElement parent = element.getParent();

    if (parent instanceof XmlText) {
      parent = parent.getParent();
    }

    myDoNotValidateParentsList.add(parent);
  }

  private static class IdGatheringRecursiveVisitor extends XmlRecursiveElementVisitor {
    private final XmlRefCountHolder myHolder;

    private IdGatheringRecursiveVisitor(@NotNull XmlRefCountHolder holder) {
      super(true);
      myHolder = holder;
    }

    @Override
    public void visitElement(final PsiElement element) {
      if (element instanceof OuterLanguageElement) {
        visitOuterLanguageElement(element);
      }

      super.visitElement(element);
    }

    private void visitOuterLanguageElement(@NotNull final PsiElement element) {
      myHolder.registerOuterLanguageElement(element);
    }

    @Override
    public void visitComment(final PsiComment comment) {
      doVisitAnyComment(comment);
      super.visitComment(comment);
    }

    @Override
    public void visitXmlComment(final XmlComment comment) {
      doVisitAnyComment(comment);
      super.visitXmlComment(comment);
    }

    private void doVisitAnyComment(final PsiComment comment) {
      final String id = XmlDeclareIdInCommentAction.getImplicitlyDeclaredId(comment);
      if (id != null) {
        myHolder.registerAdditionalId(id);
      }
    }


    @Override
    public void visitXmlAttributeValue(final XmlAttributeValue value) {
      final PsiElement element = value.getParent();
      if (!(element instanceof XmlAttribute)) return;

      final XmlAttribute attribute = (XmlAttribute)element;

      final XmlTag tag = attribute.getParent();
      if (tag == null) return;

      final XmlElementDescriptor descriptor = tag.getDescriptor();
      if (descriptor == null) return;

      final XmlAttributeDescriptor attributeDescriptor = descriptor.getAttributeDescriptor(attribute);
      if (attributeDescriptor == null) return;

      if (attributeDescriptor.hasIdType()) {
        updateMap(attribute, value, false);
      }
      else {
        final PsiReference[] references = value.getReferences();
        for (PsiReference r : references) {
          if (r instanceof IdReferenceProvider.GlobalAttributeValueSelfReference /*&& !r.isSoft()*/) {
            updateMap(attribute, value, r.isSoft());
          }
        }
      }

      if (attributeDescriptor.hasIdRefType() && PsiTreeUtil.getChildOfType(value, OuterLanguageElement.class) == null) {
        myHolder.registerIdReference(value);
      }

      super.visitXmlAttributeValue(value);
    }

    private void updateMap(@NotNull final XmlAttribute attribute, @NotNull final XmlAttributeValue value, final boolean soft) {
      final String id = XmlHighlightVisitor.getUnquotedValue(value, attribute.getParent());
      if (XmlUtil.isSimpleXmlAttributeValue(id, value) &&
          PsiTreeUtil.getChildOfType(value, OuterLanguageElement.class) == null) {
        myHolder.registerId(id, value, soft);
      }
    }
  }
}
