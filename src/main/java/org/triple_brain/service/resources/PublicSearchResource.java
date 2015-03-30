/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service.resources;

import com.google.gson.Gson;
import org.triple_brain.module.model.graph.GraphTransactional;
import org.triple_brain.module.search.GraphSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/search")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.TEXT_PLAIN)
public class PublicSearchResource {

    private Gson gson = new Gson();

    @Inject
    GraphSearch graphSearch;

    @GET
    @Path("/")
    @GraphTransactional
    public Response search(@QueryParam("text") String searchText) {
        return Response.ok(
                gson.toJson(
                        graphSearch.searchPublicVerticesOnly(
                                searchText
                        )
                )
        ).build();
    }
}
