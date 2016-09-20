package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.rest.handler.Response;
import org.meridor.perspective.rest.services.ImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.meridor.perspective.rest.handler.Response.notFound;
import static org.meridor.perspective.rest.handler.Response.ok;

@Component
@Path("/images")
public class ImagesResource {

    private final ImagesService imagesService;

    @Autowired
    public ImagesResource(ImagesService imagesService) {
        this.imagesService = imagesService;
    }
    
    @GET
    @Path("/{imageId}")
    public Response getImageById(@PathParam("imageId") String imageId) {
        Optional<Image> image = imagesService.getImageById(imageId);
        return image.isPresent() ?
                ok(image.get()) :
                notFound(String.format("Image with id = %s not found", imageId));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response saveImages(List<Image> images) {
        imagesService.addImages(images);
        return ok();
    }

    @POST
    @Path("/delete")
    @Consumes(APPLICATION_JSON)
    public Response deleteImages(List<String> imageIds) {
        imagesService.deleteImages(imageIds);
        return ok();
    }

}
