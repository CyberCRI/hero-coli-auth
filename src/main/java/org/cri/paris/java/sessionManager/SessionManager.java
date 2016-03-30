
package org.cri.paris.java.sessionManager;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arthur
 */
public class SessionManager {
    
    private static final String INSERT_SESSION_QUERY = "INSERT INTO session (googleplayerid, sessionid) VALUES (?, ?)";
    private static final String CREATESESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS session ("
                        + "ID int(20) unsigned NOT NULL auto_increment,"
                        + "googleplayerid varchar(255) NOT NULL,"
                        + "sessionid varchar(255) NOT NULL,"
                        + "PRIMARY KEY  (ID, googleplayerid)"
                        + ");";

    public static SessionManager getSessionManager(Vertx vertx,
            String host,
            String port,
            String maxPoolSize,
            String username,
            String password,
            String database) {
        JsonObject mySQLClientConfig = new JsonObject();
        mySQLClientConfig.put("host", host);
        mySQLClientConfig.put("port", port);
        mySQLClientConfig.put("maxPoolSize", maxPoolSize);
        mySQLClientConfig.put("username", username);
        mySQLClientConfig.put("password", password);
        mySQLClientConfig.put("database", database);
        SessionManager sessionManager = new SessionManager(MySQLClient.createNonShared(vertx, mySQLClientConfig));
        sessionManager.init();
        return sessionManager;
    }
    
    private final AsyncSQLClient mySQLClient;

    private SessionManager(AsyncSQLClient mySQLClient) {
        this.mySQLClient = mySQLClient;
    }

    public void putSession(String sessionID, String googlePlayerID) {
        this.mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.queryWithParams(INSERT_SESSION_QUERY, new JsonArray().add(sessionID).add(googlePlayerID), results -> {
                    //Nothing to do
                });
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error : Enable to connect to the database {0}", res.cause());
            }
        });
    }

    private void init() {
        this.mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.execute(CREATESESSION_TABLE_QUERY, results -> {

                        });
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error : Enable to connect to the database {0}", res.cause());
            }
        });
    }
}
