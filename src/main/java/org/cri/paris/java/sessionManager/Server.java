
package org.cri.paris.java.sessionManager;

import io.vertx.core.Vertx;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cri.configurator.Config;
import org.cri.configurator.Configs;


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
        
        try {
            Config<String, String> conf = Configs.getSimpleConfig(Paths.get("/etc/redlogin/redlogin.conf"),
                    "host",
                    "port",
                    "max_pool",
                    "db_username",
                    "db_password",
                    "db_name",
                    "google_client_id");
            
            Vertx vertx = Vertx.vertx();
            SessionControler service =
                    new SessionControler(SessionModel.getSessionManager(vertx,
                            conf.get("host"),
                            Integer.parseInt(conf.get("port")),
                            Integer.parseInt(conf.get("max_pool")),
                            conf.get("db_username"),
                            conf.get("db_password"),
                            conf.get("db_name")),
                            new IdentityManager(conf.get("google_client_id")));
            vertx.deployVerticle(service);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Unable to open configuration file : ", ex);
        } catch (IllegalStateException ise){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error parsing configuration : ", ise);
        }
    }
}
