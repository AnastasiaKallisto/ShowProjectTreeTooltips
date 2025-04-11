package com.jetbrains.rider.plugins.showprojecttreetooltips;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


public class TooltipProjectActivity implements ProjectActivity {
    private static SolutionExplorerModelNode lastNode = null;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        SwingUtilities.invokeLater(() -> {
            ProjectView projectView = ProjectView.getInstance(project);
            JTree tree = projectView.getCurrentProjectViewPane().getTree();
            if (tree == null) return;

            tree.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        tree.setToolTipText(null);
                        return;
                    }

                    Object userObject = path.getLastPathComponent();

                    if (!(userObject instanceof SolutionExplorerModelNode)) {
                        tree.setToolTipText(null);
                        return;
                    }

                    // Проверка: тот же узел и подсказка еще висит — ничего не делаем
                    if (userObject == lastNode && tree.getToolTipText(e) != null) {
                        return;
                    }

                    lastNode = (SolutionExplorerModelNode) userObject;
                    var virtualFile = lastNode.getVirtualFile();

                    if (virtualFile == null) {
                        tree.setToolTipText(null);
                        return;
                    }

                    var fileExtension = virtualFile.getExtension();
                    var state = AppSettings.getInstance().getState();

                    String tooltipText = null;
                    if (state.showCsprojDescription && "csproj".equals(fileExtension)) {
                        tooltipText = TooltipUtils.extractXmlTag(virtualFile, "Description");
                    } else if (state.showClassSummary && "cs".equals(fileExtension)) {
                        tooltipText = TooltipUtils.extractSummary(virtualFile, project);
                    }

                    if (tooltipText != null) {
                        String htmlTooltip = "<html><body>" +
                                tooltipText.substring(0, Math.min(tooltipText.length(), state.maxSymbols)) +
                                "</body></html>";
                        tree.setToolTipText(htmlTooltip);
                    } else {
                        tree.setToolTipText(null);
                    }

                }
            });
        });

        return Unit.INSTANCE;
    }
}

