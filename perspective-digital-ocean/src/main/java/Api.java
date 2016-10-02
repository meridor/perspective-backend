import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Images;
import com.myjeeva.digitalocean.pojo.Sizes;

import java.util.Set;

public interface Api {
    
    // Project operations
    
    Set<String> listRegions();
    
    Sizes listSizes();
    
    // Instance operations
    
    String addDroplet(Droplet droplet);

    boolean deleteDroplet(String dropletId);

    boolean startDroplet(String dropletId);

    boolean shutdownDroplet(String dropletId);

    boolean rebootDroplet(String dropletId);

    boolean hardRebootDroplet(String dropletId);

    boolean resizeDroplet(String dropletId, String flavorId);

    boolean rebuildDroplet(String dropletId, String imageId);

    boolean pauseDroplet(String dropletId);

    boolean suspendDroplet(String dropletId);

    boolean resumeDroplet(String dropletId);
        
    // Image operations
    
    Images listImages();
    
    String addImage(String dropletId, String imageName);
    
    boolean deleteImage(String imageId);
}
