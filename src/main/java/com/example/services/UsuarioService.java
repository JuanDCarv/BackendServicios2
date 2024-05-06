package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Usuario;
import com.example.models.UsuarioDTO;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONObject;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioService {

    private EntityManager entityManager;

    public UsuarioService() {
        // Obtener el EntityManager desde PersistenceManager
        entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
    }

    @POST
    @Path("/registrar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarUsuario(UsuarioDTO usuarioDTO) {
        JSONObject respuesta = new JSONObject();
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setCorreo(usuarioDTO.getCorreo());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(usuario);
            entityManager.getTransaction().commit();
            entityManager.refresh(usuario);
            respuesta.put("usuario_id", usuario.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            usuario = null;
        } finally {
            entityManager.clear();
        }
        
        return Response.status(200).entity(respuesta).build();
    }

    @GET
    @Path("/consultar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultarUsuarios() {
        Query consulta = entityManager.createQuery("SELECT u FROM Usuario u");
        return Response.status(200).entity(consulta.getResultList()).build();
    }
}