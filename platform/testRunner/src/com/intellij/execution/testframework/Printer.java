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
package com.intellij.execution.testframework;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.ui.ConsoleViewContentType;

public interface Printer {
  void print(String text, ConsoleViewContentType contentType);
  void onNewAvailable(Printable printable);
  void printHyperlink(String text, HyperlinkInfo info);
  void mark();

  interface Intermediate extends Printer, Printable {}

  Intermediate DEAF = new Intermediate() {
    public void print(final String text, final ConsoleViewContentType contentType) {}
    public void onNewAvailable(final Printable printable) {}
    public void printHyperlink(final String text, final HyperlinkInfo info) {}
    public void mark() {}
    public void printOn(final Printer printer) {}
  };
}
