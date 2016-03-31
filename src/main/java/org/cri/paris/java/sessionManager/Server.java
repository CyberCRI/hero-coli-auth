
package org.cri.paris.java.sessionManager;

import io.vertx.core.Vertx;

/**
 * Main class used to launch the VertX server
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class Server {

    /**
     * Main used to launch the VertX server
     * @param args : command line arguments
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        LoginService service = new LoginService();
        vertx.deployVerticle(service);
    }
}
