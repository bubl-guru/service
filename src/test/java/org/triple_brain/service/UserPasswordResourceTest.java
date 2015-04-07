/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service;

import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.triple_brain.module.model.forget_password.UserForgetPasswordToken;
import org.triple_brain.module.model.json.UserJson;
import org.triple_brain.service.utils.GraphManipulationRestTestUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UserPasswordResourceTest extends GraphManipulationRestTestUtils {

    @Test
    public void changing_password_returns_correct_status() {
        logoutUsingCookie(authCookie);
        userUtils().setUserForgetPasswordToken(
                defaultAuthenticatedUser,
                UserForgetPasswordToken.generate().setResetPasswordToken("token")
        );
        assertThat(
                changePassword(
                        defaultAuthenticatedUser.email(),
                        "new_password",
                        "token"
                ).getStatus(),
                is(
                        ClientResponse.Status.NO_CONTENT.getStatusCode()
                )
        );
    }

    @Test
    public void can_change_password(){
        logoutUsingCookie(authCookie);
        userUtils().setUserForgetPasswordToken(
                defaultAuthenticatedUser,
                UserForgetPasswordToken.generate().setResetPasswordToken("token")
        );
        assertThat(
                authenticateUsingEmailAndPassword(
                        defaultAuthenticatedUser.email(),
                        "new_password"
                ).getStatus(),
                is(Response.Status.UNAUTHORIZED.getStatusCode())
        );
        changePassword(
                defaultAuthenticatedUser.email(),
                "new_password",
                "token"
        );
        assertThat(
                authenticateUsingEmailAndPassword(
                        defaultAuthenticatedUser.email(),
                        "new_password"
                ).getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    @Test
    public void forget_password_token_must_match_to_change_password(){
        logoutUsingCookie(authCookie);
        ClientResponse response = changePassword(
                defaultAuthenticatedUser.email(),
                "new_password",
                "token"
        );
        assertThat(
                response.getStatus(),
                is(Response.Status.UNAUTHORIZED.getStatusCode())
        );
        UserForgetPasswordToken userForgetPasswordToken = UserForgetPasswordToken.generate();
        userForgetPasswordToken.setResetPasswordToken("token");
        userUtils().setUserForgetPasswordToken(
                defaultAuthenticatedUser,
                userForgetPasswordToken
        );
        response = changePassword(
                defaultAuthenticatedUser.email(),
                "new_password",
                "token"
        );
        assertThat(
                response.getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
    }

    @Test
    public void cant_change_password_if_forget_password_token_is_expired(){
        logoutUsingCookie(authCookie);
        UserForgetPasswordToken userForgetPasswordToken = UserForgetPasswordToken.generate();
        userForgetPasswordToken.setResetPasswordExpirationDate(
                new DateTime().minusHours(1).toDate()
        );
        userUtils().setUserForgetPasswordToken(
                defaultAuthenticatedUser,
                userForgetPasswordToken
        );
        ClientResponse response = changePassword(
                defaultAuthenticatedUser.email(),
                "new_password",
                userForgetPasswordToken.getToken()
        );
        assertThat(
                response.getStatus(),
                is(Response.Status.UNAUTHORIZED.getStatusCode())
        );
        userForgetPasswordToken = UserForgetPasswordToken.generate();
        userUtils().setUserForgetPasswordToken(
                defaultAuthenticatedUser,
                userForgetPasswordToken
        );
        response = changePassword(
                defaultAuthenticatedUser.email(),
                "new_password",
                userForgetPasswordToken.getToken()
        );
        assertThat(
                response.getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
    }

    private ClientResponse changePassword(String email, String password, String forgetPasswordToken) {
        try {
            JSONObject data = new JSONObject().put(
                    "email",
                    email
            ).put(
                    "password",
                    password
            ).put(
                    "token",
                    forgetPasswordToken
            );
            return resource
                    .path("service")
                    .path("users")
                    .path("password")
                    .post(ClientResponse.class, data);
        }catch(JSONException e){
            throw new RuntimeException(e);
        }
    }

    private ClientResponse authenticateUsingEmailAndPassword(String email, String password) {
        try {
            JSONObject loginInfo = new JSONObject()
                    .put(
                            UserJson.EMAIL,
                            email
                    )
                    .put(UserJson.PASSWORD, password);
            return resource
                    .path("service")
                    .path("users")
                    .path("session")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, loginInfo);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}