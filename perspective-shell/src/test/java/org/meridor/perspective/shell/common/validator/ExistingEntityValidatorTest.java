package org.meridor.perspective.shell.common.validator;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.shell.common.validator.annotation.ExistingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.backend.EntityGenerator.*;
import static org.meridor.perspective.shell.common.validator.Entity.*;

@ContextConfiguration(locations = "/META-INF/spring/validator-context.xml")
@RunWith(Parameterized.class)
public class ExistingEntityValidatorTest {

    @Parameterized.Parameters(name = "Entity \"{0}\" validation should work")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {PROJECT, getProject().getId()},
                {IMAGE, getImage().getId()},
                {FLAVOR, getFlavor().getId()},
                {NETWORK, getNetwork().getId()},
                {KEYPAIR, getKeypair().getName()},
        });
    }

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ExistingEntityValidator validator;

    private final Entity entity;

    private final String id;
    
    public ExistingEntityValidatorTest(Entity entity, String id) {
        this.entity = entity;
        this.id = id;
    }
    
    @Test
    public void testValidate() {
        Object instance = new TestInstance();
        assertThat(validator.validate(instance, null, null), is(true));
        assertThat(validator.validate(
                instance,
                createAnnotation(entity),
                id
        ), is(true));
        assertThat(validator.validate(
                instance,
                createAnnotation(entity, 2, 3),
                id
        ), is(false));
    }
    
    private static ExistingEntity createAnnotation(Entity entity) {
        return createAnnotation(entity, 1, 1);
    }
    
    private static ExistingEntity createAnnotation(Entity entity, int minCount, int maxCount) {
        return new ExistingEntity() {

            @Override
            public Entity value() {
                return entity;
            }

            @Override
            public int maxCount() {
                return minCount;
            }

            @Override
            public int minCount() {
                return maxCount;
            }

            @Override
            public String projectField() {
                return "project";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ExistingEntity.class;
            }

        };
    }
    
    private static class TestInstance {
        private String project = getProject().getName();
    }
    
}