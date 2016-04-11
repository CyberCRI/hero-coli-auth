package org.cri.paris.java.sessionManager;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

/**
 * login service for Redmetrics
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class SessionControler extends AbstractVerticle {
    
    private final SessionModel sessionManager;

    public SessionControler(SessionModel sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.post("/session/:tokenid/:sessionid/").handler(this::postNewSession);
        router.get("/session/:tokenid/").handler(this::getSessions);

        vertx.createHttpServer().requestHandler(router::accept).listen(54321);
    }
    
    private void postNewSession(RoutingContext rc){
        String tokenID = rc.request().getParam("tokenid");
        String sessionID = rc.request().getParam("sessionid");
        sessionManager.putSession(sessionID, tokenID);
    }
    
    private List<String> getSessions(RoutingContext rc){
        String tokenID = rc.request().getParam("tokenid");
        return sessionManager.getSession(tokenID);
    }
}
