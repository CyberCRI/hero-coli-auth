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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class SessionModel {

    private static final String UPDATE_SESSION_QUERY = "UPDATE player SET sessions = array_cat(sessions, '{?}') WHERE pid = ?;";
    private static final String INSERT_SESSION_QUERY = "INSERT INTO player VALUES ('?', '[?]', ...);";
    private static final String CREATESESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS player (pid varchar(32) NOT NULL PRIMARY KEY, sessions uuid[]);";
    private static final String GET_SESSION_TABLE_QUERY = "SELECT sessions FROM player where pid = ?;";

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

                connection.queryWithParams(GET_SESSION_TABLE_QUERY, new JsonArray().add(googlePlayerID), results -> {
                    if (results.succeeded()) {
                        if (results.result().getNumRows() < 1) {
                            connection.queryWithParams(INSERT_SESSION_QUERY,
                                    new JsonArray().add(new JsonArray(sessionID)).add(googlePlayerID),
                                    this::logQueryHandler);
                        } else {
                            connection.queryWithParams(UPDATE_SESSION_QUERY,
                                    new JsonArray().add(sessionID).add(googlePlayerID),
                                    this::logQueryHandler);
                        }
                    } else {
                        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Connexion failed : {0}", results.result().toJson());
                    }
                });
            } else {
                throw new RuntimeException("Error : Enable to connect to the database");
            }
        });
    }

    private void init() {
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.query(CREATESESSION_TABLE_QUERY, this::logQueryHandler);
            } else {
                throw new RuntimeException("Error : Enable to connect to the database");
            }
        });
    }

    List<String> getSession(String playerId) {
        final ArrayList<String> sessions = new ArrayList<>();
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.queryWithParams(GET_SESSION_TABLE_QUERY, new JsonArray().add(playerId), results -> {
                    ResultSet resSet = results.result();
                    List<JsonArray> rows = resSet.getResults();

                    rows.get(0).getJsonArray(1).forEach(session -> sessions.add((String) session));
                });
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error : Enable to connect to the database {0}", res.cause());
            }
        });
        return sessions;
    }

    void close() {
        this.sqlClient.close();
    }

    private void logQueryHandler(AsyncResult<ResultSet> result) {
        if (result.succeeded()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Info : {0}", result.result().toJson());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error : Query failed because of : {0}", result.cause());
        }
    }
}