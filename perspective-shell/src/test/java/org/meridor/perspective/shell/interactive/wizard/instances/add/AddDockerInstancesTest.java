package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AddDockerInstancesTest extends AbstractAddInstancesTest {

    public AddDockerInstancesTest() {
        super(
                Arrays.asList(
                        "1",
                        "test-instance-$number",
                        "n",
                        "3",
                        "test-image",
                        "uptime"
                ),
                true,
                "add instances " +
                        "--project '^test-project - test-region$' " +
                        "--name test-instance-$number " +
                        "--image ^test-image$ " +
                        "--count 3 " +
                        "--options command=uptime"
        );
    }

    @Override
    protected CloudType getCloudType() {
        return CloudType.DOCKER;
    }

}