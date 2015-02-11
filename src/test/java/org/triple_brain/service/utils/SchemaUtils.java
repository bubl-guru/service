/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service.utils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.triple_brain.module.model.graph.schema.Schema;
import org.triple_brain.module.model.json.LocalizedStringJson;

import javax.ws.rs.core.NewCookie;
import java.net.URI;

public class SchemaUtils {
    private WebResource resource;
    private NewCookie authCookie;
    private GraphRestTestUtils graphUtils;
    public static SchemaUtils withWebResourceAndAuthCookie(WebResource resource, NewCookie authCookie, GraphRestTestUtils graphUtils){
        return new SchemaUtils(resource, authCookie, graphUtils);
    }
    protected SchemaUtils(WebResource resource, NewCookie authCookie, GraphRestTestUtils graphUtils){
        this.resource = resource;
        this.authCookie = authCookie;
        this.graphUtils = graphUtils;
    }

    public ClientResponse createSchema() {
        return resource
                .path(graphUtils.getCurrentGraphUri())
                .path("schema")
                .cookie(authCookie)
                .post(ClientResponse.class);
    }

    public URI uriOfCreatedSchema(){
        return graphUtils.getElementUriInResponse(
                createSchema()
        );
    }

    public ClientResponse updateSchemaLabel(Schema schema, String newLabel) {
        return updateSchemaLabelWithUri(
                schema.uri(),
                newLabel
        );
    }

    public  ClientResponse updateSchemaLabelWithUri(URI schemaUri, String newLabel) {
        try {
            JSONObject localizedLabel = new JSONObject().put(
                    LocalizedStringJson.content.name(),
                    newLabel
            );
            return resource
                    .path(schemaUri.toString())
                    .path("label")
                    .cookie(authCookie)
                    .post(ClientResponse.class, localizedLabel);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}