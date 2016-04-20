package org.cri.paris.java.sessionManager;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class SessionModel {

    /*private static final String UPDATE_SESSION_QUERY = "UPDATE player SET sessions = array_cat(sessions, '{?}') WHERE pid = ?;";
    private static final String INSERT_SESSION_QUERY = "INSERT INTO player VALUES ('?', '{?}');";
    private static final String CREATESESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS player (pid varchar(32) NOT NULL PRIMARY KEY, sessions varchar(40)[]);";
    private static final String GET_SESSION_TABLE_QUERY = "SELECT sessions FROM player where pid = ?;";*/
    
    private static final String INSERT_SESSION_QUERY = "INSERT INTO session VALUES (?, ?);";
    private static final String CREATE_INDEX_SESSION_TABLE_QUERY = "CREATE INDEX pid_idx ON session (pid);";
    private static final String CREATE_SESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS session (id varchar(40) NOT NULL PRIMARY KEY, pid varchar(40));";
    private static final String GET_SESSION_TABLE_QUERY = "SELECT id FROM session where pid = ?;";

    static SessionModel getSessionManager(Vertx vertx,
            String host,
            int port,
            int maxPoolSize,
            String username,
            String password,
            String database) {
        JsonObject sqlClientConfig = new JsonObject();
        sqlClientConfig.put("host", host);
        sqlClientConfig.put("port", port);
        sqlClientConfig.put("maxPoolSize", maxPoolSize);
        sqlClientConfig.put("username", username);
        sqlClientConfig.put("password", password);
        sqlClientConfig.put("database", database);
        SessionModel sessionManager = new SessionModel(PostgreSQLClient.createShared(vertx, sqlClientConfig));
        sessionManager.init();
        return sessionManager;
    }

    private final AsyncSQLClient sqlClient;

    private SessionModel(AsyncSQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    void putSession(String sessionID, String googlePlayerID) {
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.queryWithParams(INSERT_SESSION_QUERY, new JsonArray().add(sessionID).add(googlePlayerID), this::logQueryHandler);
            } else {
                throw new RuntimeException("Error : Enable to connect to the database");
            }
        });
    }

    private void init() {
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.query(CREATE_SESSION_TABLE_QUERY, result->{
                    connection.query(CREATE_INDEX_SESSION_TABLE_QUERY, this::logQueryHandler);
                });
            } else {
                throw new RuntimeException("Error : Enable to connect to the database");
            }
        });
    }

    void getSession(String playerId, Consumer<List<String>> callback){
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.queryWithParams(GET_SESSION_TABLE_QUERY, new JsonArray().add(playerId), results -> {
                    if(results.succeeded()){
                        ResultSet resSet = results.result();
                        List<JsonArray> rows = resSet.getResults();
                        ArrayList<String> sessions = new ArrayList<>();
                        rows.forEach(row -> sessions.add(row.getString(0)));
                        callback.accept(sessions);
                    }
                });
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error : Enable to connect to the database ", res.cause());
            }
        });
    }

    void close() {
        this.sqlClient.close();
    }

    private void logQueryHandler(AsyncResult<ResultSet> result) {
        if (result.succeeded()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Info : ", result.result().toJson());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error : Query failed because of : ", result.cause());
        }
    }
}
