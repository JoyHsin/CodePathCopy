package hsin.codepathcopy;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.util.Alarm;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;

@Service(Service.Level.PROJECT)
final class CodexTerminalService {
    private static final String TAB_NAME = "Codex";

    private final Project project;
    private final Alarm alarm;
    private ShellTerminalWidget terminalWidget;
    private boolean codexStarted;

    CodexTerminalService(Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
    }

    void send(String message) {
        showTerminalToolWindow();

        // 检查是否有可复用的终端widget
        if (terminalWidget != null && !Disposer.isDisposed(terminalWidget)) {
            // 复用现有终端
            if (!codexStarted) {
                startCodex(terminalWidget);
                codexStarted = true;
                alarm.addRequest(() -> sendToWidget(terminalWidget, message), startupDelayMs());
            } else {
                sendToWidget(terminalWidget, message);
            }
            return;
        }

        // 需要创建新终端
        TerminalView terminalView = TerminalView.getInstance(project);
        String basePath = project.getBasePath();
        if (basePath == null) {
            basePath = System.getProperty("user.home");
        }

        terminalWidget = terminalView.createLocalShellWidget(basePath, TAB_NAME);
        if (terminalWidget == null) {
            CodePathNotifier.info(project, "Terminal not available.");
            return;
        }

        // 新终端需要启动codex
        codexStarted = false;
        startCodex(terminalWidget);
        codexStarted = true;
        alarm.addRequest(() -> sendToWidget(terminalWidget, message), startupDelayMs());
    }

    private void showTerminalToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
        if (toolWindow != null) {
            toolWindow.activate(null, false);
        }
    }

    private void startCodex(ShellTerminalWidget widget) {
        // 延迟一小段时间，确保终端widget完全初始化
        alarm.addRequest(() -> {
            widget.executeWithTtyConnector(connector -> {
                try {
                    connector.write(CodePathSettings.getInstance(project).getCodexCommand() + "\n");
                } catch (IOException ex) {
                    CodePathNotifier.info(project, "Failed to start Codex: " + ex.getMessage());
                }
            });
        }, 300); // 300ms 延迟，确保终端准备就绪
    }

    private void sendToWidget(JBTerminalWidget widget, String message) {
        if (widget instanceof ShellTerminalWidget) {
            ShellTerminalWidget shell = (ShellTerminalWidget) widget;
            shell.executeWithTtyConnector(connector -> {
                try {
                    connector.write(message + "\n");
                } catch (IOException ex) {
                    CodePathNotifier.info(project, "Failed to send to terminal.");
                }
            });
        } else {
            widget.writePlainMessage(message + "\n");
        }
    }

    private int startupDelayMs() {
        return CodePathSettings.getInstance(project).getStartupDelayMs();
    }
}
