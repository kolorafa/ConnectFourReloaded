/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.kolorafa.connectfourreloaded;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author kolorafa
 */
public class cfPlugin extends JavaPlugin implements Listener {

    public final Logger logger = Logger.getLogger("Minecraft");
    PluginDescriptionFile pdffile;
    
    public void log(String text) {
        if (getConfig().getBoolean("debug")) {
            logger.log(Level.INFO, "[" + pdffile.getName() + "] DEBUG: " + text);
        }
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO, "[" + pdffile.getName() + "] is disabled.");
    }

    @Override
    public void onEnable() {
        loadConfiguration();
        pdffile = this.getDescription();
        getServer().getPluginManager().registerEvents(this, this);
        invites = new HashMap<String, String>();
    }

    private void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveConfig();     
    }
    Map<String, String> invites;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("This can be use only from game.");
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                sender.sendMessage((getConfig().getStringList("messages." + getConfig().getString("messageLang") + ".help").toArray(new String[0])));
            } else if (args[0].equalsIgnoreCase("accept")) {
                String pname = invites.get(player.getName());
                if (pname == null) {
                    sender.sendMessage(getMessage("donthaveinv"));
                } else {
                    Player p = getServer().getPlayerExact(pname);
                    invites.remove(player.getName());
                    if (p == null) {
                        sender.sendMessage(getMessage("playergoneoff").replace("{player}", p.getDisplayName()));
                    } else {
                        sender.sendMessage(getMessage("accepting").replace("{player}", p.getDisplayName()));
                        p.sendMessage(getMessage("accepted").replace("{player}", ((Player)sender).getDisplayName()));
                        new cfGame(this, player, p).startGame();
                    }
                }
            } else if (args[0].equalsIgnoreCase("reject")) {
                Player p = getServer().getPlayerExact(invites.get(player.getName()));
                if (p == null) {
                    sender.sendMessage(getMessage("playergoneoff").replace("{player}", p.getDisplayName()));
                } else {
                    sender.sendMessage(getMessage("rejecting").replace("{player}", p.getDisplayName()));
                    p.sendMessage(getMessage("rejected").replace("{player}", p.getDisplayName()));
                }
                invites.remove(player.getName());
            } else if (args[0].equalsIgnoreCase("back")) {
                sender.sendMessage("Not implemented ;)");
            } else {
                Player p = getServer().getPlayer(args[0]);
                if (p == null) {
                    sender.sendMessage(getMessage("playernotfound").replace("{player}", p.getDisplayName()));
                } else {
                    if (p.equals(player)) {
                        sender.sendMessage(getMessage("inviteself").replace("{player}", p.getDisplayName()));
                    } else {
                        sender.sendMessage(getMessage("sendinginv").replace("{player}", p.getDisplayName()));
                        p.sendMessage(getMessage("invitedBy").replace("{player}", player.getDisplayName()));
                        invites.remove(p.getName());
                        invites.put(p.getName(), player.getName());
                    }
                }
            }
        }
        return true;
    }

    public String getMessage(String messageType) {
        String msg = getConfig().getString("messages." + getConfig().getString("messageLang") + "." + messageType);
        if (msg == null) {
            return "ConnectFour unknown message: " + messageType;
        }
        return msg;
    }
}
