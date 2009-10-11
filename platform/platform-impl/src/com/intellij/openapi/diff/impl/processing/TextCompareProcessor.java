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
package com.intellij.openapi.diff.impl.processing;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.ex.DiffFragment;
import com.intellij.openapi.diff.impl.ComparisonPolicy;
import com.intellij.openapi.diff.impl.fragments.Fragment;
import com.intellij.openapi.diff.impl.fragments.LineFragment;
import com.intellij.openapi.diff.impl.highlighting.FragmentSide;
import com.intellij.openapi.diff.impl.highlighting.LineBlockDivider;
import com.intellij.openapi.diff.impl.highlighting.Util;

import java.util.ArrayList;
import java.util.Iterator;

public class TextCompareProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.diff.impl.processing.Processor");
  private final ComparisonPolicy myComparisonPolicy;

  public TextCompareProcessor(ComparisonPolicy comparisonPolicy) {
    myComparisonPolicy = comparisonPolicy;
  }

  public ArrayList<LineFragment> process(String text1, String text2) {
    DiffFragment[] woFormattingBlocks = DiffPolicy.LINES_WO_FORMATTING.buildFragments(text1, text2);
    DiffFragment[] step1lineFragments = new DiffCorrection.TrueLineBlocks(myComparisonPolicy).
        correctAndNormalize(woFormattingBlocks);
    ArrayList<LineFragment> lineBlocks = new DiffFragmentsProcessor().process(step1lineFragments);
    for (Iterator<LineFragment> iterator = lineBlocks.iterator(); iterator.hasNext();) {
      LineFragment lineBlock = iterator.next();
      if (lineBlock.isOneSide() || lineBlock.isEqual()) continue;
      String subText1 = lineBlock.getText(text1, FragmentSide.SIDE1);
      String subText2 = lineBlock.getText(text2, FragmentSide.SIDE2);
      ArrayList<LineFragment> subFragments = findSubFragments(subText1, subText2);
      lineBlock.setChildren(new ArrayList<Fragment>(subFragments));
    }
    return lineBlocks;
  }

  private ArrayList<LineFragment> findSubFragments(String text1, String text2) {
    DiffFragment[] fragments = new ByWord(myComparisonPolicy).buildFragments(text1, text2);
    fragments = DiffCorrection.ConnectSingleSideToChange.INSTANCE.correct(fragments);
    fragments = UniteSameType.INSTANCE.correct(fragments);
    fragments = PreferWholeLines.INSTANCE.correct(fragments);
    fragments = UniteSameType.INSTANCE.correct(fragments);
    DiffFragment[][] lines = Util.splitByUnchangedLines(fragments);
    lines = Util.uniteFormattingOnly(lines);

    LineFragmentsCollector collector = new LineFragmentsCollector();
    for (int i = 0; i < lines.length; i++) {
      DiffFragment[] line = lines[i];
      DiffFragment[][] subLines = LineBlockDivider.SINGLE_SIDE.divide(line);
      subLines = Util.uniteFormattingOnly(subLines);
      for (int j = 0; j < subLines.length; j++) {
        DiffFragment[] subLineFragments = subLines[j];
        LineFragment subLine = collector.addDiffFragment(Util.concatenate(subLineFragments));
        if (!subLine.isOneSide()) {
          subLine.setChildren(processInlineFragments(subLineFragments));
        }
      }
    }
    return collector.getFragments();
  }

  private ArrayList<Fragment> processInlineFragments(DiffFragment[] subLineFragments) {
    LOG.assertTrue(subLineFragments.length > 0);
    FragmentsCollector result = new FragmentsCollector();
    for (int i = 0; i < subLineFragments.length; i++) {
      DiffFragment fragment = subLineFragments[i];
      result.addDiffFragment(fragment);
    }
    return result.getFragments();
  }
}
