/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import guru.bubl.module.common_utils.NoEx;
import guru.bubl.module.common_utils.Uris;
import guru.bubl.module.model.User;
import guru.bubl.module.model.UserUris;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.edge.Edge;
import guru.bubl.module.model.graph.edge.EdgePojo;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.module.model.graph.vertex.VertexInSubGraph;
import guru.bubl.module.model.graph.vertex.VertexInSubGraphJson;
import guru.bubl.module.model.graph.vertex.VertexInSubGraphPojo;
import guru.bubl.module.model.json.LocalizedStringJson;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VertexRestTestUtils {

    private WebResource resource;
    private NewCookie authCookie;
    private User authenticatedUser;
    private Gson gson = new Gson();

    public static VertexRestTestUtils withWebResourceAndAuthCookie(WebResource resource, NewCookie authCookie, User authenticatedUser) {
        return new VertexRestTestUtils(resource, authCookie, authenticatedUser);
    }

    protected VertexRestTestUtils(WebResource resource, NewCookie authCookie, User authenticatedUser) {
        this.resource = resource;
        this.authCookie = authCookie;
        this.authenticatedUser = authenticatedUser;
    }

    public ClientResponse updateVertexLabelUsingRest(URI vertexUri, String label) {
        try {
            JSONObject localizedLabel = new JSONObject().put(
                    LocalizedStringJson.content.name(),
                    label
            );
            return resource
                    .path(vertexUri.getPath())
                    .path("label")
                    .cookie(authCookie)
                    .post(ClientResponse.class, localizedLabel);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public VertexInSubGraph vertexWithUriOfAnyUser(URI vertexUri) {
        ClientResponse response = resource
                .path("service")
                .path("test")
                .path("vertex")
                .path(Uris.encodeURL(vertexUri.toString()))
                .cookie(authCookie)
                .get(ClientResponse.class);
        JSONObject jsonObject = response.getEntity(JSONObject.class);
        return vertexFromJson(
                jsonObject
        );
    }


    public Set<Edge> connectedEdgesOfVertexWithURI(URI vertexUri) {
        ClientResponse response = resource
                .path("service")
                .path("test")
                .path("vertex")
                .path(Uris.encodeURL(vertexUri.toString()))
                .path("connected_edges")
                .cookie(authCookie)
                .get(ClientResponse.class);
        JSONArray jsonArray = response.getEntity(JSONArray.class);
        return gson.fromJson(
                jsonArray.toString(),
                new TypeToken<Set<EdgePojo>>() {
                }.getType()
        );
    }

    public boolean vertexWithUriHasDestinationVertexWithUri(URI vertexUri, URI destinationVertexUri) {
        ClientResponse response = resource
                .path("service")
                .path("test")
                .path("vertex")
                .path(Uris.encodeURL(vertexUri.toString()))
                .path("has_destination")
                .path(Uris.encodeURL(destinationVertexUri.toString()))
                .cookie(authCookie)
                .get(ClientResponse.class);
        String hasDestinationStr = response.getEntity(String.class);
        return Boolean.valueOf(hasDestinationStr);
    }

    public URI uriOfVertex(JSONObject jsonObject) {
        return vertexFromJson(
                jsonObject
        ).uri();
    }

    public ClientResponse updateVertexANote(String note) {
        return resource
                .path(graphUtils().vertexAUri().getPath())
                .path("comment")
                .cookie(authCookie)
                .type(MediaType.TEXT_PLAIN)
                .post(ClientResponse.class, note);
    }

    public ClientResponse addAVertexToVertexWithUri(URI vertexUri) {
        return resource
                .path(vertexUri.getPath())
                .cookie(authCookie)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, "{}");
    }

    public ClientResponse makePublicVertexWithUri(URI vertexUri) {
        return this.setShareLevel(
                vertexUri,
                ShareLevel.PUBLIC
        );
    }

    public ClientResponse setShareLevel(URI uri, ShareLevel shareLevel) {
        return NoEx.wrap(() -> {
            ClientResponse clientResponse = resource
                    .path(uri.getPath())
                    .path("shareLevel")
                    .cookie(authCookie)
                    .post(ClientResponse.class, new JSONObject().put(
                            "shareLevel", shareLevel.name()
                    ));
            assertThat(
                    clientResponse.getStatus(),
                    is(Response.Status.NO_CONTENT.getStatusCode())
            );
            return clientResponse;
        }).get();
    }

    public ClientResponse makePrivateVertexWithUri(URI vertexUri) {
        return this.setShareLevel(
                vertexUri,
                ShareLevel.PRIVATE
        );
    }

    public ClientResponse makePublicVerticesWithUri(URI... vertexUri) {
        return this.setShareLevelOfCollection(
                ShareLevel.PUBLIC,
                authCookie,
                vertexUri
        );
    }

    public ClientResponse makePrivateVerticesWithUri(URI... vertexUri) {
        return this.setShareLevelOfCollection(
                ShareLevel.PRIVATE,
                authCookie,
                vertexUri
        );
    }

    public String getVertexBaseUri() {
        return new UserUris(authenticatedUser).baseVertexUri().toString();
    }

    public ClientResponse setShareLevelOfCollection(ShareLevel shareLevel, NewCookie cookie, URI... vertexUri) {
        return NoEx.wrap(() ->
                resource
                        .path(this.getVertexBaseUri())
                        .path("collection")
                        .path("share-level")
                        .cookie(cookie)
                        .post(
                                ClientResponse.class,
                                new JSONObject().put(
                                        "shareLevel",
                                        shareLevel.name()
                                ).put(
                                        "verticesUri",
                                        new JSONArray(Arrays.asList(vertexUri))
                                )
                        )
        ).get();
    }

    public ClientResponse removeVertexB() {
        return resource
                .path(graphUtils().vertexBUri().getPath())
                .cookie(authCookie)
                .delete(ClientResponse.class);
    }

    public URI baseVertexUri() {
        return new UserUris(
                authenticatedUser
        ).baseVertexUri();
    }

    public Vertex createSingleVertex() {
        return VertexInSubGraphJson.fromJson(
                responseForCreateSingleVertex().getEntity(
                        JSONObject.class
                )
        );
    }

    public ClientResponse responseForCreateSingleVertex() {
        return resource
                .path(
                        new UserUris(authenticatedUser).baseVertexUri().getPath()
                )
                .cookie(authCookie)
                .post(ClientResponse.class);
    }

    private GraphRestTestUtils graphUtils() {
        return GraphRestTestUtils.withWebResourceAndAuthCookie(
                authCookie,
                authenticatedUser
        );
    }

    protected VertexInSubGraph vertexFromJson(JSONObject jsonObject) {
        return gson.fromJson(
                jsonObject.toString(),
                VertexInSubGraphPojo.class
        );
    }
}
