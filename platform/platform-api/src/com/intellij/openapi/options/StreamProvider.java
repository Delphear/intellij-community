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
package com.intellij.openapi.options;

import com.intellij.openapi.components.RoamingType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface StreamProvider {
  StreamProvider DEFAULT = new StreamProvider(){
    public void saveContent(final String fileSpec, final InputStream content, final long size, final RoamingType roamingType, boolean async) throws IOException {

    }

    public InputStream loadContent(final String fileSpec, final RoamingType roamingType) throws IOException {
      return null;
    }

    public String[] listSubFiles(final String fileSpec) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    public void deleteFile(final String fileSpec, final RoamingType roamingType) {

    }

    public boolean isEnabled() {
      return false;
    }
  };

  void saveContent(String fileSpec, InputStream content, final long size, final RoamingType roamingType, boolean async) throws IOException;

  @Nullable
  InputStream loadContent(final String fileSpec, final RoamingType roamingType) throws IOException;

  String[] listSubFiles(final String fileSpec);

  void deleteFile(final String fileSpec, final RoamingType roamingType);

  boolean isEnabled();
}
