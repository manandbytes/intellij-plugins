// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.JavaLookupElementBuilder;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.hint.ShowParameterInfoHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.PsiTypeLookupItem;
import com.intellij.coldFusion.UI.editorActions.completionProviders.CfmlMethodInsertHandler;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.ui.IconManager;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlLookUpItemUtil implements PlatformIcons {
  public static LookupElement functionDescriptionToLookupItem(final CfmlFunctionDescription functionDescription) {
    String name = functionDescription.getName();
    String typeText = functionDescription.getReturnType();
    String tailText = "(" + functionDescription.getParametersListPresentableText() + ")";

    return LookupElementBuilder.create(name).withTypeText(typeText).withIcon(
        IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method))
      .withInsertHandler(new ParenthesesInsertHandler<>() {
        @Override
        protected boolean placeCaretInsideParentheses(final InsertionContext context, final LookupElement item) {
          return !functionDescription.getParameters().isEmpty();
        }

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
          super.handleInsert(context, item);
          new ShowParameterInfoHandler().invoke(context.getProject(), context.getEditor(), context.getFile());
        }
      }).withTailText(tailText).withCaseSensitivity(false);
  }

  public static LookupElement namedElementToLookupItem(PsiNamedElement element, @Nullable String prefix) {
    if (element instanceof LookupElement) return (LookupElement)element;
    // about java
    if (element instanceof PsiClass) {
      return JavaLookupElementBuilder.forClass((PsiClass)element);
    }
    if (element instanceof PsiMethod) {
      if (((PsiMethod)element).isConstructor()) {
        return JavaLookupElementBuilder.forMethod((PsiMethod)element, "init", PsiSubstitutor.EMPTY, null);
      }
      return JavaLookupElementBuilder.forMethod((PsiMethod)element, PsiSubstitutor.EMPTY);
    }
    if (element instanceof PsiVariable) {
      //noinspection CastConflictsWithInstanceof
      return JavaLookupElementBuilder.forField((PsiField)element);
    }
    if (element instanceof PsiType) {
      return PsiTypeLookupItem.createLookupItem((PsiType)element, null);
    }
    // about cfml

    String name = element.getName();
    String typeText = null;
    String tailText = null;

    if (element instanceof CfmlFunction) {
      tailText = "(" + ((CfmlFunction)element).getParametersAsString() + ")";
      PsiType returnType = ((CfmlFunction)element).getReturnType();
      typeText = returnType != null ? returnType.getCanonicalText() : null;
    }
    else if (element instanceof CfmlNamedAttributeImpl && element.getParent() instanceof CfmlFunction cfmlFunction) {
      tailText = "(" + cfmlFunction.getParametersAsString() + ")";
      PsiType returnType = cfmlFunction.getReturnType();
      typeText = returnType != null ? returnType.getCanonicalText() : null;
    }
    else if (element instanceof CfmlVariable) {
      PsiType type = ((CfmlVariable)element).getPsiType();
      if (type != null) {
        typeText = type.getPresentableText();
      }
      name = ((CfmlVariable)element).getlookUpString();
    }
    else if (element instanceof PsiDirectory) {
      name = element.getName();
      tailText = ".";
    }

    if (prefix != null && !prefix.isEmpty() && !StringUtil.toLowerCase(name).startsWith(StringUtil.toLowerCase(prefix))) {
      name = prefix + "." + name;
    }

    return LookupElementBuilder.create(element, name).withTypeText(typeText).withIcon(getIcon(element))
      .withInsertHandler(getInsertHandler(element)).withTailText(tailText).withCaseSensitivity(false);
  }

  private static @Nullable InsertHandler<LookupElement> getInsertHandler(PsiNamedElement element) {
    if (CfmlPsiUtil.isFunctionDefinition(element)) {
      return CfmlMethodInsertHandler.getInstance();
    }
    return null;
  }

  private static @Nullable Icon getIcon(PsiElement element) {
    if (CfmlPsiUtil.isFunctionDefinition(element)) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
    }
    else if (element instanceof CfmlParameter) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Parameter);
    }
    else if (element instanceof CfmlVariable) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
    }
    else if (element instanceof CfmlComponent) {
      if (((CfmlComponent)element).isInterface()) {
        return INTERFACE_ICON;
      }
      else {
        return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Class);
      }
    }
    return null;
  }

  public static CfmlFunctionDescription getFunctionDescription(CfmlFunction function) {
    PsiType returnType = function.getReturnType();
    CfmlFunctionDescription functionInfo = new CfmlFunctionDescription(function.getName(),
                                                                       returnType != null ? returnType.getCanonicalText() : null);
    CfmlParameter[] params = function.getParameters();
    for (CfmlParameter param : params) {
      functionInfo.addParameter(new CfmlFunctionDescription.CfmlParameterDescription(param.getName(), param.getType(), param.isRequired()));
    }
    return functionInfo;
  }
}
