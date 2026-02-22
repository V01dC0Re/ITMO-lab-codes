package com.example.app.rest;

import com.example.app.ejb.AuthServiceBean;
import com.example.app.entity.User;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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