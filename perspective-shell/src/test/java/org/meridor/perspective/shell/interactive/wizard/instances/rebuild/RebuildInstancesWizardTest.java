package org.meridor.perspective.shell.interactive.wizard.instances.rebuild;

import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.wizard.AbstractWizardTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildInstancesWizardTest extends AbstractWizardTest<RebuildInstancesWizard> {

    @Autowired
    private RebuildInstancesWizard rebuildInstancesWizard;

    public RebuildInstancesWizardTest() {
        super(
                Arrays.asList("1", "1"),
                true,
                "rebuild --instances test-instance --image test-image"
        );

    }

    @Override
    protected RebuildInstancesWizard getWizard() {
        return rebuildInstancesWizard;
    }
}