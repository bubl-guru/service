package org.triple_brain.service.utils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.triple_brain.module.model.User;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/*
* Copyright Mozilla Public License 1.1
*/
public class GraphRestTestUtils {

    private WebResource resource;
    private NewCookie authCookie;

    private VertexRestTestUtils vertexUtils;

    static JSONObject vertexA;
    static JSONObject vertexB;
    static JSONObject vertexC;
    private User authenticatedUser;
    public static GraphRestTestUtils withWebResourceAndAuthCookie(WebResource resource, NewCookie authCookie, User authenticatedUser){
        return new GraphRestTestUtils(resource, authCookie, authenticatedUser);
    }
    protected GraphRestTestUtils(WebResource resource, NewCookie authCookie, User authenticatedUser){
        this.resource = resource;
        this.authCookie = authCookie;
        this.authenticatedUser = authenticatedUser;
        vertexUtils = VertexRestTestUtils.withWebResourceAndAuthCookie(
                resource,
                authCookie,
                authenticatedUser
        );
    }

    public JSONObject wholeGraph(){
        ClientResponse response = resource
                .path(vertexAUri().getPath())
                .path("surround_graph")
                .path(GraphManipulationRestTest
                        .DEPTH_OF_SUB_VERTICES_COVERING_ALL_GRAPH_VERTICES
                        .toString()
                )
                .cookie(authCookie)
                .get(ClientResponse.class);
        assertThat(
                response.getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
        return response.getEntity(JSONObject.class);
    }

    public JSONArray makeGraphHave3SerialVerticesWithLongLabels() {
        return makeGraphHave3SerialVerticesWithLongLabelsUsingCookie(
                authCookie
        );
    }

    public JSONArray makeGraphHave3SerialVerticesWithLongLabelsUsingCookie(NewCookie authCookie) {
        ClientResponse response = resource
                .path("service")
                .path("users")
                .path("test")
                .path("make_graph_have_3_serial_vertices_with_long_labels")
                .cookie(authCookie)
                .get(ClientResponse.class);
        JSONArray verticesABAndC = response.getEntity(JSONArray.class);
        try{
            vertexA = verticesABAndC.getJSONObject(0);
            vertexB = verticesABAndC.getJSONObject(1);
            vertexC = verticesABAndC.getJSONObject(2);
        }catch(JSONException e){
            throw new RuntimeException(e);
        }
        return verticesABAndC;
    }

    public URI vertexAUri(){
        return vertexUtils.uriOfVertex(
                vertexA
        );
    }

    public URI vertexBUri(){
        return vertexUtils.uriOfVertex(
                vertexB
        );
    }

    public URI vertexCUri(){
        return vertexUtils.uriOfVertex(
                vertexC
        );
    }

    public JSONObject vertexA(){
        return vertexUtils.vertexWithUri(vertexAUri());
    }
    public JSONObject vertexB(){
        return vertexUtils.vertexWithUri(vertexBUri());
    }
    public JSONObject vertexC(){
        return vertexUtils.vertexWithUri(vertexCUri());
    }
}