package org.meridor.perspective.shell.interactive.wizard.images;

import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.wizard.AbstractWizardTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AddImagesWizardTest extends AbstractWizardTest<AddImagesWizard> {

    @Autowired
    private AddImagesWizard addImagesWizard;

    public AddImagesWizardTest() {
        super(
                Arrays.asList("1", "test-image"),
                true,
                "add images --instances test-instance --name test-image"
        );
    }

    @Override
    protected AddImagesWizard getWizard() {
        return addImagesWizard;
    }
}