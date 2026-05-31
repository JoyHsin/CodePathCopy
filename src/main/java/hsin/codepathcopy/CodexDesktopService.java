package hsin.codepathcopy;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Alarm;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;

@Service(Service.Level.PROJECT)
final class CodexDesktopService {
    private final Project project;
    private final Alarm alarm;

    CodexDesktopService(Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
    }

    void send(String message) {
        if (!SystemInfo.isMac) {
            CodePathNotifier.info(project, "Codex Desktop sending is only supported on macOS.");
            return;
        }

        CopyPasteManager.getInstance().setContents(new StringSelection(message));
        CodePathSettings settings = CodePathSettings.getInstance(project);
        alarm.addRequest(() -> pasteIntoCodex(settings.getCodexDesktopBundleId(), settings.getDesktopPasteDelayMs()),
                0);
        CodePathNotifier.info(project, "Copied and sent to Codex Desktop: " + message);
    }

    private void pasteIntoCodex(String bundleId, int pasteDelayMs) {
        String script = "tell application id " + quoteAppleScript(bundleId) + " to activate\n"
                + "delay " + String.format("%.2f", pasteDelayMs / 1000.0) + "\n"
                + "tell application \"System Events\"\n"
                + "    keystroke \"v\" using command down\n"
                + "end tell\n";

        ProcessBuilder processBuilder = new ProcessBuilder("osascript", "-e", script);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                CodePathNotifier.info(project,
                        "Failed to paste into Codex Desktop. Check macOS Accessibility permission for IntelliJ IDEA.");
            }
        } catch (IOException ex) {
            CodePathNotifier.info(project, "Failed to run AppleScript: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            CodePathNotifier.info(project, "Sending to Codex Desktop was interrupted.");
        }
    }

    private String quoteAppleScript(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
