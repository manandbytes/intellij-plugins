// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public final class JadeFileViewProviderFactory implements FileViewProviderFactory {

  @Override
  public @NotNull FileViewProvider createFileViewProvider(final @NotNull VirtualFile file,
                                                          final Language language,
                                                          final @NotNull PsiManager manager,
                                                          final boolean eventSystemEnabled) {
    return new JadeFileViewProvider(manager, file, eventSystemEnabled);
  }
}
