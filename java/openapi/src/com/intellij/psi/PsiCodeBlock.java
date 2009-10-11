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
package com.intellij.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Java code block, usually surrounded by curly braces.
 */
public interface PsiCodeBlock extends PsiElement {
  /**
   * The empty array of PSI code blocks which can be reused to avoid unnecessary allocations.
   */
  PsiCodeBlock[] EMPTY_ARRAY = new PsiCodeBlock[0];

  /**
   * Returns the array of statements contained in the block.
   *
   * @return the array of statements.
   */
  @NotNull
  PsiStatement[] getStatements();

  /**
   * Returns the first PSI element contained in the block.
   *
   * @return the first PSI element, or null if the block is empty.
   */
  @Nullable
  PsiElement getFirstBodyElement();

  /**
   * Returns the last PSI element contained in the block.
   *
   * @return the last PSI element, or null if the block is empty.
   */
  @Nullable
  PsiElement getLastBodyElement();

  /**
   * Returns the opening curly brace of the block.
   *
   * @return the opening curly brace, or null if the block does not have one.
   */
  @Nullable
  PsiJavaToken getLBrace();

  /**
   * Returns the closing curly brace of the block.
   *
   * @return the closing curly brace, or null if the block does not have one.
   */
  @Nullable
  PsiJavaToken getRBrace();
}
