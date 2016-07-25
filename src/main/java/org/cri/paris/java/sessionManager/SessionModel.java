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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO Break this apart
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class SessionModel {

    private static final String CREATE_INDEX_SESSION_TABLE_QUERY = "CREATE INDEX gid_idx ON player (gid);";
    private static final String CREATE_SESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS session (id varchar(40) NOT NULL PRIMARY KEY, pid int REFERENCES player (id));";
    private static final String CREATE_PLAYER_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS player (id SERIAL PRIMARY KEY, gid varchar(40) NOT NULL);";

    private static final String GET_PLAYER_QUERY = "SELECT id FROM player where gid=?;";
    private static final String GET_SESSIONS_QUERY = "SELECT id, pid FROM session GROUP BY pid;";

    private static final String INSERT_SESSION_QUERY = "INSERT INTO session (id, pid) VALUES(?,?);";
    private static final String INSERT_PLAYER_QUERY = "INSERT INTO player VALUES(DEFAULT, ?) RETURNING id;";

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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Inserting session : {0} {1}", new Object[]{sessionID, googlePlayerID});
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                connection.queryWithParams(GET_PLAYER_QUERY, new JsonArray().add(googlePlayerID), getPlayerResults -> {
                    if (getPlayerResults.succeeded()) {
                        //if a player is found
                        if (getPlayerResults.result().getNumRows() >= 1) {
                            //get the found player id
                            int playerid = getPlayerResults.result().getRows().get(0).getInteger("id");
                            //put a new session in database with the pid playerid
                            connection.updateWithParams(INSERT_SESSION_QUERY, new JsonArray().add(sessionID).add(playerid), insertSessionResults -> {
                                if (insertSessionResults.failed()) {
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error inserting session : ", insertSessionResults.cause());
                                }
                            });
                        } else {
                            connection.queryWithParams(INSERT_PLAYER_QUERY, new JsonArray().add(googlePlayerID), insertPlayerResults -> {
                                if (insertPlayerResults.succeeded()) {
                                    int playerid = insertPlayerResults.result().getRows().get(0).getInteger("id");
                                    connection.queryWithParams(INSERT_SESSION_QUERY, new JsonArray().add(sessionID).add(playerid), insertSessionResults -> {
                                        if (insertSessionResults.failed()) {
                                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error inserting player : ", insertSessionResults.cause());
                                        }
                                    });
                                }
                            });
                        }
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
                connection.query(CREATE_PLAYER_TABLE_QUERY, playerQuery -> {
                    if(playerQuery.succeeded()){
                        connection.query(CREATE_SESSION_TABLE_QUERY, sessionQuery -> {
                            if(sessionQuery.succeeded()){
                                connection.query(CREATE_INDEX_SESSION_TABLE_QUERY, this::logQueryHandler);
                            }
                        });
                    }
                });
            } else {
                throw new RuntimeException("Error : Enable to connect to the database");
            }
        });
    }

    void getSession(String googleId, Consumer<List<String>> callback) {
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.queryWithParams(GET_SESSIONS_QUERY, new JsonArray().add(googleId), results -> {
                    if (results.succeeded()) {
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

    void getSessions(Consumer<List<String>> callback) {
        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.query(GET_SESSIONS_QUERY, results -> {
                    if (results.succeeded()) {
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
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Info : ", result.result().getRows().toString());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error : Query failed because of : ", result.cause());
        }
    }
}
