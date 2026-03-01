package com.telco.reconciliator.rest;

import com.telco.reconciliator.ejb.ReconciliationServiceBean;
import com.telco.reconciliator.model.ReconciliationResult;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/** REST endpoint for triggering and querying reconciliation runs. */
@Path("/reconciliation")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReconciliationResource {

    @EJB
    private ReconciliationServiceBean reconciliationService;

    /** Trigger reconciliation for all UNPROCESSED invoices. */
    @POST
    @Path("/run/all")
    public Response runAll() {
        List<ReconciliationResult> results = reconciliationService.reconcileAll();
        return Response.ok(results).build();
    }

    /** Trigger reconciliation for a single invoice by its primary key. */
    @POST
    @Path("/run/{invoiceId}")
    public Response runOne(@PathParam("invoiceId") Long invoiceId) {
        try {
            ReconciliationResult result = reconciliationService.reconcileInvoice(invoiceId);
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/results")
    public List<ReconciliationResult> getResults(@QueryParam("status") String status) {
        if (status != null && !status.isEmpty()) {
            return reconciliationService.findResultsByStatus(status.toUpperCase());
        }
        // Return all results across all statuses
        List<ReconciliationResult> all = new java.util.ArrayList<>();
        for (String s : new String[]{"MATCHED","OVERCHARGED","UNDERCHARGED","MISSING_CONSUMPTION","ERROR"}) {
            all.addAll(reconciliationService.findResultsByStatus(s));
        }
        return all;
    }

    @GET
    @Path("/stats")
    public Map<String, Object> getStats() {
        return reconciliationService.getStats();
    }
}
