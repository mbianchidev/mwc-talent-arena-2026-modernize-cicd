package com.telco.reconciliator.rest;

import com.telco.reconciliator.ejb.InvoiceServiceBean;
import com.telco.reconciliator.model.Invoice;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/** REST endpoint for invoice queries. */
@Path("/invoices")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvoiceResource {

    @EJB
    private InvoiceServiceBean invoiceService;

    @GET
    public List<Invoice> listAll(@QueryParam("customerId") String customerId,
                                 @QueryParam("reconciliationStatus") String recStatus) {
        if (customerId != null && !customerId.isEmpty()) {
            return invoiceService.findByCustomer(customerId);
        }
        if (recStatus != null && !recStatus.isEmpty()) {
            return invoiceService.findByReconciliationStatus(recStatus.toUpperCase());
        }
        return invoiceService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Invoice inv = invoiceService.findById(id);
        if (inv == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Invoice not found\"}").build();
        }
        return Response.ok(inv).build();
    }
}
