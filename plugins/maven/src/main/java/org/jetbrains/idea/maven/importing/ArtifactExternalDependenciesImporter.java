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
package org.jetbrains.idea.maven.importing;

import com.intellij.openapi.roots.ui.configuration.artifacts.ManifestFilesInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementResolvingContext;
import com.intellij.packaging.impl.artifacts.ArtifactUtil;
import com.intellij.packaging.impl.artifacts.PackagingElementProcessor;
import com.intellij.packaging.impl.elements.ArtifactElementType;
import com.intellij.packaging.impl.elements.ArtifactPackagingElement;
import com.intellij.packaging.ui.ManifestFileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nik
 */
public class ArtifactExternalDependenciesImporter {
  private ManifestFilesInfo myManifestFiles = new ManifestFilesInfo();
  private Map<Artifact, List<PackagingElement<?>>> myExternalDependencies = new HashMap<Artifact, List<PackagingElement<?>>>();

  public ManifestFileConfiguration getManifestFile(@NotNull Artifact artifact,
                                                   @NotNull PackagingElementResolvingContext context) {
    return myManifestFiles.getManifestFile(artifact.getRootElement(), artifact.getArtifactType(), context);
  }

  public List<PackagingElement<?>> getExternalDependenciesList(@NotNull Artifact artifact) {
    List<PackagingElement<?>> elements = myExternalDependencies.get(artifact);
    if (elements == null) {
      elements = new ArrayList<PackagingElement<?>>();
      myExternalDependencies.put(artifact, elements);
    }
    return elements;
  }

  public void applyChanges(ModifiableArtifactModel artifactModel, final PackagingElementResolvingContext context) {
    myManifestFiles.saveManifestFiles();
    final List<Pair<? extends CompositePackagingElement<?>, List<PackagingElement<?>>>> elementsToInclude = new ArrayList<Pair<? extends CompositePackagingElement<?>, List<PackagingElement<?>>>>();
    for (Artifact artifact : artifactModel.getArtifacts()) {
      ArtifactUtil.processPackagingElements(artifact, ArtifactElementType.ARTIFACT_ELEMENT_TYPE, new PackagingElementProcessor<ArtifactPackagingElement>() {
        @Override
        public boolean process(@NotNull List<CompositePackagingElement<?>> parents,
                               @NotNull ArtifactPackagingElement artifactPackagingElement) {
          final Artifact included = artifactPackagingElement.findArtifact(context);
          if (!parents.isEmpty() && included != null) {
            final CompositePackagingElement<?> parent = parents.get(0);
            final List<PackagingElement<?>> elements = myExternalDependencies.get(included);
            if (elements != null) {
              elementsToInclude.add(Pair.create(parent, elements));
            }
          }
          return true;
        }
      }, context, false);
    }

    for (Pair<? extends CompositePackagingElement<?>, List<PackagingElement<?>>> pair : elementsToInclude) {
      pair.getFirst().addOrFindChildren(pair.getSecond());
    }
  }
}
