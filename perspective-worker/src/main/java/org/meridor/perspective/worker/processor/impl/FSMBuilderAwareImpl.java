package org.meridor.perspective.worker.processor.impl;

import org.meridor.perspective.worker.processor.FSMBuilderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

@Component
public class FSMBuilderAwareImpl implements FSMBuilderAware {

    private final ApplicationContext applicationContext;

    @Autowired
    public FSMBuilderAwareImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> Yatomata.Builder<T> get(Class<T> cls) {
        return new FSMBuilder<>(cls);
    }

    private <T> T getFSMInstance(Class<T> cls) {
        return applicationContext.getBean(cls);
    }

    private class FSMBuilder<T> implements Yatomata.Builder<T> {

        private final Class<T> cls;

        FSMBuilder(Class<T> cls) {
            this.cls = cls;
        }

        @Override
        public Yatomata<T> build(Object state) {
            try {
                if (state == null) {
                    return new YatomataImpl<>(cls, getFSMInstance(cls));
                }
                return new YatomataImpl<>(cls, getFSMInstance(cls), state);
            } catch (Exception e) {
                throw new RuntimeException("Could not initialize the FSM Engine for FSM " + cls.getCanonicalName(), e);
            }
        }

        @Override
        public Yatomata<T> build() {
            return build(null);
        }
    }
}
