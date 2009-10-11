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
 * User: anna
 * Date: 22-Jun-2009
 */
package com.intellij.refactoring.extractMethod;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.controlFlow.ControlFlow;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.ParameterTablePanel;
import com.intellij.refactoring.util.duplicates.DuplicatesFinder;
import com.intellij.util.ArrayUtil;

import java.util.*;

public class InputVariables {
  private final List<ParameterTablePanel.VariableData> myInputVariables;

  private List<? extends PsiVariable> myInitialParameters;
  private final Project myProject;
  private final LocalSearchScope myScope;

  private ParametersFolder myFolding;
  private boolean myFoldingAvailable;

  public InputVariables(final List<? extends PsiVariable> inputVariables,
                        Project project,
                        LocalSearchScope scope,
                        boolean foldingAvailable) {
    myInitialParameters = inputVariables;
    myProject = project;
    myScope = scope;
    myFoldingAvailable = foldingAvailable;
    myFolding = new ParametersFolder();
    myInputVariables = wrapInputVariables(inputVariables);
  }

  /**
   * copy use only
   */
  public InputVariables(List<ParameterTablePanel.VariableData> inputVariables,
                        Project project,
                        LocalSearchScope scope) {
    myProject = project;
    myScope = scope;
    myInputVariables = new ArrayList<ParameterTablePanel.VariableData>(inputVariables);
  }

  public boolean isFoldable() {
    return myFolding.isFoldable();
  }

  public ArrayList<ParameterTablePanel.VariableData> wrapInputVariables(final List<? extends PsiVariable> inputVariables) {
    final ArrayList<ParameterTablePanel.VariableData> inputData = new ArrayList<ParameterTablePanel.VariableData>(inputVariables.size());
    for (PsiVariable var : inputVariables) {
      String name = var.getName();
      if (!(var instanceof PsiParameter)) {
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(myProject);
        VariableKind kind = codeStyleManager.getVariableKind(var);
        name = codeStyleManager.variableNameToPropertyName(name, kind);
        name = codeStyleManager.propertyNameToVariableName(name, VariableKind.PARAMETER);
      }
      PsiType type = var.getType();
      if (type instanceof PsiEllipsisType) {
        type = ((PsiEllipsisType)type).toArrayType();
      }

      ParameterTablePanel.VariableData data = new ParameterTablePanel.VariableData(var, type);
      data.name = name;
      data.passAsParameter = true;
      inputData.add(data);

      if (myFoldingAvailable) myFolding.isParameterFoldable(data, myScope, inputVariables);
    }


    if (myFoldingAvailable) {
      final Set<ParameterTablePanel.VariableData> toDelete = new HashSet<ParameterTablePanel.VariableData>();
      for (int i = inputData.size() - 1; i >=0; i--) {
        final ParameterTablePanel.VariableData data = inputData.get(i);
        if (myFolding.isParameterSafeToDelete(data, myScope)) {
          toDelete.add(data);
        }
      }
      inputData.removeAll(toDelete);
    }


    return inputData;
  }

  public List<ParameterTablePanel.VariableData> getInputVariables() {
    return myInputVariables;
  }

  public PsiExpression replaceWrappedReferences(PsiElement[] elements, PsiExpression expression) {
    if (!myFoldingAvailable) return expression;

    boolean update = elements[0] == expression;
    for (ParameterTablePanel.VariableData inputVariable : myInputVariables) {
      myFolding.foldParameterUsagesInBody(inputVariable, elements, myScope);
    }
    return update ? (PsiExpression)elements[0] : expression;
  }

  public boolean toDeclareInsideBody(PsiVariable variable) {
    final ArrayList<ParameterTablePanel.VariableData> knownVars = new ArrayList<ParameterTablePanel.VariableData>(myInputVariables);
    for (ParameterTablePanel.VariableData data : knownVars) {
      if (data.variable.equals(variable)) {
        return false;
      }
    }
    return !myFolding.wasExcluded(variable);
  }

  public boolean contains(PsiVariable variable) {
    for (ParameterTablePanel.VariableData data : myInputVariables) {
      if (data.variable.equals(variable)) return true;
    }
    return false;
  }

  public void removeParametersUsedInExitsOnly(PsiElement codeFragment,
                                              Collection<PsiStatement> exitStatements,
                                              ControlFlow controlFlow,
                                              int startOffset,
                                              int endOffset) {
    final LocalSearchScope scope = new LocalSearchScope(codeFragment);
    Variables:
    for (Iterator<ParameterTablePanel.VariableData> iterator = myInputVariables.iterator(); iterator.hasNext();) {
      final ParameterTablePanel.VariableData data = iterator.next();
      for (PsiReference ref : ReferencesSearch.search(data.variable, scope)) {
        PsiElement element = ref.getElement();
        int elementOffset = controlFlow.getStartOffset(element);
        if (elementOffset >= startOffset && elementOffset <= endOffset) {
          if (!isInExitStatements(element, exitStatements)) continue Variables;
        }
      }
      iterator.remove();
    }
  }

  private static boolean isInExitStatements(PsiElement element, Collection<PsiStatement> exitStatements) {
    for (PsiStatement exitStatement : exitStatements) {
      if (PsiTreeUtil.isAncestor(exitStatement, element, false)) return true;
    }
    return false;
  }


  public InputVariables copy() {
    final InputVariables inputVariables = new InputVariables(myInputVariables, myProject, myScope);
    inputVariables.myFoldingAvailable = myFoldingAvailable;
    inputVariables.myFolding = myFolding;
    inputVariables.myInitialParameters = myInitialParameters;
    return inputVariables;
  }


  public void appendCallArguments(ParameterTablePanel.VariableData data, StringBuilder buffer) {
    if (myFoldingAvailable) {
      buffer.append(myFolding.getGeneratedCallArgument(data));
    } else {
      buffer.append(data.variable.getName());
    }
  }

  public void setFoldingAvailable(boolean foldingAvailable) {
    myFoldingAvailable = foldingAvailable;

    myFolding.clear();
    myInputVariables.clear();
    myInputVariables.addAll(wrapInputVariables(myInitialParameters));
  }

  public void annotateWithParameter(PsiJavaCodeReferenceElement reference) {
    for (ParameterTablePanel.VariableData data : myInputVariables) {
      final PsiElement element = reference.resolve();
      if (data.variable.equals(element)) {
        PsiType type = data.variable.getType();
        final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(reference, PsiMethodCallExpression.class);
        if (methodCallExpression != null) {
          int idx = ArrayUtil.find(methodCallExpression.getArgumentList().getExpressions(), reference);
          if (idx > -1) {
            final PsiMethod psiMethod = methodCallExpression.resolveMethod();
            if (psiMethod != null) {
              final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
              if (idx >= parameters.length) { //vararg parameter
                idx = parameters.length - 1;
                if (idx >= 0) { //incomplete code
                  type = parameters[idx].getType();
                }
              }
              if (type instanceof PsiEllipsisType) {
                type = ((PsiEllipsisType)type).getComponentType();
              }
            }
          }
        }
        if (!myFoldingAvailable || !myFolding.annotateWithParameter(data, reference)) {
          reference.putUserData(DuplicatesFinder.PARAMETER, Pair.create(data.variable, type));
        }
      }
    }
  }

  public boolean isFoldingSelectedByDefault() {
    return myFolding.isFoldingSelectedByDefault();
  }
}