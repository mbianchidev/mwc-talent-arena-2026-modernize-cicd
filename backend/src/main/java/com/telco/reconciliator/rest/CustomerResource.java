package com.telco.reconciliator.rest;

import com.telco.reconciliator.ejb.CustomerServiceBean;
import com.telco.reconciliator.model.Customer;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/** REST endpoint exposing customer management operations. */
@Path("/customers")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @EJB
    private CustomerServiceBean customerService;

    @GET
    public List<Customer> listAll() {
        return customerService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Customer c = customerService.findById(id);
        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Customer not found\"}").build();
        }
        return Response.ok(c).build();
    }

    @GET
    @Path("/status/{status}")
    public List<Customer> getByStatus(@PathParam("status") String status) {
        return customerService.findByStatus(status.toUpperCase());
    }
}
