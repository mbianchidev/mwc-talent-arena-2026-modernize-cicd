package com.telco.reconciliator.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/** JAX-RS application root; mounts all resources under /api. */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // Resource and provider classes are auto-discovered by CDI bean-discovery-mode=all
}
