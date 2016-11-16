package org.meridor.perspective.shell.interactive.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChoicesStorage<T> {

    //Terminology: 
    // choice - a bean like project, image and so on to be used in wizard logic;
    // answer - a string to be used in final command
    private final Function<T, String> answerProvider;
    private final List<T> choices;

    public ChoicesStorage(Function<T, String> answerProvider, List<T> choices) {
        this.answerProvider = answerProvider;
        this.choices = choices;
    }

    public Map<Integer, String> getAnswersMap() {
        return getMapFromChoices(answerProvider);
    }

    public Map<Integer, T> getChoicesMap() {
        return getMapFromChoices(Function.identity());
    }

    private <O> Map<Integer, O> getMapFromChoices(Function<T, O> converter) {
        Map<Integer, O> map = new HashMap<>();
        for (int i = 1; i <= choices.size(); i++) {
            map.put(i, converter.apply(choices.get(i - 1)));
        }
        return map;
    }

}