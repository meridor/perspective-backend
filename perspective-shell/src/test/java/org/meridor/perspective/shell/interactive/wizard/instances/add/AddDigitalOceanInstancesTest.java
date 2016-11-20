package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AddDigitalOceanInstancesTest extends AbstractAddInstancesTest {

    public AddDigitalOceanInstancesTest() {
        super(
                Arrays.asList(
                        "1",
                        "test-instance-$number",
                        "n",
                        "5",
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
                        "--keypair ^test-keypair$ " +
                        "--count 5"
        );
    }

    @Override
    protected CloudType getCloudType() {
        return CloudType.DIGITAL_OCEAN;
    }

}