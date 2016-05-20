package org.meridor.perspective.rest.data.converters;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.rest.data.beans.ImageMetadata;
import org.meridor.perspective.rest.data.beans.ProjectImage;

import java.util.stream.Stream;

public final class ImageConverters {
    
    public static Stream<ImageMetadata> imageToMetadata(Image i) {
        return i.getMetadata().keySet().stream()
                .map(k -> new ImageMetadata(i.getId(), k.toString().toLowerCase(), i.getMetadata().get(k)));
    }
    
    public static Stream<ProjectImage> imageToProjectImages(Image i) {
        return i.getProjectIds().stream()
                .map(p -> new ProjectImage(p, i.getId()));
    }
    
    private ImageConverters() {
        
    }
    
}
