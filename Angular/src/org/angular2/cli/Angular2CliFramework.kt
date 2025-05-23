// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.framework.FrameworkType
import icons.AngularIcons
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

/**
 * @author Dennis.Ushakov
 */
class Angular2CliFramework private constructor() : FrameworkType(ID) {

  override fun getPresentableName(): String {
    return Angular2Bundle.message("angular.description.angular-cli")
  }

  override fun getIcon(): Icon {
    return AngularIcons.Angular2
  }

  companion object {
    @JvmField
    val INSTANCE: Angular2CliFramework = Angular2CliFramework()

    @NonNls
    const val ID: String = "AngularCLI"
  }
}
