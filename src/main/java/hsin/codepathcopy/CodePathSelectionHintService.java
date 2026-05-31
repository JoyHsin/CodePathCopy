package hsin.codepathcopy;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.LightweightHint;
import com.intellij.util.IconUtil;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.util.Map;
import java.util.WeakHashMap;

@Service(Service.Level.PROJECT)
final class CodePathSelectionHintService implements SelectionListener {
    private static final int HINT_FLAGS = HintManager.HIDE_BY_TEXT_CHANGE
            | HintManager.HIDE_BY_SCROLLING
            | HintManager.HIDE_BY_CARET_MOVE
            | HintManager.HIDE_BY_ESCAPE;
    private static final int SHOW_DELAY_MS = 250;

    private final Project project;
    private final Alarm alarm;
    private final Map<Editor, LightweightHint> hints = new WeakHashMap<>();
    private final Map<Editor, TextRange> ranges = new WeakHashMap<>();
    private Editor pendingEditor;
    private TextRange pendingRange;
    private boolean mouseSelecting;

    CodePathSelectionHintService(Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
        EditorFactory.getInstance()
                .getEventMulticaster()
                .addSelectionListener(this, project);
        EditorFactory.getInstance()
                .getEventMulticaster()
                .addEditorMouseListener(new EditorMouseAdapter() {
                    @Override
                    public void mousePressed(@NotNull EditorMouseEvent event) {
                        mouseSelecting = true;
                        hide(event.getEditor());
                    }

                    @Override
                    public void mouseReleased(@NotNull EditorMouseEvent event) {
                        mouseSelecting = false;
                        if (pendingEditor != null && pendingRange != null) {
                            scheduleShow(pendingEditor, pendingRange);
                        }
                    }
                }, project);
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        if (!CodePathSettings.getInstance(project).isSelectionHintEnabled()) {
            hide(editor);
            clearPending(editor);
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        TextRange newRange = e.getNewRange();

        if (newRange == null || newRange.getLength() == 0 || !selectionModel.hasSelection()) {
            hide(editor);
            clearPending(editor);
            return;
        }

        TextRange lastRange = ranges.get(editor);
        if (newRange.equals(lastRange) && isHintVisible(editor)) {
            return;
        }
        ranges.put(editor, newRange);

        hide(editor);
        scheduleShow(editor, newRange);
    }

    private boolean isHintVisible(Editor editor) {
        LightweightHint hint = hints.get(editor);
        return hint != null && hint.isVisible();
    }

    private void show(Editor editor, SelectionModel selectionModel, VirtualFile file) {
        hide(editor);

        String reference = CodePathReference.build(editor, selectionModel, file);
        JComponent component = buildHintComponent(reference, editor);
        LightweightHint hint = new LightweightHint(component);
        hints.put(editor, hint);

        Point point = HintManagerImpl.getHintPosition(hint, editor,
                editor.offsetToVisualPosition(selectionModel.getSelectionStart()),
                HintManager.RIGHT_UNDER);

        HintManagerImpl.getInstanceImpl()
                .showEditorHint(hint, editor, point, HINT_FLAGS, 0, false);
    }

    private JComponent buildHintComponent(String reference, Editor editor) {
        JButton button = new JButton(scaleIcon(CodePathIcons.CODEX));
        button.setToolTipText("Send to Codex");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.addActionListener(event -> {
            CodexTerminalService service = project.getService(CodexTerminalService.class);
            if (service != null) {
                service.send(reference);
                CodePathNotifier.info(project, "Sent to Codex: " + reference);
            } else {
                CodePathNotifier.info(project, "Codex service not available.");
            }
            hide(editor);
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.add(button, BorderLayout.CENTER);
        return panel;
    }

    private Icon scaleIcon(Icon icon) {
        float scale = JBUI.scale(0.2f);
        return IconUtil.scale(icon, null, scale);
    }

    private void hide(Editor editor) {
        LightweightHint hint = hints.remove(editor);
        if (hint != null) {
            hint.hide();
        }
        ranges.remove(editor);
    }

    private void scheduleShow(Editor editor, TextRange range) {
        pendingEditor = editor;
        pendingRange = range;
        alarm.cancelAllRequests();
        if (mouseSelecting) {
            return;
        }
        alarm.addRequest(() -> showIfStable(editor, range), SHOW_DELAY_MS);
    }

    private void showIfStable(Editor editor, TextRange range) {
        if (editor.isDisposed()) {
            return;
        }
        if (!CodePathSettings.getInstance(project).isSelectionHintEnabled()) {
            hide(editor);
            clearPending(editor);
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            hide(editor);
            return;
        }

        TextRange currentRange = TextRange.create(selectionModel.getSelectionStart(),
                selectionModel.getSelectionEnd());
        if (!currentRange.equals(range)) {
            return;
        }

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null) {
            hide(editor);
            return;
        }

        show(editor, selectionModel, file);
    }

    private void clearPending(Editor editor) {
        if (pendingEditor == editor) {
            pendingEditor = null;
            pendingRange = null;
        }
        alarm.cancelAllRequests();
    }
}
