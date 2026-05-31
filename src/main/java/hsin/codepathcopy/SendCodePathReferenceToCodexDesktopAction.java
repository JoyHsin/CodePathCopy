package hsin.codepathcopy;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SendCodePathReferenceToCodexDesktopAction extends AnAction implements DumbAware {
    @Override
    public void update(AnActionEvent e) {
        CodePathActionUtil.updateSelectionAction(e);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || editor == null || file == null) {
            CodePathNotifier.info(project, "No active editor or file.");
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            CodePathNotifier.info(project, "Select code first.");
            return;
        }

        String reference = CodePathReference.build(editor, selectionModel, file);
        CodexDesktopService service = project.getService(CodexDesktopService.class);
        if (service == null) {
            CodePathNotifier.info(project, "Codex Desktop service not available.");
            return;
        }

        service.send(reference);
    }
}
