package org.cri.paris.java.sessionManager;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Skeleton de webService
 * @author Besnard Arthur
 */
public class LoginService extends AbstractVerticle {
    
    private SessionManager sessionManager;
    private static final String HOST = "localhost";
    private static final String PORT = "5000";
    private static final String MAX_POOL_SIZE = "256";
    private static final String USERNAME = "manager";
    private static final String PASSWORD = "slkdfj315746";
    private static final String DATABASE = "sessions";

    @Override
    public void start() {
        Router router = Router.router(vertx);
        sessionManager = SessionManager.getSessionManager(vertx, HOST, PORT, MAX_POOL_SIZE, USERNAME, PASSWORD, DATABASE);

        router.post("/session/:tokenid/:sessionid/").handler(this::postNewSession);
        router.get("/session/:tokenid/").handler(this::getSessions);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
    
    private void postNewSession(RoutingContext rc){
        String tokenID = rc.request().getParam("tokenid");
        String sessionID = rc.request().getParam("sessionid");
        sessionManager.putSession(sessionID, tokenID);
    }
    
    private void getSessions(RoutingContext rc){
        String tokenID = rc.request().getParam("tokenid");
    }
}
