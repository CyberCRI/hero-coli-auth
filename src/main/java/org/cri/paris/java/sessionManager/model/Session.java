/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cri.paris.java.sessionManager.model;

/**
 *
 * @author arthur
 */
public class Session {
    final Integer id;
    final String playerId;

    public Session(Integer id, String playerId) {
        this.id = id;
        this.playerId = playerId;
    }
}
