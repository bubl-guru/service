package org.triple_brain.service.vertex;

import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.triple_brain.module.common_utils.Uris;
import org.triple_brain.module.model.UserUris;
import org.triple_brain.module.model.json.LocalizedStringJson;
import org.triple_brain.module.model.json.graph.EdgeJson;
import org.triple_brain.module.model.json.graph.VertexJson;
import org.triple_brain.service.utils.GraphManipulationRestTest;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertTrue;
import static org.triple_brain.module.model.json.FriendlyResourceJson.COMMENT;
import static org.triple_brain.module.model.json.FriendlyResourceJson.LABEL;
import static org.triple_brain.module.model.json.StatementJsonFields.*;

/**
 * Copyright Mozilla Public License 1.1
 */

public class VertexResourceTest extends GraphManipulationRestTest {

    @Test
    public void adding_a_vertex_returns_correct_status() throws Exception {
        ClientResponse response = vertexUtils().addAVertexToVertexAWithUri(
                vertexAUri()
        );
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void can_add_a_vertex() throws Exception {
        int numberOfConnectedEdges = vertexUtils().connectedEdgesOfVertexWithURI(
                vertexAUri()
        ).length();
        vertexUtils().addAVertexToVertexAWithUri(vertexAUri());
        int updatedNumberOfConnectedEdges = vertexUtils().connectedEdgesOfVertexWithURI(
                vertexAUri()
        ).length();
        assertThat(updatedNumberOfConnectedEdges, is(numberOfConnectedEdges + 1));
    }

    @Test
    public void adding_a_vertex_returns_the_new_edge_and_vertex_id() throws Exception {
        ClientResponse response = vertexUtils().addAVertexToVertexAWithUri(vertexAUri());
        JSONObject createdStatement = response.getEntity(JSONObject.class);
        JSONObject subject = createdStatement.getJSONObject(SOURCE_VERTEX);
        assertThat(subject.getString(VertexJson.URI), is(vertexAUri().toString()));
        JSONObject newEdge = edgeUtils().edgeWithUri(
                Uris.get(
                        createdStatement.getJSONObject(EDGE).getString(
                                EdgeJson.URI
                        )
                )
        );
        JSONObject newVertex = vertexUtils().vertexWithUriOfCurrentUser(
                Uris.get(
                        createdStatement.getJSONObject(END_VERTEX).getString(
                                VertexJson.URI
                        )
                )
        );
        JSONArray edgesOfVertexA = vertexUtils().connectedEdgesOfVertexWithURI(
                vertexAUri()
        );
        assertTrue(edgeUtils().edgeIsInEdges(newEdge, edgesOfVertexA));
        assertTrue(vertexUtils().vertexWithUriHasDestinationVertexWithUri(
                vertexAUri(),
                vertexUtils().uriOfVertex(newVertex)
        ));
    }

    @Test
    public void cannot_add_a_vertex_that_user_doesnt_own() throws Exception {
        authenticate(createAUser());
        ClientResponse response = resource
                .path(
                        new UserUris(
                                defaultAuthenticatedUser
                        ).defaultVertexUri().getPath()
                )
                .cookie(authCookie)
                .post(ClientResponse.class);
        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void can_remove_a_vertex() throws Exception {
        assertTrue(graphElementWithIdExistsInCurrentGraph(
                vertexBUri()
        ));
        vertexUtils().removeVertexB();
        assertFalse(graphElementWithIdExistsInCurrentGraph(
                vertexBUri()
        ));
    }

    @Test
    public void removing_vertex_returns_correct_response_status() throws Exception {
        ClientResponse response = vertexUtils().removeVertexB();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void can_update_label() throws Exception {
        String vertexALabel = vertexA().getString(VertexJson.LABEL);
        assertThat(vertexALabel, is(not("new vertex label")));
        updateVertexALabelUsingRest("new vertex label");
        vertexALabel = vertexA().getString(VertexJson.LABEL);
        assertThat(vertexALabel, is("new vertex label"));
    }

    @Test
    public void can_update_note() throws Exception {
        String vertexANote = vertexA().getString(VertexJson.COMMENT);
        assertThat(vertexANote, is(not("some note")));
        vertexUtils().updateVertexANote("some note");
        vertexANote = vertexA().getString(VertexJson.COMMENT);
        assertThat(vertexANote, is("some note"));
    }

    @Test
    public void updating_label_returns_correct_status() throws Exception {
        ClientResponse response = updateVertexALabelUsingRest("new vertex label");
        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    private ClientResponse updateVertexALabelUsingRest(String label) throws Exception {
        JSONObject localizedLabel = new JSONObject().put(
                LocalizedStringJson.content.name(),
                label
        );
        ClientResponse response = resource
                .path(vertexAUri().getPath())
                .path("label")
                .cookie(authCookie)
                .post(ClientResponse.class, localizedLabel);
        return response;
    }

    @Test
    public void updating_note_updates_search() throws Exception {
        indexGraph();
        JSONObject resultsForA = searchUtils().searchOwnVerticesOnlyForAutoCompleteUsingRest(
                vertexA().getString(LABEL)
        ).getEntity(JSONArray.class).getJSONObject(0);
        Assert.assertThat(resultsForA.getString(COMMENT), Is.is(""));
        vertexUtils().updateVertexANote(
                "A description"
        );
        resultsForA = searchUtils().searchOwnVerticesOnlyForAutoCompleteUsingRest(
                vertexA().getString(LABEL)
        ).getEntity(JSONArray.class).getJSONObject(0);
        Assert.assertThat(resultsForA.getString(COMMENT), Is.is("A description"));
    }

    @Test
    public void when_deleting_a_vertex_its_relations_are_also_removed_from_search(){
        indexGraph();
        JSONArray relations = searchUtils().searchForRelations(
                "between",
                defaultAuthenticatedUserAsJson
        ).getEntity(JSONArray.class);
        Assert.assertThat(relations.length(), Is.is(2));
        vertexUtils().removeVertexB();
        relations = searchUtils().searchForRelations(
                "between",
                defaultAuthenticatedUserAsJson
        ).getEntity(JSONArray.class);
        Assert.assertThat(relations.length(), Is.is(0));
    }

    @Test
    public void making_vertex_public_re_indexes_it() throws Exception {
        indexGraph();
        JSONObject anotherUser = createAUser();
        authenticate(
                anotherUser
        );
        JSONArray results = searchUtils().searchOwnVerticesAndPublicOnesForAutoCompleteUsingRestAndUser(
                vertexA().getString(LABEL),
                anotherUser
        ).getEntity(JSONArray.class);
        Assert.assertThat(results.length(), Is.is(0));
        authenticate(defaultAuthenticatedUser);
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        authenticate(anotherUser);
        results = searchUtils().searchOwnVerticesAndPublicOnesForAutoCompleteUsingRestAndUser(
                vertexA().getString(LABEL),
                anotherUser
        ).getEntity(JSONArray.class);
        Assert.assertThat(results.length(), Is.is(1));
    }

    @Test
    public void making_vertex_private_re_indexes_it() throws Exception {
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        indexGraph();
        JSONObject anotherUser = createAUser();
        authenticate(anotherUser);
        JSONArray results = searchUtils().searchOwnVerticesAndPublicOnesForAutoCompleteUsingRestAndUser(
                vertexA().getString(LABEL),
                anotherUser
        ).getEntity(JSONArray.class);
        Assert.assertThat(results.length(), Is.is(greaterThan(0)));
        authenticate(defaultAuthenticatedUser);
        vertexUtils().makePrivateVertexWithUri(
                vertexAUri()
        );
        authenticate(anotherUser);
        results = searchUtils().searchOwnVerticesAndPublicOnesForAutoCompleteUsingRestAndUser(
                vertexA().getString(LABEL),
                anotherUser
        ).getEntity(JSONArray.class);
        Assert.assertThat(results.length(), Is.is(0));
    }

    @Test
    public void number_of_connected_vertices_are_included()throws Exception{
        assertThat(
                vertexB().getInt(VertexJson.NUMBER_OF_CONNECTED_EDGES),
                is(2)
        );
    }
}
