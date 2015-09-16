package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import static org.meridor.perspective.mock.EntityGenerator.getErrorInstance;
import static org.meridor.perspective.mock.EntityGenerator.getImage;
import static org.meridor.perspective.mock.EntityGenerator.getInstance;

@Component
public class ImagesStorage extends HashSet<Image> {

    public ImagesStorage() {
        add(getImage());
    }

}
