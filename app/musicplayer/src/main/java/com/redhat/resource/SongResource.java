package com.redhat.resource;

import com.redhat.model.Song;
import com.redhat.service.SongService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/songs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SongResource {

    @Inject
    SongService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        return Response.ok(service.listAll()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Long id) {
        Song song = service.findById(id).get();
        
        if (song == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(song).build();
    }

    @POST
    public Response create(Song song) {
        Song created = service.create(song);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean removed = service.delete(id);
        
        if (removed) {
            return Response.noContent().build();
        }
        
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}