package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.request.FindImagesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindImagesResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.IMAGE;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorageUtils.getProjectName;

@Component
public abstract class AbstractImageStep extends SingleChoiceStep<FindImagesResult> {

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Override
    protected List<FindImagesResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return imagesRepository.findImages(
                requestProvider.get(FindImagesRequest.class).withProjects(getProjectName(getProjectStepClass(), previousAnswers))
        );
    }

    protected abstract Class<? extends AbstractProjectStep> getProjectStepClass();

    @Override
    protected Function<FindImagesResult, String> getAnswerProvider() {
        return FindImagesResult::getName;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindImagesResult> choicesStorage, String answer) {
        FindImagesResult image = choicesStorage.getChoicesMap().get(Integer.parseUnsignedInt(answer));
        answersStorage.put(getClass(), IMAGE, image);
    }
}
