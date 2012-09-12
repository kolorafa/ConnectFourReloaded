/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.connectfourreloaded;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Spring;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author kolorafa
 */
public class cfGame implements Listener {

    Player p1;
    Player p2;
    cfPlugin plugin;
    boolean kolejka;
    boolean gameLocked;
    boolean working = true;
    int[][] plansza = new int[7][7];//new int[7][6];
    ItemStack[] inv1;
    ItemStack[] inv2;

    enum gameStatus {

        GAME_RUNNING,
        GAME_PROCESSING,
        GAME_ENDED
    }
    gameStatus status = gameStatus.GAME_RUNNING;

    public cfGame(cfPlugin plugin, Player p1, Player p2) {
        this.plugin = plugin;
        this.p1 = p1;
        this.p2 = p2;
    }

    private void log(String s) {
        plugin.log("Game: [" + p1.getName() + ", " + p2.getName() + "] " + s);
    }

    private void registerEvenets() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void unregisterEvenets() {
        working = false;
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    public void renderGame(Player p) {
        renderGame(p.getOpenInventory().getTopInventory(), plansza);
        //p.updateInventory();
    }

    public void renderGame(Inventory i) {
        renderGame(i, plansza);
    }

    public void renderGame(Inventory i, int[][] planszar) {
        ItemStack[] inv = i.getContents();

        inv = renderInv(inv, planszar);

        i.setContents(inv);
    }

    public ItemStack[] renderInv(ItemStack[] inv, int[][] planszar) {
        for (int y = 0; y < 6; ++y) {
            inv[y * 9] = new ItemStack(Material.WATER);
            inv[y * 9 + 8] = new ItemStack(Material.WATER);
            for (int x = 0; x < 7; ++x) {
                if (planszar[x][y] == 1) {
                    inv[y * 9 + x + 1] = new ItemStack(Material.WOOL, 1, (short) 14);
                }
                if (planszar[x][y] == 2) {
                    inv[y * 9 + x + 1] = new ItemStack(Material.WOOL, 1, (short) 4);
                }
                if (planszar[x][y] == 3) {
                    inv[y * 9 + x + 1] = new ItemStack(Material.WOOL, 1, (short) 0);
                }
                if (planszar[x][y] == 0) {
                    inv[y * 9 + x + 1] = new ItemStack(Material.AIR);
                }
            }
        }
        return inv;
    }

    public void ruch(Player p, int kolumna) {
        if (gameLocked) {
            //p.sendMessage("Game ended");
            return;
        }
        if (kolejka) {
            if (p.equals(p1)) {
                p1.sendMessage(plugin.getMessage("notYourMove"));
                return;
            }
        } else {
            if (p.equals(p2)) {
                p2.sendMessage(plugin.getMessage("notYourMove"));
                return;
            }
        }

        int pozycja = (kolumna % 9) - 1;
        if (pozycja < 0 || pozycja > 6) {
            //p.sendMessage("Click on empty space");
            return;
        }

        for (int i = 5; i >= 0; --i) {
            if (plansza[pozycja][i] == 0) {
                if (plugin.getConfig().getBoolean("animateGame")) {
                    gameLocked = true;
                    try {
                        for (int z = 0; z < 5; ++z) {
                            if (plansza[pozycja][z + 1] == 0) {
                                plansza[pozycja][z] = (kolejka) ? 1 : 2;
                                renderGame(p1);
                                renderGame(p2);
                                plansza[pozycja][z] = 0;
                                Thread.sleep(200);
                            }
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(cfGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    gameLocked = false;
                }
                plansza[pozycja][i] = (kolejka) ? 1 : 2;
                renderGame(p1);
                renderGame(p2);
                checkGame();
                kolejka = !kolejka;
                return;
            }
        }
    }

    @EventHandler
    public void invclick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (p.equals(p1) || p.equals(p2)) {
                int slot = event.getSlot();
                if (slot >= 0 && slot <= 54) {
                    plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new cfDelayClick(this, p, event.getSlot()), 1L);
                }
                //event.setCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    public void quitGame(Player p) {
        unregisterEvenets();
        if (p.equals(p1)) {
            p2.closeInventory();
            p2.sendMessage(plugin.getMessage("playerquit").replace("{player}", p1.getDisplayName()));
        }
        if (p.equals(p2)) {
            p1.closeInventory();
            p1.sendMessage(plugin.getMessage("playerquit").replace("{player}", p2.getDisplayName()));
        }
    }

    @EventHandler
    public void invclose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player p = (Player) event.getPlayer();
            if (p.equals(p1) || p.equals(p2)) {
                quitGame(p);
            }
        }
    }

    @EventHandler
    public void playerquit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(p1) || event.getPlayer().equals(p2)) {
            quitGame(event.getPlayer());
        }
    }

    public void startGame() {
        registerEvenets();
        String invName = plugin.getMessage("invTitle").replace("{player}", p2.getDisplayName());
        if(invName.length()>30)invName=invName.substring(0, 30);
        Inventory p1ii = Bukkit.createInventory(p1, 54, invName);
        renderGame(p1ii);
        p1.openInventory(p1ii);
        invName = plugin.getMessage("invTitle").replace("{player}", p1.getDisplayName());
        if(invName.length()>30)invName=invName.substring(0, 30);
        Inventory p2ii = Bukkit.createInventory(p2, 54, invName);
        renderGame(p2ii);
        p2.openInventory(p2ii);
        kolejka = false;
        gameLocked = false;
        if (plugin.getConfig().getBoolean("broadcastMsg")) {
            plugin.getServer().broadcastMessage(plugin.getMessage("startedgame").replace("{player}", p1.getDisplayName()).replace("{player2}", p2.getDisplayName()));
        }
    }

    private void winner(int player) {
        gameLocked = true;
        Player pwin = (player == 2) ? p1 : p2;
        Player plose = (player == 2) ? p2 : p1;

        if (plugin.getConfig().getBoolean("broadcastMsg")) {
            plugin.getServer().broadcastMessage(plugin.getMessage("wonthegame").replace("{won}", pwin.getDisplayName()).replace("{lose}", plose.getDisplayName()));
        }

        pwin.sendMessage(plugin.getMessage("youWin").replace("{player}", p1.getDisplayName()));
        plose.sendMessage(plugin.getMessage("youLose").replace("{player}", p1.getDisplayName()));

        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new cfWinAni(this), 1L);
    }

