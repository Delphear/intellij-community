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

package com.intellij.execution.ui.layout.impl;

import com.intellij.execution.ui.layout.Tab;
import com.intellij.execution.ui.layout.View;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.openapi.util.Key;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;

public class ViewImpl implements View {

  public static final Key<String> ID = Key.create("ViewID");

  private String myID;

  private Tab myTab;
  private int myTabIndex;

  private PlaceInGrid myPlaceInGrid;

  private boolean myMinimizedInGrid;

  public ViewImpl(String id, TabImpl tab, final PlaceInGrid placeInGrid, boolean minimizedInGrid) {
    myID = id;
    myTab = tab;
    myPlaceInGrid = placeInGrid;
    myMinimizedInGrid = minimizedInGrid;
  }

  public ViewImpl(RunnerLayout settings, Element element) {
    XmlSerializer.deserializeInto(this, element);
    assignTab(settings.getOrCreateTab(myTabIndex));
  }

  public void write(final Element content) {
    content.addContent(XmlSerializer.serialize(this));
  }

  public Tab getTab() {
    return myTab;
  }

  public PlaceInGrid getPlaceInGrid() {
    return myPlaceInGrid;
  }


  public boolean isMinimizedInGrid() {
    return myMinimizedInGrid;
  }

  public void setID(final String ID) {
    myID = ID;
  }

  public String getID() {
    return myID;
  }


  public void setMinimizedInGrid(final boolean minimizedInGrid) {
    myMinimizedInGrid = minimizedInGrid;
  }

  public void setPlaceInGrid(PlaceInGrid placeInGrid) {
    myPlaceInGrid = placeInGrid;
  }

  public void assignTab(final Tab tab) {
    myTab = tab;
  }

  public int getTabIndex() {
    return myTab != null ? myTab.getIndex() : myTabIndex;
  }

  public void setTabIndex(final int tabIndex) {
    myTabIndex = tabIndex;
  }

  public static class Default {

    private final String myID;
    private final int myTabID;
    private final PlaceInGrid myPlaceInGrid;
    private final boolean myMinimizedInGrid;

    public Default(final String ID, final int tabID, final PlaceInGrid placeInGrid, final boolean minimizedInGrid) {
      myID = ID;
      myTabID = tabID;
      myPlaceInGrid = placeInGrid;
      myMinimizedInGrid = minimizedInGrid;
    }

    public ViewImpl createView(RunnerLayout settings) {
      final TabImpl tab = myTabID == Integer.MAX_VALUE ? settings.createNewTab() : settings.getOrCreateTab(myTabID);
      return new ViewImpl(myID, tab, myPlaceInGrid, myMinimizedInGrid);
    }

    public PlaceInGrid getPlaceInGrid() {
      return myPlaceInGrid;
    }
  }

}
