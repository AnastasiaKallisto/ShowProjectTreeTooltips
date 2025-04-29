package com.github.AnastasiaKallisto.showprojecttreetooltips;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpDummyBlock;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.CSharpNamespaceDeclaration;
import com.jetbrains.rider.languages.fileTypes.csharp.psi.impl.CSharpDummyDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TooltipUtils {

    /**
     * Асинхронно извлекает текст подсказки, не блокируя UI-поток (EDT),
     * и обновляет тултип для указанного дерева.
     * <p>
     * Вытаскивание текста происходит в фоне через ReadAction.nonBlocking,
     * затем безопасно обновляется на UI-потоке.
     *
     * @param project            Текущий проект для привязки жизни задачи.
     * @param extractionFunction Функция для получения текста подсказки.
     * @param tree               Дерево, у которого нужно обновить тултип.
     * @param maxSymbols         Максимальное количество символов в подсказке.
     */
    public static void safeExtractTooltip(@NotNull Project project,
                                          @NotNull Supplier<@Nullable String> extractionFunction,
                                          @NotNull JTree tree,
                                          int maxSymbols) {
        ReadAction.nonBlocking(extractionFunction::get)
                .expireWith(project)
                .finishOnUiThread(ModalityState.any(), tooltipText -> updateTooltip(tree, tooltipText, maxSymbols))
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    /**
     * Обновляет тултип дерева на основе переданного текста.
     * Если текст пустой — сбрасывает тултип.
     *
     * @param tree        Дерево, у которого нужно изменить подсказку.
     * @param tooltipText Текст подсказки (может быть null).
     * @param maxSymbols  Максимальное количество символов, которые будут показаны.
     */
    private static void updateTooltip(JTree tree, @Nullable String tooltipText, int maxSymbols) {
        if (tooltipText != null) {
            String htmlTooltip = "<html><body style='width:200px;'>" +
                    tooltipText.substring(0, Math.min(tooltipText.length(), maxSymbols)) +
                    "</body></html>";
            tree.setToolTipText(htmlTooltip);
        } else {
            tree.setToolTipText(null);
        }
    }

    /**
     * извлекает текст указанного XML-тега из содержимого файла.
     * <p>
     * Применяется для файлов типа .csproj (или любых других XML-файлов).
     * Читает содержимое через VfsUtilCore.loadText() без блокировки EDT.
     *
     * @param file Файл, из которого нужно извлечь содержимое тега.
     * @param tag  имя XML-тега для поиска.
     * @return Содержимое тега или null, если тег не найден или произошла ошибка.
     */
    public static String extractXmlTag(VirtualFile file, String tag) {
        try {
            // Магия. Работает с уже загруженным содержимым файловой системы (и не падает на EDT):
            String text = VfsUtilCore.loadText(file);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
            if (doc.getElementsByTagName(tag).getLength() > 0) {
                return doc.getElementsByTagName(tag).item(0).getTextContent();
            }
        } catch (Exception ignored) {
            // do nothing
        }
        return null;
    }

    /**
     * <summary>
     * извлекает текст XML-документации (тег <b>&lt;summary&gt;</b>) из первого найденного C# класса,
     * структуры или интерфейса в указанном файле. <br/>
     * Сначала пытается найти такие элементы внутри пространства имён, если оно присутствует.
     * Если пространства имён нет, выполняет поиск в корне файла. <br/>
     * Возвращает текст комментария (без ///, но с &lt;br/&gt;)
     * </summary>
     *
     * @param virtualFile Файл .cs, из которого нужно извлечь документацию.
     * @param project     Текущий проект, необходимый для получения PSI.
     * @return Строка с текстом документации или null, если комментарий не найден.
     */
    public static String extractSummary(VirtualFile virtualFile, @NotNull Project project) {
        // Получаем PSI-файл для работы с синтаксическим деревом
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        Optional<@NotNull PsiElement> csharpElement;

        // Пробуем найти пространство имён
        var maybeNamespace = Arrays.stream(psiFile.getChildren())
                .filter(child -> child instanceof CSharpNamespaceDeclaration)
                .findFirst();
        if (maybeNamespace.isPresent()) {
            // Если пространство имён найдено, ищем в нём CSharpDummyDeclaration через CSharpDummyBlock
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
            // Если пространства имён нет, ищем CSharpDummyDeclaration в корне
            csharpElement = Arrays.stream(psiFile.getChildren())
                    .flatMap(element -> Arrays.stream(element.getChildren()))
                    .filter(child -> child instanceof CSharpDummyDeclaration)
                    .findFirst();
        }

        // извлекаем XML-комментарий, если он есть
        if (csharpElement != null && csharpElement.isPresent()) {

            // ищем комментарии "вплоть до элемента"
            PsiElement[] siblings = csharpElement.get().getParent().getChildren();
            List<String> collectedComments = new ArrayList<>();

            for (PsiElement sibling : siblings) {
                if (sibling == csharpElement.get()) break;

                if (sibling instanceof PsiCommentImpl) {
                    String text = sibling.getText().replaceFirst("^///", "").trim();
                    if (!text.isEmpty()) {
                        collectedComments.add(text.replaceFirst("^<summary>", "").replaceFirst("</summary>", ""));
                    }
                }
            }

            if (!collectedComments.isEmpty()) {
                return String.join("\n", collectedComments);
            }
        }

        return null; // комментарий не найден
    }

}
