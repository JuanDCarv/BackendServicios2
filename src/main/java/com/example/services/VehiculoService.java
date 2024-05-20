package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Vehiculo;
import com.example.models.VehiculoDTO;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/vehiculos")
@Produces(MediaType.APPLICATION_JSON)
public class VehiculoService {

    @PersistenceContext(unitName = "VehiculosPU")
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
public Response agregarVehiculo(VehiculoDTO vehiculoDTO) {
    Vehiculo vehiculo = new Vehiculo();
    vehiculo.setPlaca(vehiculoDTO.getPlaca());
    vehiculo.setMarca(vehiculoDTO.getMarca());
    vehiculo.setModelo(vehiculoDTO.getModelo());
    vehiculo.setCapacidadCarga(vehiculoDTO.getCapacidadCarga());
    vehiculo.setTipoCarroceria(vehiculoDTO.getTipoCarroceria());
    vehiculo.setDisponible(true); 

    try {
        entityManager.getTransaction().begin();
        entityManager.persist(vehiculo);
        entityManager.getTransaction().commit();
        entityManager.refresh(vehiculo);
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

/*
    @POST
    @Path("/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarVehiculo(VehiculoDTO vehiculoDTO) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(vehiculoDTO.getPlaca());
        vehiculo.setMarca(vehiculoDTO.getMarca());
        vehiculo.setModelo(vehiculoDTO.getModelo());
        vehiculo.setCapacidadCarga(vehiculoDTO.getCapacidadCarga());
        vehiculo.setTipoCarroceria(vehiculoDTO.getTipoCarroceria());

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(vehiculo);
            entityManager.getTransaction().commit();
            entityManager.refresh(vehiculo);
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
*/
    
    @POST
    @Path("/reporte")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enviarReporteDiario(@QueryParam("vehiculoId") Long vehiculoId, String ubicacionActual, String novedades) {
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, vehiculoId);

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Vehículo no encontrado").build();
        }

        vehiculo.setUbicacionActual(ubicacionActual);
        vehiculo.setNovedades(novedades);

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(vehiculo);
            entityManager.getTransaction().commit();
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

        return Response.status(Response.Status.OK).entity("Reporte diario enviado con éxito").build();
    }
    
    @PUT
    @Path("/modificar/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarVehiculo(@PathParam("id") Long id, VehiculoDTO vehiculoDTO) {
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, id);

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        vehiculo.setPlaca(vehiculoDTO.getPlaca());
        vehiculo.setMarca(vehiculoDTO.getMarca());
        vehiculo.setModelo(vehiculoDTO.getModelo());
        vehiculo.setCapacidadCarga(vehiculoDTO.getCapacidadCarga());
        vehiculo.setTipoCarroceria(vehiculoDTO.getTipoCarroceria());

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(vehiculo);
            entityManager.getTransaction().commit();
            entityManager.refresh(vehiculo);
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

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/eliminar/{id}")
    public Response eliminarVehiculo(@PathParam("id") Long id) {
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, id);

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            entityManager.getTransaction().begin();
            entityManager.remove(vehiculo);
            entityManager.getTransaction().commit();
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

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/consultar/{id}")
    public Response consultarVehiculo(@PathParam("id") Long id) {
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, id);

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(vehiculo).build();
    }

    @GET
    @Path("/consultarTodos")
    public Response consultarTodosLosVehiculos() {
        Query q = entityManager.createQuery("SELECT v FROM Vehiculo v");
        List<Vehiculo> vehiculos = q.getResultList();
        return Response.status(Response.Status.OK).entity(vehiculos).build();
    }
}