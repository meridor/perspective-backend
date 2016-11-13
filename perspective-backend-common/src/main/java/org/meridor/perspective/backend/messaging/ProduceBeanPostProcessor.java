package org.meridor.perspective.backend.messaging;

import org.meridor.perspective.backend.messaging.impl.ProducerImpl;
import org.meridor.perspective.backend.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class ProduceBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProduceBeanPostProcessor.class);

    private final Storage storage;

    @Autowired
    public ProduceBeanPostProcessor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class cls = bean.getClass();
        String className = cls.getCanonicalName();
        ReflectionUtils.doWithFields(
                cls,
                f -> {
                    LOG.debug("Injecting producer to field {} of bean {}", f.getName(), className);
                    ReflectionUtils.makeAccessible(f);
                    Producer producer;
                    if (f.isAnnotationPresent(Destination.class)) {
                        Destination annotation = f.getAnnotation(Destination.class);
                        String queueName = annotation.value().value();
                        producer = new ProducerImpl(queueName, storage);
                    } else {
                        producer = new ProducerImpl(className, storage);
                    }
                    if (AopUtils.isAopProxy(bean)) {
                        Advised advisedBean = (Advised) bean;
                        try {
                            Object originalBean = advisedBean.getTargetSource().getTarget();
                            ReflectionUtils.setField(f, originalBean, producer);
                        } catch (Exception e) {
                            LOG.debug("Failed to inject producer to AOP proxied bean {}", bean);
                        }
                    } else {
                        ReflectionUtils.setField(f, bean, producer);
                    }
                },
                f -> Producer.class.isAssignableFrom(f.getType())
        );
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
