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
package com.intellij.openapi.editor;

import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;

/**
 * @author yole
*/
public class DisposableEditorPanel extends JPanel implements Disposable {
  private final Editor myEditor;

  public DisposableEditorPanel(Editor editor) {
    super(new BorderLayout());
    myEditor = editor;
    add(editor.getComponent(), BorderLayout.CENTER);
  }

  public void dispose() {
    EditorFactory.getInstance().releaseEditor(myEditor);
  }
}
