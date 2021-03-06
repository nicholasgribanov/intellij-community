// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.plugins.newui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.options.newEditor.SettingsDialog;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Lobas
 */
public class RestartButton extends InstallButton {
  public RestartButton(@NotNull MyPluginModel pluginModel) {
    super(true);
    addActionListener(e -> {
      pluginModel.needRestart = true;
      pluginModel.createShutdownCallback = false;

      DialogWrapper settings = DialogWrapper.findInstance(this);
      assert settings instanceof SettingsDialog : settings;
      ((SettingsDialog)settings).doOKAction();

      ((ApplicationImpl)ApplicationManager.getApplication()).exit(true, false, true);
    });
  }

  @Override
  protected void setTextAndSize() {
    setText("Restart IDE");
  }
}