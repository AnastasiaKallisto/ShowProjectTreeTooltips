package com.jetbrains.rider.plugins.showprojecttreetooltips;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpDocComment;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpNamespaceDeclaration;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.impl.CSharpDummyDeclaration;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public class TooltipUtils {

    public static String extractXmlTag(VirtualFile file, String tag) {
        try (InputStream inputStream = file.getInputStream()) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            if (doc.getElementsByTagName(tag).getLength() > 0) {
                return doc.getElementsByTagName(tag).item(0).getTextContent();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String extractSummary(VirtualFile virtualFile, @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        Optional<@NotNull PsiElement> first = Arrays.stream(psiFile.getChildren())
                .flatMap(element -> Arrays.stream(element.getChildren()))
                .filter(child -> child instanceof CSharpDummyDeclaration)
                .findFirst();
        if (first.isPresent() && first.get() instanceof CSharpNamespaceDeclaration) {
            // если namespace, добавляем его детей
            first = Optional.ofNullable(first.get().getFirstChild());
        }
        if (first.isPresent()) {
            CSharpDocComment docComment = ((CSharpDummyDeclaration) first.get()).getDocComment();
            if (docComment != null)
                return docComment.getMeaningfulText();
        }
        return null;
    }

}
