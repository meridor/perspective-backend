package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.screen.CountOrRangeScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.CountOrRangeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AddOpenstackInstancesRangeTest extends AbstractAddInstancesTest {

    @Autowired
    private AnswersStorage answersStorage;
    
    public AddOpenstackInstancesRangeTest() {
        super(
                Arrays.asList(
                        "1",
                        "test-instance-$number",
                        "y",
                        "1,3-5",
                        "1",
                        "1",
                        "1",
                        "1"
                ),
                true,
                "add instances " +
                        "--project '^test-project - test-region$' " +
                        "--name test-instance-$number " +
                        "--flavor ^test-flavor$ " +
                        "--image ^test-image$ " +
                        "--network ^test-network$ " +
                        "--keypair ^test-keypair$ " +
                        "--range 1,3-5"
        );
    }

    @Override
    protected CloudType getCloudType() {
        return CloudType.OPENSTACK;
    }

    @Override
    protected void beforeInjectingAnswer(Class<? extends WizardScreen> cls) {
        super.beforeInjectingAnswer(cls);
        if (CountOrRangeScreen.class.isAssignableFrom(cls)) {
            answersStorage.putAnswer(CountOrRangeStep.class, "y");
        }
    }
}