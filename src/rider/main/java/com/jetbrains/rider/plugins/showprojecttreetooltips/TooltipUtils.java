package com.jetbrains.rider.plugins.showprojecttreetooltips;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpDocComment;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpDummyBlock;
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

    /**
     * <summary>
     * Извлекает текст XML-документации (тег <b>&lt;summary&gt;</b>) из первого найденного C# класса,
     * структуры или интерфейса в указанном файле. <br/>
     * Сначала пытается найти такие элементы внутри пространства имён, если оно присутствует.
     * Если пространства имён нет, выполняет поиск в корне файла.
     * Возвращает текст комментария (без ///, но с &lt;br/&gt;)
     * </summary>
     *
     * @param virtualFile Файл .cs, из которого нужно извлечь документацию.
     * @param project     Текущий проект, необходимый для получения PSI.
     * @return Строка с текстом документации или null, если комментарий не найден.
     */
    public static String extractSummary(VirtualFile virtualFile, @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        Optional<@NotNull PsiElement> csharpElement;

        var maybeNamespace = Arrays.stream(psiFile.getChildren())
                .filter(child -> child instanceof CSharpNamespaceDeclaration)
                .findFirst();
        if (maybeNamespace.isPresent()){
            csharpElement = maybeNamespace
                    .map(namespace -> Arrays.stream(namespace.getChildren())
                            .filter(child -> child instanceof CSharpDummyBlock)
                            .findFirst()
                            .map(block -> Arrays.stream(block.getChildren())
                                    .filter(child -> child instanceof CSharpDummyDeclaration)
                                    .findFirst()
                                    .orElse(null))
                            .orElse(null)
                    );
        } else {
            csharpElement = Arrays.stream(psiFile.getChildren())
                    .flatMap(element -> Arrays.stream(element.getChildren()))
                    .filter(child -> child instanceof CSharpDummyDeclaration)
                    .findFirst();
        }
        if (csharpElement != null && csharpElement.isPresent()) {
            CSharpDocComment docComment = ((CSharpDummyDeclaration) csharpElement.get()).getDocComment();
            if (docComment != null)
                return docComment.getMeaningfulText();
        }
        return null;
    }

}
