package hsin.codepathcopy;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

public class CodePathStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(Project project) {
        project.getService(CodePathSelectionHintService.class);
    }
}
