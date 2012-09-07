/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.connectfourreloaded;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author kolorafa
 */
public class cfWinAni implements Runnable {

    cfGame gra;

    public cfWinAni(cfGame gra) {
        this.gra = gra;
    }

    @Override
    public void run() {
        boolean show = false;
        Inventory i;
        try {
            while (gra.working) {
                i = gra.p1.getOpenInventory().getTopInventory();
                if (i == null) {
                    break;
                }
                i.setContents(show ? gra.inv1 : gra.inv2);
                

                i = gra.p2.getOpenInventory().getTopInventory();
                if (i == null) {
                    break;
                }
                i.setContents(show ? gra.inv1 : gra.inv2);

                show = !show;
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
        }
    }
}
