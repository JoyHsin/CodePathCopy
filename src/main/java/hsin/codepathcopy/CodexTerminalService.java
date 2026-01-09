package hsin.codepathcopy;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.Alarm;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;

@Service(Service.Level.PROJECT)
final class CodexTerminalService {
    private static final String TAB_NAME = "Codex";
    private static final int STARTUP_DELAY_MS = 800;

    private final Project project;
    private final Alarm alarm;
    private ShellTerminalWidget terminalWidget;
    private boolean codexStarted;

    CodexTerminalService(Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    }

    void send(String message) {
        ShellTerminalWidget widget = getOrCreateTerminal();
        if (widget == null) {
            CodePathNotifier.info(project, "Terminal not available.");
            return;
        }

        showTerminalToolWindow();

        if (!codexStarted) {
            execute(widget, "codex");
            codexStarted = true;
            alarm.addRequest(() -> execute(widget, message), STARTUP_DELAY_MS);
        } else {
            execute(widget, message);
        }
    }

    private ShellTerminalWidget getOrCreateTerminal() {
        if (terminalWidget != null && !Disposer.isDisposed(terminalWidget)) {
            return terminalWidget;
        }

        TerminalView terminalView = TerminalView.getInstance(project);
        String basePath = project.getBasePath();
        if (basePath == null) {
            basePath = System.getProperty("user.home");
        }
        terminalWidget = terminalView.createLocalShellWidget(basePath, TAB_NAME);
        codexStarted = false;
        return terminalWidget;
    }

    private void showTerminalToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
        if (toolWindow != null) {
            toolWindow.activate(null, false);
        }
    }

    private void execute(ShellTerminalWidget widget, String command) {
        try {
            widget.executeCommand(command);
        } catch (Exception ex) {
            CodePathNotifier.info(project, "Terminal busy, try again.");
        }
    }
}
