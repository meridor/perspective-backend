package org.meridor.perspective.worker.processor;

import ru.yandex.qatools.fsm.Yatomata;

public interface FSMBuilderAware {

    <T> Yatomata.Builder<T> get(Class<T> cls);
    
}
