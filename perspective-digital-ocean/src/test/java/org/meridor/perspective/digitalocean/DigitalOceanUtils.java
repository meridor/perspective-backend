package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.Account;
import com.myjeeva.digitalocean.pojo.Key;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Size;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class DigitalOceanUtils {

    static Integer getRandomInteger() {
        return ThreadLocalRandom.current().nextInt(1, 1000);
    }

    static Account getAccount() {
        return new Account() {
            {
                setDropletLimit(10);
                setFloatingIPLimit(3);
            }
        };
    }

    static Region getRegion(String name) {
        return new Region() {
            {
                setSlug(name);
                setName(name);
                setAvailable(true);
                setSizes(Collections.emptyList());
            }
        };
    }

    static Size getSize() {
        return new Size() {
            {
                setVirutalCpuCount(2);
                setDiskSize(100);
                setMemorySizeInMb(1024);
                setSlug("test-size");
                setAvailable(true);
            }
        };
    }

    static Key getKey() {
        return new Key() {
            {
                setName("test-key");
                setId(42);
                setPublicKey("test-public-key");
                setFingerprint("test-fingerprint");
            }
        };
    }
}
