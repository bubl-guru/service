/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.json.UserJson;
import org.triple_brain.service.Launcher;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.triple_brain.module.repository_sql.SQLConnection.*;


public abstract class RestTestUtils {

    public static URI BASE_URI;
    public static WebResource resource;
    static public Launcher launcher;
    static public Client client;
    protected NewCookie authCookie;
    public static final String DEFAULT_PASSWORD = "password";

    @BeforeClass
    static public void startServer() throws Exception {

    }

    @AfterClass
    static public void stopServer() throws Exception {

    }

    @Before
    public void before_rest_test() throws SQLException {
        cleanTables();
    }

    @After
    public void after_rest_test() throws SQLException {
        closeConnection();
    }


    static protected void cleanTables() throws SQLException {
        clearDatabases();
        createTables();
    }

    protected ClientResponse createUser(JSONObject userAsJson) {
        ClientResponse response = resource
                .path("service")
                .path("users")
                .cookie(authCookie)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, userAsJson);
        assertThat(
                response.getStatus(),
                is(Response.Status.CREATED.getStatusCode())
        );
        return response;
    }

    protected User authenticate(User user) {
        try {
            JSONObject loginInfo = new JSONObject()
                    .put(
                            UserJson.EMAIL,
                            user.email()
                    )
                    .put(UserJson.PASSWORD, DEFAULT_PASSWORD);
            ClientResponse response = resource
                    .path("service")
                    .path("users")
                    .path("session")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, loginInfo);
            assertThat(response.getStatus(), is(200));
            authCookie = response.getCookies().get(0);
            return user;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected ClientResponse authenticate(JSONObject user) {
        try {
            JSONObject loginInfo = new JSONObject()
                    .put(
                            UserJson.EMAIL,
                            user.getString(UserJson.EMAIL)
                    )
                    .put(UserJson.PASSWORD, DEFAULT_PASSWORD);
            ClientResponse response = resource
                    .path("service")
                    .path("users")
                    .path("session")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, loginInfo);
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            authCookie = response.getCookies().get(0);
            return response;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}