package hsin.codepathcopy;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

final class CodePathNotifier {
    private CodePathNotifier() {
    }

    static void info(@Nullable Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("CodePathCopy")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }
}
