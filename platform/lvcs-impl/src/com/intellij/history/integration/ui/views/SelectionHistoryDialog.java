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

package com.intellij.history.integration.ui.views;

import com.intellij.history.core.LocalVcs;
import com.intellij.history.integration.IdeaGateway;
import com.intellij.history.integration.ui.models.FileHistoryDialogModel;
import com.intellij.openapi.vfs.VirtualFile;

public class SelectionHistoryDialog extends FileHistoryDialog {
  private final int myFrom;
  private final int myTo;

  public SelectionHistoryDialog(IdeaGateway gw, VirtualFile f, int from, int to) {
    super(gw, f, false);
    myFrom = from;
    myTo = to;
    init();
  }

  @Override
  protected FileHistoryDialogModel createModel(LocalVcs vcs) {
    return new SelectionHistoryDialogModel(myGateway, vcs, myFile, myFrom, myTo);
  }
}
