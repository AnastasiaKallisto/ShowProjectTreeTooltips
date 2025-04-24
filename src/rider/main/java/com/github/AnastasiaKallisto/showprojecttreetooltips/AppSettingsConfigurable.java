package com.github.AnastasiaKallisto.showprojecttreetooltips;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;
    private AppSettings appSettingsInstance;

    public AppSettingsConfigurable() {
        appSettingsInstance = AppSettings.getInstance();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Project Tooltip Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettings.State state = Objects.requireNonNull(appSettingsInstance.getState());
        return mySettingsComponent.getShowCsprojCheckboxValue() != state.showCsprojDescription ||
                mySettingsComponent.getCountOfSymbols() != state.maxSymbols ||
                mySettingsComponent.getShowSummaryCheckboxValue() != state.showClassSummary;
    }

    @Override
    public void apply() {
        AppSettings.State state = Objects.requireNonNull(appSettingsInstance.getState());
        state.maxSymbols = mySettingsComponent.getCountOfSymbols();
        state.showClassSummary = mySettingsComponent.getShowSummaryCheckboxValue();
        state.showCsprojDescription = mySettingsComponent.getShowCsprojCheckboxValue();
    }

    @Override
    public void reset() {
        AppSettings.State state = Objects.requireNonNull(appSettingsInstance.getState());
        mySettingsComponent.setCountOfSymbols(state.maxSymbols);
        mySettingsComponent.setShowCsprojCheckboxValue(state.showCsprojDescription);
        mySettingsComponent.setShowSummaryCheckboxValue(state.showClassSummary);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}