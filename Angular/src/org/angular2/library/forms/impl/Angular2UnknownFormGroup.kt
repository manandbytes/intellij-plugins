package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolsScope
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.util.containers.Stack
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormGroup : PolySymbol {

  override val name: @NlsSafe String
    get() = "Unknown form group"

  override val pattern: PolySymbolsPattern? = PolySymbolsPatternFactory.createRegExMatch(".*")

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: PolySymbolsListSymbolsQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbolsScope> =
    when (qualifiedKind) {
      NG_FORM_CONTROL_PROPS -> listOf(Angular2UnknownFormControl)
      NG_FORM_GROUP_FIELDS -> listOf(Angular2UnknownFormArray)
      NG_FORM_GROUP_PROPS -> listOf(this)
      else -> emptyList()
    }

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind in NG_FORM_ANY_CONTROL_PROPS

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION to true,
          PolySymbol.Companion.PROP_DOC_HIDE_PATTERN to true)

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = NG_FORM_GROUP_PROPS

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)
}