//    private void visualize(int x, int y){
//                gameLocked = true;
//                try {
//                    int old = plansza[x][y];
//                    plansza[x][y] = 3;
//                    renderGame(p1);
//                    renderGame(p2);
//                    plansza[x][y] = old;
//                    Thread.sleep(1000);
//
//
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(cfGame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                gameLocked = false;
//        
//    }
//    
    private boolean check2(int x, int y, int player) {
        int stan = 0;
        while (y >= 0 && x < plansza.length && y < plansza[0].length) {
            if (plansza[x][y] == player) {
                ++stan;
            } else {
                stan = 0;
            }
            if (stan == 4) {
                plansza[x][y] = 3;
                plansza[x - 1][y - 1] = 3;
                plansza[x - 2][y - 2] = 3;
                plansza[x - 3][y - 3] = 3;
                inv1 = p1.getOpenInventory().getTopInventory().getContents();
                renderGame(p1);
                renderGame(p2);
                inv2 = p1.getOpenInventory().getTopInventory().getContents();
                return true;
            }
            ++x;
            ++y;
        }
        return false;
    }

    private boolean check3(int x, int y, int player) {
        int stan = 0;
        while (y >= 0 && x < plansza.length && y < plansza[0].length) {
            if (plansza[x][y] == player) {
                ++stan;
            } else {
                stan = 0;
            }
            if (stan == 4) {
                plansza[x][y] = 3;
                plansza[x - 1][y + 1] = 3;
                plansza[x - 2][y + 2] = 3;
                plansza[x - 3][y + 3] = 3;
                inv1 = p1.getOpenInventory().getTopInventory().getContents();
                renderGame(p1);
                renderGame(p2);
                inv2 = p1.getOpenInventory().getTopInventory().getContents();
                return true;
            }
            ++x;
            --y;
        }
        return false;
    }

    private void checkPlayer(int player) {
        //pionowo
        for (int x = 0; x < plansza.length; ++x) {
            int stan = 0;
            for (int y = 0; y < plansza.length; ++y) {
                if (plansza[x][y] == player) {
                    ++stan;
                } else {
                    stan = 0;
                }
                if (stan == 4) {
                    plansza[x][y] = 3;
                    plansza[x][y - 1] = 3;
                    plansza[x][y - 2] = 3;
                    plansza[x][y - 3] = 3;
                    inv1 = p1.getOpenInventory().getTopInventory().getContents();
                    renderGame(p1);
                    renderGame(p2);
                    inv2 = p1.getOpenInventory().getTopInventory().getContents();
                    winner(player);
                    return;
                }
            }
        }
        //poziomo
        for (int y = 0; y < plansza[0].length; ++y) {
            int stan = 0;
            for (int x = 0; x < plansza.length; ++x) {
                if (plansza[x][y] == player) {
                    ++stan;
                } else {
                    stan = 0;
                }
                if (stan == 4) {
                    plansza[x][y] = 3;
                    plansza[x - 1][y] = 3;
                    plansza[x - 2][y] = 3;
                    plansza[x - 3][y] = 3;
                    inv1 = p1.getOpenInventory().getTopInventory().getContents();
                    renderGame(p1);
                    renderGame(p2);
                    inv2 = p1.getOpenInventory().getTopInventory().getContents();
                    winner(player);
                    return;
                }
            }
        }

        for (int x = 0; x < 5; ++x) {
            if (check2(0, x, player)) {
                winner(player);
                return;
            }
        }
        for (int x = 1; x < 5; ++x) {
            if (check2(x, 0, player)) {
                winner(player);
                return;
            }
        }

        for (int x = 0; x < 7; ++x) {
            if (check3(0, 6 - x, player)) {
                winner(player);
                return;
            }
        }
        for (int x = 1; x < 5; ++x) {
            if (check3(x, 6, player)) {
                winner(player);
                return;
            }
        }


    }

    private void checkGame() {
        checkPlayer(1);
        checkPlayer(2);
    }
}
