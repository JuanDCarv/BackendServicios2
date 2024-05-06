package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Carga;
import com.example.models.CargaDTO;
import com.example.models.Remision;
import com.example.models.RemisionDTO;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/cargas")
@Produces(MediaType.APPLICATION_JSON)
public class CargaService {

    @PersistenceContext(unitName = "CargasPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Métodos CRUD para Cargas (ya definidos)
 @POST
    @Path("/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarCarga(CargaDTO cargaDTO) {
        Carga carga = new Carga();
        carga.setFecha(cargaDTO.getFecha());
        carga.setPropietarioCarga(cargaDTO.getPropietarioCarga());
        carga.setOrigen(cargaDTO.getOrigen());
        carga.setDestino(cargaDTO.getDestino());
        carga.setDimensiones(cargaDTO.getDimensiones());
        carga.setPeso(cargaDTO.getPeso());
        carga.setValorAsegurado(cargaDTO.getValorAsegurado());
        carga.setEmpaque(cargaDTO.getEmpaque());

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(carga);
            entityManager.getTransaction().commit();
            entityManager.refresh(carga);
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
     
    @POST
    @Path("/{cargaId}/remisiones/agregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarRemision(@PathParam("cargaId") Long cargaId, RemisionDTO remisionDTO) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Remision remision = new Remision();
        remision.setFechaHoraRecogida(remisionDTO.getFechaHoraRecogida());
        remision.setOrigen(remisionDTO.getOrigen());
        remision.setDestino(remisionDTO.getDestino());
        remision.setPlacaCamion(remisionDTO.getPlacaCamion());
        remision.setConductor(remisionDTO.getConductor());
        remision.setRuta(remisionDTO.getRuta());

        try {
            entityManager.getTransaction().begin();
            carga.getRemisiones().add(remision); // Asociar remisión a la carga
            entityManager.persist(remision);
            entityManager.getTransaction().commit();
            entityManager.refresh(remision);
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

    // Métodos CRUD para Remisiones (operaciones de consulta, modificar y eliminar remisiones)
    @PUT
    @Path("/modificar/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarCarga(@PathParam("id") Long id, CargaDTO cargaDTO) {
        Carga carga = entityManager.find(Carga.class, id);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        carga.setFecha(cargaDTO.getFecha());
        carga.setPropietarioCarga(cargaDTO.getPropietarioCarga());
        carga.setOrigen(cargaDTO.getOrigen());
        carga.setDestino(cargaDTO.getDestino());
        carga.setDimensiones(cargaDTO.getDimensiones());
        carga.setPeso(cargaDTO.getPeso());
        carga.setValorAsegurado(cargaDTO.getValorAsegurado());
        carga.setEmpaque(cargaDTO.getEmpaque());

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(carga);
            entityManager.getTransaction().commit();
            entityManager.refresh(carga);
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
    
    @PUT
    @Path("/{cargaId}/remisiones/{remisionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificarRemision(@PathParam("cargaId") Long cargaId,
                                      @PathParam("remisionId") Long remisionId, RemisionDTO remisionDTO) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Remision remision = entityManager.find(Remision.class, remisionId);

        if (remision == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        remision.setFechaHoraRecogida(remisionDTO.getFechaHoraRecogida());
        remision.setOrigen(remisionDTO.getOrigen());
        remision.setDestino(remisionDTO.getDestino());
        remision.setPlacaCamion(remisionDTO.getPlacaCamion());
        remision.setConductor(remisionDTO.getConductor());
        remision.setRuta(remisionDTO.getRuta());

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(remision);
            entityManager.getTransaction().commit();
            entityManager.refresh(remision);
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
    public Response eliminarCarga(@PathParam("id") Long id) {
        Carga carga = entityManager.find(Carga.class, id);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            entityManager.getTransaction().begin();
            entityManager.remove(carga);
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
    
    @DELETE
    @Path("/{cargaId}/remisiones/{remisionId}")
    public Response eliminarRemision(@PathParam("cargaId") Long cargaId,
                                     @PathParam("remisionId") Long remisionId) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Remision remision = entityManager.find(Remision.class, remisionId);

        if (remision == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            entityManager.getTransaction().begin();
            carga.getRemisiones().remove(remision); // Desasociar remisión de la carga
            entityManager.remove(remision);
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
    public Response consultarCarga(@PathParam("id") Long id) {
        Carga carga = entityManager.find(Carga.class, id);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(carga).build();
    }

    @GET
    @Path("/consultarTodos")
    public Response consultarTodasLasCargas() {
        Query q = entityManager.createQuery("SELECT c FROM Carga c");
        List<Carga> cargas = q.getResultList();
        return Response.status(Response.Status.OK).entity(cargas).build();
    }
}