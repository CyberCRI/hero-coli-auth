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
public class Player {
    final Integer id;
    final String googleId;

    public Player(Integer id, String googleId) {
        this.id = id;
        this.googleId = googleId;
    }
}
