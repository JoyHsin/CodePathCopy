package hsin.codepathcopy;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;

final class CodePathActionUtil {
    private CodePathActionUtil() {
    }

    static void updateSelectionAction(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(hasSelectedFileRange(e));
    }

    private static boolean hasSelectedFileRange(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        return editor != null && file != null && editor.getSelectionModel().hasSelection();
    }
}
