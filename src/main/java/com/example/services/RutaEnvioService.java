package com.example.services;

import com.example.PersistenceManager;
import com.example.models.RutaEnvio;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/rutas")
@Produces(MediaType.APPLICATION_JSON)
public class RutaEnvioService {

    @PersistenceContext(unitName = "RutasEnvioPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @POST
    @Path("/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarRuta(RutaEnvio rutaEnvio) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(rutaEnvio);
            entityManager.getTransaction().commit();
            entityManager.refresh(rutaEnvio);
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            entityManager.clear();
            entityManager.close();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/consultar/{id}")
    public Response consultarRuta(@PathParam("id") Long id) {
        RutaEnvio rutaEnvio = entityManager.find(RutaEnvio.class, id);

        if (rutaEnvio == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(rutaEnvio).build();
    }

    @GET
    @Path("/consultarTodos")
    public Response consultarTodasLasRutas() {
        Query q = entityManager.createQuery("SELECT r FROM RutaEnvio r");
        List<RutaEnvio> rutas = q.getResultList();
        return Response.status(Response.Status.OK).entity(rutas).build();
    }
}