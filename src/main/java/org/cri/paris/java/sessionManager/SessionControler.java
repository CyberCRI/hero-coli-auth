package org.cri.paris.java.sessionManager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * login service for Redmetrics
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class SessionControler extends AbstractVerticle {
    
    private final SessionModel sessionModel;
    private final IdentityManager identityManager;

    public SessionControler(SessionModel sessionModel, IdentityManager identityManager) {
        this.sessionModel = sessionModel;
        this.identityManager = identityManager;
    }
    
    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.post("/session/:tokenid/:sessionid/").handler(this::postNewSession);
        router.get("/session/:tokenid/").handler(this::getSessions);

        vertx.createHttpServer().requestHandler(router::accept).listen(54321);
    }
    
    private void postNewSession(RoutingContext rc){
        try {
            String tokenID = rc.request().getParam("tokenid");
            String sessionID = rc.request().getParam("sessionid");
            String googleID = identityManager.validateToken(tokenID);
            if(googleID != null){
                sessionModel.putSession(sessionID, googleID);
                rc.response().putHeader("content-type", "application/json")
                        .end(new JsonObject().put("result", "OK").encodePrettily());
            }
            
        } catch (GeneralSecurityException | IOException ex) {
            Logger.getLogger(SessionControler.class.getName()).log(Level.SEVERE, "Error : Unable tu verify tokenID using Google API", ex);
        }
    }
    
    private void getSessions(RoutingContext rc){
        String tokenID = rc.request().getParam("tokenid");
        sessionModel.getSession(tokenID, sessions -> {
            rc.response()
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("sessions", new JsonArray(sessions)).encodePrettily());
        });
        
    }
}
