package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractProjectStep;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

public final class AnswersStorageUtils {

    public static String getProjectName(Class<? extends AbstractProjectStep> projectStepCls, AnswersStorage previousAnswers) {
        if (projectStepCls == null) {
            return null;
        }
        FindProjectsResult findProjectsResult = previousAnswers.get(projectStepCls, PROJECT, FindProjectsResult.class);
        return findProjectsResult != null ? findProjectsResult.getName() : null;
    }

}
