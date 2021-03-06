/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources.vertex;

import guru.bubl.module.model.FriendlyResource;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.subgraph.SubGraphJson;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.graph.subgraph.UserGraph;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OwnedSurroundGraphResource {

    private FriendlyResource centerBubble;
    private UserGraph userGraph;

    public OwnedSurroundGraphResource(
            UserGraph userGraph,
            FriendlyResource centerBubble
    ) {
        this.userGraph = userGraph;
        this.centerBubble = centerBubble;
    }

    @GET
    @Path("/")
    public Response get() {
        SubGraphPojo subGraphPojo;
        subGraphPojo = getGraph();
        if (!subGraphPojo.hasCenter(centerBubble.uri())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(
                SubGraphJson.toJson(
                        subGraphPojo
                )
        ).build();
    }

    private SubGraphPojo getGraph() {
        return userGraph.aroundForkUriInShareLevels(
                centerBubble.uri(),
                ShareLevel.allShareLevelsInt
        );
    }
}
