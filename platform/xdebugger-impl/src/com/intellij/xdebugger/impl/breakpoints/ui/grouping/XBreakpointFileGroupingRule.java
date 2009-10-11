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
package com.intellij.xdebugger.impl.breakpoints.ui.grouping;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author nik
 */
public class XBreakpointFileGroupingRule<B extends XLineBreakpoint<?>> extends XBreakpointGroupingRule<B, XBreakpointFileGroup> {
  public XBreakpointFileGroupingRule() {
    super("by-file", XDebuggerBundle.message("rule.name.group.by.file"));
  }

  public XBreakpointFileGroup getGroup(@NotNull final B breakpoint, @NotNull final Collection<XBreakpointFileGroup> groups) {
    XSourcePosition position = breakpoint.getSourcePosition();
    if (position == null) return null;

    VirtualFile file = position.getFile();
    for (XBreakpointFileGroup group : groups) {
      if (group.getFile().equals(file)) {
        return group;
      }
    }

    return new XBreakpointFileGroup(file);
  }
}
