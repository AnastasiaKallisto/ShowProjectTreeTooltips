package com.github.AnastasiaKallisto.showprojecttreetooltips;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(
        name = "com.jetbrains.rider.plugins.showprojecttreetooltips.AppSettings",
        storages = {@Storage("ShowProjectTreeTooltips.xml")}
)
final class AppSettings implements PersistentStateComponent<AppSettings.State> {

    static class State {
        public boolean showCsprojDescription = true;
        public boolean showClassSummary = true;
        public int maxSymbols = 200;
    }

    private State myState = new State();

    public static AppSettings getInstance() {
        return ServiceManager.getService(AppSettings.class);
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }
}

