/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources.vertex;

import com.sun.jersey.api.client.ClientResponse;
import guru.bubl.module.model.UserUris;
import guru.bubl.module.model.friend.FriendStatus;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.subgraph.SubGraphJson;
import guru.bubl.module.model.graph.relation.Relation;
import guru.bubl.module.model.graph.subgraph.SubGraph;
import guru.bubl.module.model.graph.tag.Tag;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.service.SessionHandler;
import guru.bubl.service.utils.GraphManipulationRestTestUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class NotOwnedSurroundGraphResourceTest extends GraphManipulationRestTestUtils {

    @Test
    public void getting_graph_of_another_user_returns_correct_status() {
        vertexUtils().makePublicVertexWithUri(
                vertexBUri()
        );
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        assertThat(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB(), currentXsrfToken).getStatus(),
                is(
                        Response.Status.OK.getStatusCode()
                )
        );
    }

    @Test
    public void getting_graph_of_another_user_private_center_vertex_returns_not_found_status() {
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        assertThat(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexA(), currentXsrfToken).getStatus(),
                is(
                        Response.Status.NOT_FOUND.getStatusCode()
                )
        );
    }

    @Test
    public void error_404_when_trying_to_access_deleted_bubble() {
        URI vertexBUri = vertexB().uri();
        vertexUtils().removeVertexB();
        assertThat(
                graphUtils().getNonOwnedGraphOfCentralVertexWithUri(vertexBUri).getStatus(),
                is(
                        Response.Status.NOT_FOUND.getStatusCode()
                )
        );
    }

    @Test
    public void can_get_graph_of_another_user() {
        vertexUtils().makePublicVertexWithUri(
                vertexBUri()
        );
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertTrue(
                subGraph.vertices().containsKey(vertexBUri())
        );
    }

    @Test
    public void surround_vertices_have_to_be_public_to_be_included() {
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        vertexUtils().makePublicVertexWithUri(
                vertexBUri()
        );
        vertexUtils().makePrivateVertexWithUri(
                vertexCUri()
        );
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.vertices().size(),
                is(1)
        );
    }

    @Test
    public void surround_public_vertices_are_accessible() {
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        vertexUtils().makePublicVertexWithUri(
                vertexBUri()
        );
        vertexUtils().makePublicVertexWithUri(
                vertexCUri()
        );
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.vertices().size(),
                is(3)
        );
    }

    @Test
    public void owner_can_access_all_even_if_private() {
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        vertexUtils().makePrivateVertexWithUri(
                vertexBUri()
        );
        vertexUtils().makePrivateVertexWithUri(
                vertexCUri()
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.vertices().size(),
                is(3)
        );
    }

    @Test
    public void owner_has_to_provide_xsrf_token_to_access_private_bubbles() {
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        vertexUtils().makePrivateVertexWithUri(
                vertexBUri()
        );
        vertexUtils().makePrivateVertexWithUri(
                vertexCUri()
        );
        ClientResponse response = graphUtils().getNonOwnedGraphOfCentralVertex(vertexB(), UUID.randomUUID().toString());
        assertThat(
                response.getStatus(),
                is(Response.Status.NOT_FOUND.getStatusCode())
        );
    }

    @Test
    public void relations_related_to_private_vertices_are_absent() {
        graphElementUtils().makePublicGraphElementsWithUri(
                vertexAUri(),
                vertexBUri(),
                vertexCUri()
        );
        Relation relationBetweenAAndB = edgeUtils().edgeBetweenAAndB();
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertTrue(
                subGraph.hasEdgeWithUri(
                        relationBetweenAAndB.uri()
                )
        );
        vertexUtils().makePrivateVertexWithUri(vertexAUri());
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertex(vertexB()).getEntity(JSONObject.class)
        );
        assertFalse(
                subGraph.hasEdgeWithUri(
                        relationBetweenAAndB.uri()
                )
        );
    }

    @Test
    public void anonymous_users_can_get_it_too() {
        graphElementUtils().makePublicGraphElementsWithUri(
                vertexAUri(),
                vertexBUri(),
                vertexCUri()
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                getNonOwnedGraphOfCentralVertexNotAuthenticated(
                        vertexB()
                ).getEntity(JSONObject.class)
        );
        assertFalse(
                subGraph.vertices().isEmpty()
        );
    }

    @Test
    public void works_if_vertices_have_more_than_one_relation_to_each_other() {
        graphElementUtils().makePublicGraphElementsWithUri(
                vertexBUri()
        );
        graphElementUtils().makePrivateGraphElementsWithUri(
                vertexAUri(),
                vertexCUri()
        );
        edgeUtils().addRelationBetweenSourceAndDestinationVertexUri(
                vertexBUri(),
                vertexCUri()
        );
        assertThat(
                getNonOwnedGraphOfCentralVertexNotAuthenticated(
                        vertexB()
                ).getStatus(),
                is(
                        Response.Status.OK.getStatusCode()
                )
        );
    }

    @Test
    public void all_edges_are_removed_when_vertices_have_more_than_one_relation_to_each_other() {
        graphElementUtils().makePublicGraphElementsWithUri(
                vertexBUri()
        );
        graphElementUtils().makePrivateGraphElementsWithUri(
                vertexAUri(),
                vertexCUri()
        );
        edgeUtils().addRelationBetweenSourceAndDestinationVertexUri(
                vertexBUri(),
                vertexCUri()
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                getNonOwnedGraphOfCentralVertexNotAuthenticated(
                        vertexB()
                ).getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.vertices().size(),
                is(1)
        );
        assertThat(
                subGraph.edges().size(),
                is(0)
        );
    }

    @Test
    public void cannot_get_private_vertex_when_no_relations() {
        vertexUtils().removeVertexB();
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        authenticate(createAUser());
        ClientResponse response = graphUtils().getNonOwnedGraphOfCentralVertex(vertexA());
        assertThat(
                response.getStatus(),
                is(Response.Status.NOT_FOUND.getStatusCode())
        );
    }

    @Test
    public void can_get_graph_with_depth() {
        ClientResponse response = graphUtils().getNonOwnedGraphOfCentralVertexWithUriAtDepth(
                vertexAUri(),
                2
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                response
                        .getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.numberOfVertices(),
                is(3)
        );
    }

    @Test
    public void returns_graph_of_1_depth_when_not_specified() {
        ClientResponse response = getWithNoDepthSpecifiedForVertexUri(
                vertexAUri()
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                response
                        .getEntity(JSONObject.class)
        );
        assertThat(
                subGraph.numberOfVertices(),
                is(2)
        );
    }

    @Test
    public void getting_for_edge_returns_ok_status() {
        assertThat(
                getNonOwnedGraphOfEdgeBetweenAAndB().getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    @Test
    public void can_get_friend_bubbles() {
        vertexUtils().setShareLevel(
                vertexBUri(),
                ShareLevel.PUBLIC
        );
        vertexUtils().setShareLevel(
                vertexCUri(),
                ShareLevel.FRIENDS
        );
        vertexUtils().setShareLevel(
                vertexAUri(),
                ShareLevel.FRIENDS
        );
        String username = currentAuthenticatedUser.username();
        JSONObject anotherUser = createAUser();
        String otherUsername = anotherUser.optString("user_name");
        authenticate(anotherUser);
        userUtils().addFriend(
                otherUsername,
                username
        );
        SubGraph subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertexWithUri(vertexBUri()).getEntity(
                        JSONObject.class
                )
        );
        assertThat(
                subGraph.numberOfVertices(),
                is(1)
        );
        authenticate(defaultAuthenticatedUser);
        ClientResponse response = userUtils().addFriend(
                username,
                otherUsername
        );
        FriendStatus friendStatus = FriendStatus.valueOf(
                response.getEntity(JSONObject.class).optString("status")
        );
        assertThat(
                friendStatus,
                is(FriendStatus.confirmed)
        );
        authenticate(anotherUser);
        subGraph = SubGraphJson.fromJson(
                graphUtils().getNonOwnedGraphOfCentralVertexWithUri(vertexBUri()).getEntity(
                        JSONObject.class
                )
        );
        assertThat(
                subGraph.numberOfVertices(),
                is(3)
        );
    }

    @Test
    public void get_not_owned_surround_graph_tag_is_ok_status() {
        graphElementUtils().addFoafPersonTypeToVertexA();
        Tag tag = vertexA().getTags().values().iterator().next();
        assertThat(
                getNotOwnedSurroundGraphOfATag(tag.uri()).getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    private ClientResponse getNonOwnedGraphOfEdgeBetweenAAndB() {
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        vertexUtils().makePublicVertexWithUri(
                vertexBUri()
        );
        Relation relation = edgeUtils().edgeBetweenTwoVerticesUriGivenEdges(
                vertexAUri(),
                vertexBUri(),
                graphUtils().graphWithCenterVertexUri(vertexBUri()).edges()

        );
        String shortId = UserUris.graphElementShortId(
                relation.uri()
        );
        return resource
                .path(getUsersBaseUri(relation.getOwnerUsername()))
                .path("non_owned")
                .path("edge")
                .path(shortId)
                .path("surround_graph")
                .get(ClientResponse.class);
    }

    private ClientResponse getNonOwnedGraphOfCentralVertexNotAuthenticated(Vertex vertex) {
        return getNonOwnedGraphOfCentralVertexNotAuthenticatedWithDepth(
                vertex,
                1
        );
    }

    private ClientResponse getNonOwnedGraphOfCentralVertexNotAuthenticatedWithDepth(Vertex vertex, Integer depth) {
        String shortId = UserUris.graphElementShortId(
                vertex.uri()
        );
        return resource
                .path(getUsersBaseUri(vertex.getOwnerUsername()))
                .path("non_owned")
                .path("vertex")
                .path(shortId)
                .path("surround_graph")
                .queryParam("depth", depth.toString())
                .get(ClientResponse.class);
    }

    private ClientResponse getWithNoDepthSpecifiedForVertexUri(URI vertexUri) {
        String shortId = UserUris.graphElementShortId(
                vertexUri
        );
        return resource
                .path(getUsersBaseUri(UserUris.ownerUserNameFromUri(vertexUri)))
                .path("non_owned")
                .path("vertex")
                .path(shortId)
                .path("surround_graph")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .cookie(authCookie)
                .header(SessionHandler.X_XSRF_TOKEN, currentXsrfToken)
                .get(ClientResponse.class);
    }

    private ClientResponse getNotOwnedSurroundGraphOfATag(URI tagUri) {
        String shortId = UserUris.graphElementShortId(
                tagUri
        );
        return resource
                .path(getUsersBaseUri(UserUris.ownerUserNameFromUri(tagUri)))
                .path("non_owned")
                .path("meta")
                .path(shortId)
                .path("surround_graph")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .cookie(authCookie)
                .header(SessionHandler.X_XSRF_TOKEN, currentXsrfToken)
                .get(ClientResponse.class);
    }
}
