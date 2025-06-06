// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation

import com.intellij.lang.javascript.navigation.DumbAwareChooseByNameContributor
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi.PsiNavigatablePolySymbolNavigationTarget
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider.getDirective
import org.angular2.entities.Angular2EntityUtils.getAttributeDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getAttributeName
import org.angular2.entities.Angular2EntityUtils.getElementDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getElementName
import org.angular2.entities.Angular2EntityUtils.isAttributeDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.isElementDirectiveIndexName
import org.angular2.index.Angular2IndexUtil
import org.angular2.index.Angular2SourceDirectiveIndexKey
import org.angular2.index.Angular2SymbolIndex

class Angular2GotoSymbolContributor : DumbAwareChooseByNameContributor() {
  override fun doProcessNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
    StubIndex.getInstance().processAllKeys(Angular2SourceDirectiveIndexKey, { key: String ->
      val name = when {
        isElementDirectiveIndexName(key) -> {
          getElementName(key)
        }
        isAttributeDirectiveIndexName(key) -> {
          getAttributeName(key)
        }
        else -> {
          return@processAllKeys true
        }
      }
      if (!name.isEmpty()) {
        return@processAllKeys processor.process(name)
      }
      true
    }, scope, filter)
    ContainerUtil.process(Angular2IndexUtil.getAllKeys(Angular2SymbolIndex.KEY, scope.project!!), processor)
  }

  override fun doProcessElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
    for (indexName in sequenceOf(getAttributeDirectiveIndexName(name), getElementDirectiveIndexName(name))) {
      StubIndex.getInstance().processElements(
        Angular2SourceDirectiveIndexKey, indexName, parameters.project, parameters.searchScope,
        parameters.idFilter, JSImplicitElementProvider::class.java
      ) { provider ->
        for (element in provider.indexingData?.implicitElements ?: emptyList()) {
          if (element.isValid) {
            val directive = getDirective(element)
            if (directive != null) {
              return@processElements processSelectors(name, directive.selector.simpleSelectorsWithPsi, processor)
            }
          }
        }
        true
      }
    }
    Angular2IndexUtil.multiResolve(parameters.project, parameters.searchScope, Angular2SymbolIndex.KEY, name, processor)
  }

  private fun processSelectors(name: String,
                               selectors: List<SimpleSelectorWithPsi>,
                               processor: Processor<in NavigationItem>): Boolean {
    for (selector in selectors) {
      if (!processSelectorElement(name, selector.element, processor)) {
        return false
      }
      for (attribute in selector.attributes) {
        if (!processSelectorElement(name, attribute, processor)) {
          return false
        }
      }
      if (!processSelectors(name, selector.notSelectors, processor)) {
        return false
      }
    }
    return true
  }

  private fun processSelectorElement(
    name: String,
    element: Angular2DirectiveSelectorSymbol?,
    processor: Processor<in NavigationItem>,
  ): Boolean {
    if (element == null || name != element.name) return true
    for (target in element.getNavigationTargets(element.project)) {
      val navigationItem = (target as? PsiNavigatablePolySymbolNavigationTarget)?.getNavigationItem()
      if (navigationItem != null && processor.process(navigationItem)) {
        return true
      }
    }
    return false
  }
}