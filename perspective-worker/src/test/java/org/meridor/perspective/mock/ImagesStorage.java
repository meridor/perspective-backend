package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Image;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class ImagesStorage extends HashSet<Image> {

    public ImagesStorage() {
        add(EntityGenerator.getImage());
    }

}
