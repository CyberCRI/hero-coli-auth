package org.cri.paris.java.sessionManager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Skeleton de webService
 * @author Besnard Arthur
 */
public class LoginService extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.post("/session/:tokenid/:sessionid/").handler(this::postNewSession);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
    
    private void postNewSession(RoutingContext rc){
        rc.request().getParam("tokenid");
    }
}
