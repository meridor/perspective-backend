package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.rest.services.ImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.rest.resources.ResponseUtils.notFound;

@Component
@Path("/images")
public class ImagesResource {

    @Autowired
    private ImagesService imagesService;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{imageId}")
    public Response getImageById(@PathParam("imageId") String imageId) {
        Optional<Image> image = imagesService.getImageById(imageId);
        return image.isPresent() ?
                Response.ok(image.get()).build() :
                notFound(String.format("Image with id = %s not found", imageId));
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveImages(List<Image> images) {
        imagesService.addImages(images);
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteImages(List<String> imageIds) {
        imagesService.deleteImages(imageIds);
        return Response.ok().build();
    }

}
