/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cri.paris.java.sessionManager;

import io.vertx.core.Vertx;
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

    private final AsyncSQLClient mySQLClient;

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

    private SessionManager(AsyncSQLClient mySQLClient) {
        this.mySQLClient = mySQLClient;
    }

    public void putSession(PlayerSession session, String googlePlayerID) {
        this.mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.execute("INSERT INTO session (googleplayerid, sessionid) VALUES (" + googlePlayerID + ", " + session.getId() + ")", results -> {

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
                connection.execute("CREATE TABLE IF NOT EXISTS session ("
                        + "ID int(20) unsigned NOT NULL auto_increment,"
                        + "googleplayerid varchar(255) NOT NULL,"
                        + "sessionid varchar(255) NOT NULL,"
                        + "PRIMARY KEY  (ID, googleplayerid)"
                        + ")", results -> {

                        });
            } else {
                Logger.getLogger(this.getClass().getName()).severe("Error : Enable to connect to the database " + res.cause());
            }
        });
    }
}
