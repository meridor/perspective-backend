package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AddOpenstackInstancesCountTest extends AbstractAddInstancesTest {

    public AddOpenstackInstancesCountTest() {
        super(
                Arrays.asList(
                        "1",
                        "test-instance-$number",
                        "n",
                        "5",
                        "1",
                        "1",
                        "1",
                        "1" //Some answers are selected automatically and thus not printed...
                ),
                true,
                "add instances " +
                        "--project '^test-project - test-region$' " +
                        "--name test-instance-$number " +
                        "--flavor ^test-flavor$ " +
                        "--image ^test-image$ " +
                        "--network ^test-network$ " +
                        "--keypair ^test-keypair$ " +
                        "--count 5"
        );
    }

    @Override
    protected CloudType getCloudType() {
        return CloudType.OPENSTACK;
    }

}