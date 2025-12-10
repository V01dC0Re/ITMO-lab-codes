package rest;

import ejb.AuthServiceBean;
import entity.User;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
public class AuthResource {
    @EJB
    private AuthServiceBean authService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("login") String login,
            @FormParam("password") String password,
            @Context HttpSession session
    ) {
        User user = authService.authenticate(login, password);
        if (user == null) {
            return Response.status(401).entity("Invalid credentials").build();
        }

        session.setAttribute("user", user);
        return Response.ok().build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpSession session) {
        session.invalidate();
        return Response.ok().build();
    }
}