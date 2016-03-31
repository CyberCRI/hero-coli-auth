
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
        //TODO use config management
        final String HOST = "localhost";
        final String PORT = "5000";
        final String MAX_POOL_SIZE = "256";
        final String USERNAME = "manager";
        final String PASSWORD = "slkdfj315746";
        final String DATABASE = "sessions";
        Vertx vertx = Vertx.vertx();
        LoginService service = new LoginService(SessionManager.getSessionManager(vertx, HOST, PORT, MAX_POOL_SIZE, USERNAME, PASSWORD, DATABASE));
        vertx.deployVerticle(service);
    }
}
