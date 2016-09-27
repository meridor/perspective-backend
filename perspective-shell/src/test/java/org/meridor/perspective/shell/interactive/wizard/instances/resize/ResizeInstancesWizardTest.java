package org.meridor.perspective.shell.interactive.wizard.instances.resize;

import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.wizard.AbstractWizardTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ResizeInstancesWizardTest extends AbstractWizardTest<ResizeInstancesWizard> {

    @Autowired
    private ResizeInstancesWizard resizeInstancesWizard;

    public ResizeInstancesWizardTest() {
        super(
                Arrays.asList("1", "1"),
                true,
                "resize --instances test-instance --flavor test-flavor"
        );
    }

    @Override
    protected ResizeInstancesWizard getWizard() {
        return resizeInstancesWizard;
    }
}