package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AnswersStorage {

    private final Map<Class<? extends Step>, Map<AnswersStorageKey, Object>> storage = new HashMap<>();

    public String getAnswer(Class<? extends Step> step) {
        return get(step, AnswersStorageKey.ANSWER, String.class);
    }

    public void putAnswer(Class<? extends Step> step, String value) {
        put(step, AnswersStorageKey.ANSWER, value);
    }

    public <T> T get(Class<? extends Step> step, AnswersStorageKey key, Class<T> cls) {
        return cls.cast(getStepMap(step).get(key));
    }

    public void put(Class<? extends Step> step, AnswersStorageKey key, Object value) {
        getStepMap(step).put(key, value);
    }

    private Map<AnswersStorageKey, Object> getStepMap(Class<? extends Step> step) {
        storage.putIfAbsent(step, new HashMap<>());
        return storage.get(step);
    }

    public void clear() {
        storage.clear();
    }

    public enum AnswersStorageKey {
        ANSWER,
        FLAVOR,
        IMAGE,
        INSTANCES,
        KEYPAIR,
        NETWORK,
        PROJECT
    }

}
