package hsin.codepathcopy;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.vfs.VirtualFile;

final class CodePathReference {
    private CodePathReference() {
    }

    static String build(Editor editor, SelectionModel selectionModel, VirtualFile file) {
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        int adjustedEndOffset = Math.max(startOffset, endOffset - 1);

        int startLine = editor.getDocument().getLineNumber(startOffset) + 1;
        int endLine = editor.getDocument().getLineNumber(adjustedEndOffset) + 1;

        return "@" + file.getPath() + "#L" + startLine + "-L" + endLine;
    }
}
