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

package com.intellij.history.core.changes;

import com.intellij.history.core.LocalVcsTestCase;
import com.intellij.history.core.tree.Entry;
import com.intellij.history.core.tree.RootEntry;
import org.junit.Before;
import org.junit.Test;

public class PutLabelChangesTest extends LocalVcsTestCase {
  Entry r;

  @Before
  public void setUp() {
    r = new RootEntry();
    createDirectory(r, 1, "dir");
    createDirectory(r, 2, "dir/subDir");
    createFile(r, 3, "dir/subDir/file", null, -1);
  }

  @Test
  public void testLabelOnFile() {
    Change c = new PutEntryLabelChange("dir/subDir/file", null, -1);
    c.applyTo(r);

    assertFalse(c.affects(r.getEntry("dir")));
    assertFalse(c.affects(r.getEntry("dir/subDir")));
    assertTrue(c.affects(r.getEntry("dir/subDir/file")));
  }

  @Test
  public void testLabelOnDirectory() {
    Change c = new PutEntryLabelChange("dir/subDir", null, -1);
    c.applyTo(r);

    assertFalse(c.affects(r.getEntry("dir")));
    assertTrue(c.affects(r.getEntry("dir/subDir")));
    assertTrue(c.affects(r.getEntry("dir/subDir/file")));
  }

  @Test
  public void testGlobalLabel() {
    Change c = new PutLabelChange(null, -1);
    c.applyTo(r);

    assertTrue(c.affects(r.getEntry("dir")));
    assertTrue(c.affects(r.getEntry("dir/subDir")));
    assertTrue(c.affects(r.getEntry("dir/subDir/file")));
  }
}