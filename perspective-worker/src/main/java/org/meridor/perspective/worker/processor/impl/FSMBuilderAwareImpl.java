package org.meridor.perspective.worker.processor.impl;

import org.meridor.perspective.worker.processor.FSMBuilderAware;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.YatomataImpl;

@Component
public class FSMBuilderAwareImpl implements FSMBuilderAware, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <T> Yatomata.Builder<T> get(Class<T> cls) {
        return new FSMBuilder<>(cls);
    }

    private <T> T getFSMInstance(Class<T> cls) {
        return applicationContext.getBean(cls);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public class FSMBuilder<T> implements Yatomata.Builder<T> {

        private final Class<T> cls;

        public FSMBuilder(Class<T> cls) {
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
