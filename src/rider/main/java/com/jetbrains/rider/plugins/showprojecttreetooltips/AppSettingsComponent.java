package com.jetbrains.rider.plugins.showprojecttreetooltips;

import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

final class AppSettingsComponent{
    private JPanel panel;
    private JCheckBox showCsprojCheckbox;
    private JCheckBox showSummaryCheckbox;
    private JBIntSpinner maxSymbolsSpinner;


    public AppSettingsComponent() {
        AppSettings settings = AppSettings.getInstance();

        // Сформируем панель с элементами управления
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Show description for .csproj:"),
                        showCsprojCheckbox = new JBCheckBox("", settings.getState().showCsprojDescription),
                        1, false)
                .addLabeledComponent(new JBLabel("Show summary for .cs:"),
                        showSummaryCheckbox = new JBCheckBox("", settings.getState().showClassSummary),
                        1, false)
                .addLabeledComponent(new JBLabel("Max count of symbols in tooltip:"),
                        maxSymbolsSpinner = new JBIntSpinner(settings.getState().maxSymbols, 30, 500),
                        1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public JComponent getPreferredFocusedComponent() {
        return showCsprojCheckbox;
    }

    public boolean getShowCsprojCheckboxValue() {
        return showCsprojCheckbox.isSelected();
    }

    public void setShowCsprojCheckboxValue(boolean value) {
        this.showCsprojCheckbox.setSelected(value);
    }

    public boolean getShowSummaryCheckboxValue() {
        return showSummaryCheckbox.isSelected();
    }

    public void setShowSummaryCheckboxValue(boolean value) {
        this.showSummaryCheckbox.setSelected(value);
    }

    public int getCountOfSymbols() {
        return maxSymbolsSpinner.getNumber();
    }

    public void setCountOfSymbols(int countOfSymbols) {
        this.maxSymbolsSpinner.setNumber(countOfSymbols);
    }
}

