// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.refactoring.rename.inplace

import com.intellij.codeInsight.hints.presentation.DynamicDelegatePresentation
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.refactoring.rename.inplace.SelectableInlayPresentation.SelectionListener
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicReference

class SelectableInlayButton(
  private val editor: EditorEx,
  private val default: InlayPresentation,
  private val active: InlayPresentation,
  private val hovered: InlayPresentation,
  private val inlayToUpdate: AtomicReference<Inlay<PresentationRenderer>>
  ): DynamicDelegatePresentation(default), SelectableInlayPresentation {

  private val selectionListeners: MutableList<SelectionListener> = mutableListOf()

  override var isSelected = false
    set(value) {
      field = value
      selectionListeners.forEach { it.selectionChanged(value) }
      update()
    }

  override fun addSelectionListener(listener: SelectionListener) {
    selectionListeners.add(listener)
  }

  private var isHovered = false
    set(value) {
      field = value
      update()
    }

  private fun update(){
    delegate = when {
      isHovered -> hovered
      isSelected -> active
      else -> default
    }
    inlayToUpdate.get()?.repaint()
  }

  override fun mouseClicked(event: MouseEvent, translated: Point) {
    isSelected = true
  }

  override fun mouseExited() {
    editor.setCustomCursor(this, null)
    isHovered = false
  }

  override fun mouseMoved(event: MouseEvent, translated: Point) {
    val defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    editor.setCustomCursor(this, defaultCursor)
    isHovered = true
  }
}