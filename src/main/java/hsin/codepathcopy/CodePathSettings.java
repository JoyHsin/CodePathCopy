package hsin.codepathcopy;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@State(name = "CodePathCopySettings", storages = @Storage("codePathCopy.xml"))
public final class CodePathSettings implements PersistentStateComponent<CodePathSettings.State> {
    private final State state = new State();

    static CodePathSettings getInstance(Project project) {
        return project.getService(CodePathSettings.class);
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State loadedState) {
        XmlSerializerUtil.copyBean(loadedState, state);
    }

    String getCodexCommand() {
        String command = state.codexCommand == null ? "" : state.codexCommand.trim();
        return command.isEmpty() ? State.DEFAULT_CODEX_COMMAND : command;
    }

    void setCodexCommand(String codexCommand) {
        String command = codexCommand == null ? "" : codexCommand.trim();
        state.codexCommand = command.isEmpty() ? State.DEFAULT_CODEX_COMMAND : command;
    }

    int getStartupDelayMs() {
        return Math.max(State.MIN_STARTUP_DELAY_MS, state.startupDelayMs);
    }

    void setStartupDelayMs(int startupDelayMs) {
        state.startupDelayMs = Math.max(State.MIN_STARTUP_DELAY_MS, startupDelayMs);
    }

    String getCodexDesktopBundleId() {
        String bundleId = state.codexDesktopBundleId == null ? "" : state.codexDesktopBundleId.trim();
        return bundleId.isEmpty() ? State.DEFAULT_CODEX_DESKTOP_BUNDLE_ID : bundleId;
    }

    void setCodexDesktopBundleId(String codexDesktopBundleId) {
        String bundleId = codexDesktopBundleId == null ? "" : codexDesktopBundleId.trim();
        state.codexDesktopBundleId = bundleId.isEmpty() ? State.DEFAULT_CODEX_DESKTOP_BUNDLE_ID : bundleId;
    }

    int getDesktopPasteDelayMs() {
        return Math.max(State.MIN_DESKTOP_PASTE_DELAY_MS, state.desktopPasteDelayMs);
    }

    void setDesktopPasteDelayMs(int desktopPasteDelayMs) {
        state.desktopPasteDelayMs = Math.max(State.MIN_DESKTOP_PASTE_DELAY_MS, desktopPasteDelayMs);
    }

    boolean isSelectionHintEnabled() {
        return state.selectionHintEnabled;
    }

    void setSelectionHintEnabled(boolean selectionHintEnabled) {
        state.selectionHintEnabled = selectionHintEnabled;
    }

    public static final class State {
        static final String DEFAULT_CODEX_COMMAND = "codex";
        static final String DEFAULT_CODEX_DESKTOP_BUNDLE_ID = "com.openai.codex";
        static final int MIN_STARTUP_DELAY_MS = 300;
        static final int MIN_DESKTOP_PASTE_DELAY_MS = 100;

        public String codexCommand = DEFAULT_CODEX_COMMAND;
        public String codexDesktopBundleId = DEFAULT_CODEX_DESKTOP_BUNDLE_ID;
        public boolean selectionHintEnabled = true;
        public int startupDelayMs = 1500;
        public int desktopPasteDelayMs = 500;
    }
}
