package com.jetbrains.rider.plugins.showprojecttreetooltips;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

final class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    public AppSettingsConfigurable() {}

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Tooltips in project tree";
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
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        return mySettingsComponent.getShowCsprojCheckboxValue() != state.showCsprojDescription ||
                mySettingsComponent.getCountOfSymbols() != state.maxSymbols ||
                mySettingsComponent.getShowSummaryCheckboxValue() != state.showClassSummary;
    }

    @Override
    public void apply() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        state.maxSymbols = mySettingsComponent.getCountOfSymbols();
        state.showClassSummary = mySettingsComponent.getShowSummaryCheckboxValue();
        state.showCsprojDescription = mySettingsComponent.getShowCsprojCheckboxValue();
    }

    @Override
    public void reset() {
        AppSettings.State state = Objects.requireNonNull(AppSettings.getInstance().getState());
        mySettingsComponent.setCountOfSymbols(state.maxSymbols);
        mySettingsComponent.setShowCsprojCheckboxValue(state.showCsprojDescription);
        mySettingsComponent.setShowSummaryCheckboxValue(state.showClassSummary);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}