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
            //tokenID = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjY3YWYyYTZiOWMwZmQxNjM1MThiYzdlOTI4NTZkNDZlNGRlZWNhN2QifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6IlVONU9rM0pnR0dLQUY2UlpnaDNYMXciLCJhdWQiOiIxMTgyMzE0NjA5MjQtN2NtZnBpZWZyYWxiNWlqNHV2N2t2Nm9rY29saHJjM2suYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTYxMjQ2MTg0NjA0MjU3MTIwMjUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiMTE4MjMxNDYwOTI0LTdjbWZwaWVmcmFsYjVpajR1djdrdjZva2NvbGhyYzNrLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaGQiOiJjcmktcGFyaXMub3JnIiwiZW1haWwiOiJhcnRodXIuYmVzbmFyZEBjcmktcGFyaXMub3JnIiwiaWF0IjoxNDYxMjM3MTEzLCJleHAiOjE0NjEyNDA3MTMsIm5hbWUiOiJBcnRodXIgQmVzbmFyZCIsImdpdmVuX25hbWUiOiJBcnRodXIiLCJmYW1pbHlfbmFtZSI6IkJlc25hcmQiLCJsb2NhbGUiOiJmciJ9.nFuj-Wj1VVKZTDnooVY-dZVqMWo-cGaFf82Liug2nGTUzBqenej9tMpXFpY2XKKHbvVxHPKW2KYw45Pgoa1BjxhZRYIfHacN8oLjrk1-oXXoQwKY4mjV6DvgQcL_omG_CkX-9w_5xvwm8KLPMmTvwZpC1nLxdZ2rw4ZxEbv8tA_1YsKp7XX657NifaslU9012pObD-gdaNhGT0AzD3GiTSndoqgdaKPug4gW9R_PuGR-vct5GydrU0yq6GuGL_BTDG1JAl328vfnx4VMwZg8Mwz86BtRFSjjtbowwhqVetTZYjL3Icy38wnU2VIY6bMar1Ys-Yyapxru90zz_MHoVA";
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
