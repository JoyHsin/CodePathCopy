package hsin.codepathcopy;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class CodePathSettingsConfigurable implements SearchableConfigurable {
    private final Project project;
    private JBTextField codexCommandField;
    private JBTextField codexDesktopBundleIdField;
    private JBCheckBox selectionHintCheckBox;
    private JSpinner startupDelaySpinner;
    private JSpinner desktopPasteDelaySpinner;

    public CodePathSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getId() {
        return "hsin.codepathcopy.settings";
    }

    @Override
    public @Nls String getDisplayName() {
        return "Code Path Copy";
    }

    @Override
    public @Nullable JComponent createComponent() {
        codexCommandField = new JBTextField();
        codexDesktopBundleIdField = new JBTextField();
        selectionHintCheckBox = new JBCheckBox("Show quick Codex button after selecting code");
        startupDelaySpinner = new JSpinner(new SpinnerNumberModel(1500, 300, 10000, 100));
        desktopPasteDelaySpinner = new JSpinner(new SpinnerNumberModel(500, 100, 5000, 100));

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Codex command:"), codexCommandField, 1, false)
                .addLabeledComponent(new JBLabel("Startup delay (ms):"), startupDelaySpinner, 1, false)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Codex Desktop bundle ID:"), codexDesktopBundleIdField, 1, false)
                .addLabeledComponent(new JBLabel("Desktop paste delay (ms):"), desktopPasteDelaySpinner, 1, false)
                .addComponent(selectionHintCheckBox, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        CodePathSettings settings = CodePathSettings.getInstance(project);
        return !settings.getCodexCommand().equals(codexCommandField.getText().trim())
                || settings.getStartupDelayMs() != (Integer) startupDelaySpinner.getValue()
                || !settings.getCodexDesktopBundleId().equals(codexDesktopBundleIdField.getText().trim())
                || settings.getDesktopPasteDelayMs() != (Integer) desktopPasteDelaySpinner.getValue()
                || settings.isSelectionHintEnabled() != selectionHintCheckBox.isSelected();
    }

    @Override
    public void apply() {
        CodePathSettings settings = CodePathSettings.getInstance(project);
        settings.setCodexCommand(codexCommandField.getText());
        settings.setStartupDelayMs((Integer) startupDelaySpinner.getValue());
        settings.setCodexDesktopBundleId(codexDesktopBundleIdField.getText());
        settings.setDesktopPasteDelayMs((Integer) desktopPasteDelaySpinner.getValue());
        settings.setSelectionHintEnabled(selectionHintCheckBox.isSelected());
    }

    @Override
    public void reset() {
        CodePathSettings settings = CodePathSettings.getInstance(project);
        codexCommandField.setText(settings.getCodexCommand());
        startupDelaySpinner.setValue(settings.getStartupDelayMs());
        codexDesktopBundleIdField.setText(settings.getCodexDesktopBundleId());
        desktopPasteDelaySpinner.setValue(settings.getDesktopPasteDelayMs());
        selectionHintCheckBox.setSelected(settings.isSelectionHintEnabled());
    }

    @Override
    public void disposeUIResources() {
        codexCommandField = null;
        codexDesktopBundleIdField = null;
        selectionHintCheckBox = null;
        startupDelaySpinner = null;
        desktopPasteDelaySpinner = null;
    }
}
