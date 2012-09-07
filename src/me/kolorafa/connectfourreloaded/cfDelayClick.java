/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.connectfourreloaded;

import org.bukkit.entity.Player;

/**
 *
 * @author kolorafa
 */
public class cfDelayClick implements Runnable {
    cfGame gra;
    Player p;
    int slot;

    public cfDelayClick(cfGame gra, Player p, int slot) {
        this.gra = gra;
        this.p = p;
        this.slot = slot;
    }

    @Override
    public void run() {
        gra.ruch(p, slot);
    }
}
