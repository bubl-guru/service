/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources;

import guru.bubl.module.model.User;
import guru.bubl.module.model.json.UserJson;
import guru.bubl.module.repository.user.NonExistingUserException;
import guru.bubl.module.repository.user.UserRepository;
import guru.bubl.service.SecurityInterceptor;
import guru.bubl.service.SessionHandler;
import guru.bubl.service.recaptcha.Recaptcha;
import guru.bubl.service.recaptcha.RecaptchaResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserSessionResource {

    @Inject
    UserRepository userRepository;

    @Inject
    SessionHandler sessionHandler;

    @Inject
    Recaptcha recaptcha;

    @GET
    @Path("/")
    public Response get(
            @Context HttpServletRequest request,
            @CookieParam(SessionHandler.PERSISTENT_SESSION) String persistentSessionId
    ) {
        if (!sessionHandler.isUserInSession(request.getSession(), persistentSessionId) || !isRightXsrfToken(request)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return Response.ok(
                UserJson.toJson(
                        sessionHandler.userFromSession(request.getSession())
                )
        ).build();
    }

    @POST
    @Produces(MediaType.WILDCARD)
    @Path("/")
    public Response authenticate(
            JSONObject loginInfo,
            @Context HttpServletRequest request,
            @CookieParam(SessionHandler.PERSISTENT_SESSION) String persistentSessionId
    ) {
        this._logout(request, persistentSessionId);
        try {
            RecaptchaResult recaptchaResult = recaptcha.getResult(
                    loginInfo
            );
            if (!recaptchaResult.isOk()) {
                return Response.status(
                        Response.Status.UNAUTHORIZED.getStatusCode()
                ).entity(new JSONObject().put(
                        "reason",
                        (recaptchaResult.isSuccess() ? "recaptcha score" : "problem connecting with recaptcha")
                )).build();
            }
            User user = userRepository.findByEmail(
                    loginInfo.getString(UserJson.EMAIL).toLowerCase().trim()
            );
            if (user.hasPassword(
                    loginInfo.getString(UserJson.PASSWORD)
            )) {
                authenticateUserInSession(
                        user,
                        request.getSession(),
                        request.getHeader(SessionHandler.X_XSRF_TOKEN)
                );
                Response.ResponseBuilder response = Response.ok(
                        UserJson.toJson(user)
                );
                if (loginInfo.optBoolean("staySignedIn")) {
                    response.cookie(sessionHandler.persistSessionForUser(
                            request.getSession(),
                            user,
                            request.getHeader(SessionHandler.X_XSRF_TOKEN)
                    ));
                } else {
                    if (sessionHandler.isUserInSession(request.getSession(), persistentSessionId)) {
                        sessionHandler.removePersistentSession(
                                persistentSessionId
                        );
                    }

                }
                return response.build();
            }
        } catch (NonExistingUserException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response logout(
            @CookieParam(SessionHandler.PERSISTENT_SESSION) String persistentSessionId,
            @Context HttpServletRequest request
    ) {
        if (!isRightXsrfToken(request)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        this._logout(request, persistentSessionId);
        return Response.ok().build();
    }

    public static void authenticateUserInSession(User user, HttpSession session, String xsrfToken) {
        session.setAttribute(SecurityInterceptor.AUTHENTICATION_ATTRIBUTE_KEY, true);
        session.setAttribute(SecurityInterceptor.AUTHENTICATED_USER_KEY, user);
        session.setAttribute(SessionHandler.X_XSRF_TOKEN, xsrfToken);
    }

    public static Boolean isRightXsrfToken(HttpServletRequest request) {
        Object xsrfTokenInSessionObject = request.getSession().getAttribute(SessionHandler.X_XSRF_TOKEN);
        if (xsrfTokenInSessionObject == null) {
            return false;
        }
        return xsrfTokenInSessionObject.toString().equals(
                request.getHeader(SessionHandler.X_XSRF_TOKEN)
        );
    }

    private void _logout(HttpServletRequest request, String persistentSessionId) {
        sessionHandler.removePersistentSession(persistentSessionId);
        request.getSession().setAttribute(SecurityInterceptor.AUTHENTICATION_ATTRIBUTE_KEY, false);
        request.getSession().setAttribute(SecurityInterceptor.AUTHENTICATED_USER_KEY, null);
        request.getSession().setAttribute(SessionHandler.X_XSRF_TOKEN, null);
    }
}
