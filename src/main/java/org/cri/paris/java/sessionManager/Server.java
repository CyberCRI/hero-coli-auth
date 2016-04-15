
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
        final int PORT = 5432;
        final int MAX_POOL_SIZE = 256;
        final String USERNAME = "manager";
        final String PASSWORD = "slkdfj315746";
        final String DATABASE = "player";
        final String CLIENT_ID = "118231460924-7cmfpiefralb5ij4uv7kv6okcolhrc3k.apps.googleusercontent.com";
        Vertx vertx = Vertx.vertx();
        SessionControler service = 
                new SessionControler(SessionModel.getSessionManager(vertx, 
                    HOST, 
                    PORT, 
                    MAX_POOL_SIZE, 
                    USERNAME, 
                    PASSWORD, 
                    DATABASE), 
                new IdentityManager(CLIENT_ID));
        vertx.deployVerticle(service);
    }
}
