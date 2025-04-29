package com.github.AnastasiaKallisto.showprojecttreetooltips;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


/**
 * <summary>
 * Активность, запускаемая после старта проекта в Rider. <br/>
 * Подключает отображение тултипов с кратким описанием
 * C# файлов (summary) и проектов (.csproj) в дереве проекта.
 * </summary>
 *
 * <p>Тултипы отображаются при наведении мыши на элементы дерева проекта
 * в зависимости от настроек пользователя:
 * включены ли подсказки для .csproj и .cs файлов и какая задана максимальная длина текста.</p>
 */
public class TooltipProjectActivity implements ProjectActivity {
    // Кэш последнего узла, чтобы избежать лишнего обновления тултипа
    private static SolutionExplorerModelNode lastNode = null;

    /**
     * Метод выполняется при старте проекта.
     * инициализирует слушатель наведения мыши на дерево проекта.
     *
     * @param project      текущий проект Rider
     * @param continuation kotlin-контекст для совместимости с coroutines
     * @return Unit.INSTANCE
     */
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        SwingUtilities.invokeLater(() -> {
            // Получаем панель проекта и дерево проекта
            ProjectView projectView = ProjectView.getInstance(project);
            JTree tree = projectView.getCurrentProjectViewPane().getTree();
            if (tree == null) return;

            // Получаем настройки плагина
            var state = AppSettings.getInstance().getState();

            Color tooltipBg = UIManager.getColor("Tooltip.background");
            Color tooltipFg = UIManager.getColor("Tooltip.foreground");
            UIManager.put("ToolTip.background", tooltipBg);
            UIManager.put("ToolTip.foreground", tooltipFg);

            // Добавляем слушатель движения мыши
            tree.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    // Проверка, включены ли тултипы в настройках
                    if (!state.showCsprojDescription && !state.showClassSummary)
                        return;

                    // Определяем путь элемента под курсором
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        tree.setToolTipText(null);
                        return;
                    }

                    Object userObject = path.getLastPathComponent();

                    // Проверка, что это узел дерева Rider
                    if (!(userObject instanceof SolutionExplorerModelNode)) {
                        tree.setToolTipText(null);
                        return;
                    }

                    // Оптимизация: если узел тот же, не обновляем тултип
                    if (userObject == lastNode && tree.getToolTipText(e) != null) {
                        return;
                    }

                    lastNode = (SolutionExplorerModelNode) userObject;
                    var virtualFile = lastNode.getVirtualFile();

                    if (virtualFile == null) {
                        tree.setToolTipText(null);
                        return;
                    }

                    // Определяем расширение файла
                    var fileExtension = virtualFile.getExtension();

                    // извлекаем текст подсказки в зависимости от типа файла
                    if (state.showCsprojDescription && "csproj".equals(fileExtension)) {
                        TooltipUtils.safeExtractTooltip(project, () -> TooltipUtils.extractXmlTag(virtualFile, "Description"), tree, state.maxSymbols);
                    } else if (state.showClassSummary && "cs".equals(fileExtension)) {
                        TooltipUtils.safeExtractTooltip(project, () -> TooltipUtils.extractSummary(virtualFile, project), tree, state.maxSymbols);
                    }
                }
            });
        });

        return Unit.INSTANCE;
    }
}

