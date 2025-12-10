package rest;

import ejb.HitServiceBean;
import entity.HitResult;
import entity.User;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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