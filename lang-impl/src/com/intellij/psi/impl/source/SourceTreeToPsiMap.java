package com.intellij.psi.impl.source;

import com.intellij.extapi.psi.ASTDelegatePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import org.jetbrains.annotations.Nullable;

public class SourceTreeToPsiMap {
  private SourceTreeToPsiMap() {
  }

  public static PsiElement treeElementToPsi(@Nullable ASTNode element) {
    if (element == null) return null;
    return element.getPsi();
  }

  public static ASTNode psiElementToTree(@Nullable PsiElement psiElement) {
    if (psiElement == null) return null;
    return psiElement.getNode();
  }

  public static boolean hasTreeElement(@Nullable PsiElement psiElement) {
    return psiElement instanceof TreeElement || psiElement instanceof ASTDelegatePsiElement || psiElement instanceof PsiFileImpl;
  }
}
