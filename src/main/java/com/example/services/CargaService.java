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
 import com.example.PersistenceManager;
import com.example.models.AplicacionCarga;
import com.example.models.Carga;
import com.example.models.CargaDTO;
import com.example.models.Vehiculo;
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
        carga.setEstado("pendiente");

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(carga);
            entityManager.getTransaction().commit();
            entityManager.refresh(carga);
            
            // Notificar a los propietarios de vehículos
            notificarPropietariosVehiculos(carga);
            
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

    private void notificarPropietariosVehiculos(Carga carga) {
        // Lógica para notificar a los propietarios de vehículos
        Query q = entityManager.createQuery("SELECT v FROM Vehiculo v WHERE v.disponible = true AND v.capacidadCarga >= :capacidadCarga AND v.ubicacion = :ubicacion");
        q.setParameter("capacidadCarga", carga.getPeso()); // Suponiendo que la capacidad de carga y peso están en la misma unidad
        q.setParameter("ubicacion", carga.getOrigen());
        
        List<Vehiculo> vehiculos = q.getResultList();

        for (Vehiculo vehiculo : vehiculos) {
            
            System.out.println("Notificando al propietario del vehículo con placa: " + vehiculo.getPlaca());
        }

        // Actualizar el estado de la carga a "notificado"
        carga.setEstado("notificado");
        entityManager.getTransaction().begin();
        entityManager.merge(carga);
        entityManager.getTransaction().commit();
    }
    
    @POST
    @Path("/asignar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response asignarVehiculo(@QueryParam("cargaId") Long cargaId, @QueryParam("vehiculoId") Long vehiculoId) {
        Carga carga = entityManager.find(Carga.class, cargaId);
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, vehiculoId);

        if (carga == null || vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!vehiculo.isDisponible()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El vehículo no está disponible").build();
        }

        try {
            entityManager.getTransaction().begin();
            carga.setVehiculoAsignado(vehiculo);
            carga.setEstado("asignado");
            vehiculo.setDisponible(false);
            entityManager.merge(carga);
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

        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("/disponibles")
    public Response obtenerCargasDisponibles() {
        Query q = entityManager.createQuery("SELECT c FROM Carga c WHERE c.estado = 'notificado'");
        List<Carga> cargas = q.getResultList();
        return Response.status(Response.Status.OK).entity(cargas).build();
    }

    @POST
    @Path("/aplicar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aplicarACarga(@QueryParam("cargaId") Long cargaId, @QueryParam("vehiculoId") Long vehiculoId) {
        Carga carga = entityManager.find(Carga.class, cargaId);
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, vehiculoId);

        if (carga == null || vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            AplicacionCarga aplicacion = new AplicacionCarga();
            aplicacion.setCarga(carga);
            aplicacion.setVehiculo(vehiculo);
            aplicacion.setEstado("pendiente");

            entityManager.getTransaction().begin();
            entityManager.persist(aplicacion);
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

        return Response.status(Response.Status.CREATED).build();
    }
    /*
    @POST
    @Path("/elegir")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response elegirPropietario(@QueryParam("cargaId") Long cargaId, @QueryParam("vehiculoId") Long vehiculoId) {
        Carga carga = entityManager.find(Carga.class, cargaId);
        Vehiculo vehiculo = entityManager.find(Vehiculo.class, vehiculoId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
        }

        if (carga.getAplicaciones() == null || carga.getAplicaciones().isEmpty()) {
            // Si no hay interesados, cancelar la carga
            carga.setEstado("cancelado");
            entityManager.getTransaction().begin();
            entityManager.merge(carga);
            entityManager.getTransaction().commit();
            return Response.status(Response.Status.OK).entity("No hay interesados, la carga ha sido cancelada").build();
        }

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Vehículo no encontrado").build();
        }

        if (!vehiculo.isDisponible()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El vehículo no está disponible").build();
        }

        try {
            entityManager.getTransaction().begin();
            carga.setVehiculoAsignado(vehiculo);
            carga.setEstado("asignado");
            vehiculo.setDisponible(false);

            // Actualizar el estado de las aplicaciones a aceptada o rechazada
            for (AplicacionCarga aplicacion : carga.getAplicaciones()) {
                if (aplicacion.getVehiculo().getId().equals(vehiculoId)) {
                    aplicacion.setEstado("aceptada");
                } else {
                    aplicacion.setEstado("rechazada");
                }
                entityManager.merge(aplicacion);
            }

            entityManager.merge(carga);
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

        return Response.status(Response.Status.OK).build();
    }
    */
    @POST
@Path("/elegir")
@Consumes(MediaType.APPLICATION_JSON)
public Response elegirPropietario(@QueryParam("cargaId") Long cargaId, @QueryParam("vehiculoId") Long vehiculoId) {
    Carga carga = entityManager.find(Carga.class, cargaId);
    Vehiculo vehiculo = entityManager.find(Vehiculo.class, vehiculoId);

    if (carga == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
    }

    if (vehiculo == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("Vehículo no encontrado").build();
    }

    if (!vehiculo.isDisponible()) {
        return Response.status(Response.Status.BAD_REQUEST).entity("El vehículo no está disponible").build();
    }

    List<AplicacionCarga> aplicaciones = carga.getAplicaciones();
    if (aplicaciones == null || aplicaciones.isEmpty()) {
        // Si no hay interesados, cancelar la carga
        try {
            entityManager.getTransaction().begin();
            carga.setEstado("cancelado");
            entityManager.merge(carga);
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
        return Response.status(Response.Status.OK).entity("No hay interesados, la carga ha sido cancelada").build();
    }

    try {
        entityManager.getTransaction().begin();
        carga.setVehiculoAsignado(vehiculo);
        carga.setEstado("asignado");
        vehiculo.setDisponible(false);

        // Actualizar el estado de las aplicaciones a aceptada o rechazada
        for (AplicacionCarga aplicacion : aplicaciones) {
            if (aplicacion.getVehiculo().getId().equals(vehiculoId)) {
                aplicacion.setEstado("aceptada");
            } else {
                aplicacion.setEstado("rechazada");
            }
            entityManager.merge(aplicacion);
        }

        entityManager.merge(carga);
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

    return Response.status(Response.Status.OK).build();
}
    
    @POST
    @Path("/ruta")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response definirRuta(@QueryParam("cargaId") Long cargaId, String ruta) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
        }

        carga.setRuta(ruta);

        // Enviar mensaje al conductor
        enviarMensajeConductor(carga);

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(carga);
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

        return Response.status(Response.Status.OK).entity("Ruta definida con éxito").build();
    }

    private void enviarMensajeConductor(Carga carga) {
        // Lógica para enviar un mensaje al conductor con los detalles de la solicitud y la ruta
        String mensaje = "Detalles de la solicitud:\n" +
                "Origen: " + carga.getOrigen() + "\n" +
                "Destino: " + carga.getDestino() + "\n" +
                "Ruta: " + carga.getRuta();
        System.out.println("Mensaje enviado al conductor: " + mensaje);
    }
    
    /*
    @POST
    @Path("/remitir")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response generarRemision(@QueryParam("cargaId") Long cargaId, Remision remision) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
        }

        // Actualizar los campos de la carga
        carga.setRemitida(true);

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(carga);
            entityManager.persist(remision);
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

        return Response.status(Response.Status.OK).entity("Remisión generada con éxito").build();
    }
    */
    
    @POST
@Path("/remitir")
@Consumes(MediaType.APPLICATION_JSON)
public Response generarRemision(@QueryParam("cargaId") Long cargaId, Remision remision) {
    Carga carga = entityManager.find(Carga.class, cargaId);

    if (carga == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
    }

    try {
        entityManager.getTransaction().begin();
        entityManager.merge(carga);
        entityManager.persist(remision);
        // Actualizar los campos de la carga para reflejar la remisión
        carga.setRemitida(true);
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

    return Response.status(Response.Status.OK).entity("Remisión generada con éxito").build();
}
    
    @POST
    @Path("/entregar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reportarEntregaCarga(@QueryParam("cargaId") Long cargaId) {
        Carga carga = entityManager.find(Carga.class, cargaId);

        if (carga == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Carga no encontrada").build();
        }

        carga.setEntregada(true);

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(carga);
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

        return Response.status(Response.Status.OK).entity("Carga entregada correctamente").build();
    }

    @POST
    @Path("/cerrar-remision")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cerrarRemision(@QueryParam("remisionId") Long remisionId, int valoracionServicio, String comentarioValoracion) {
        Remision remision = entityManager.find(Remision.class, remisionId);

        if (remision == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Remisión no encontrada").build();
        }

        remision.setCerrada(true);
        remision.setValoracionServicio(valoracionServicio);
        remision.setComentarioValoracion(comentarioValoracion);

        try {
            entityManager.getTransaction().begin();
            entityManager.merge(remision);
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

        return Response.status(Response.Status.OK).entity("Remisión cerrada y valoración del servicio registrada").build();
    }
    
/*
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
     */
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