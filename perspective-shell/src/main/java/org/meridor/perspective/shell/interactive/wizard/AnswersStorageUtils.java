package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

public final class AnswersStorageUtils {

    public static String getProjectName(AnswersStorage previousAnswers) {
        FindProjectsResult findProjectsResult = previousAnswers.get(ProjectStep.class, PROJECT, FindProjectsResult.class);
        return findProjectsResult != null ? findProjectsResult.getName() : null;
    }

}
