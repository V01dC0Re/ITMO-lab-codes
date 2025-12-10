package com.example.app.rest;

import com.example.app.ejb.HitServiceBean;
import com.example.app.entity.HitResult;
import com.example.app.entity.User;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/hits")
public class HitResource {
    @EJB
    private HitServiceBean hitService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response checkHit(
            @Context HttpSession session,
            HitResult request
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Response.status(401).build();
        }

        // Валидация входных данных
        if (request.getY() < -5 || request.getY() > 3 || request.getR() <= 0) {
            return Response.status(400).entity("Invalid parameters").build();
        }

        boolean hit = hitService.checkHit(request.getX(), request.getY(), request.getR());
        HitResult result = hitService.saveResult(
                request.getX(),
                request.getY(),
                request.getR(),
                hit,
                user
        );

        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory(@Context HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Response.status(401).build();
        }

        List<HitResult> history = hitService.getUserHistory(user);
        return Response.ok(history).build();
    }
}