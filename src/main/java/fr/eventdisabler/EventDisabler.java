package fr.eventdisabler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class EventDisabler extends JavaPlugin {
    private Events events;

    @Override
    public void onEnable() {
        events = new Events(this);
    }

    @Override
    public void onDisable() { }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("eventdisabler.reload")) {
            sender.sendMessage(color("&cYou don't have permission."));
            return true;
        }

        events = new Events(this);
        sender.sendMessage(color("&3&l[&e&lEventDisabler&3&l] Reloaded."));
        return true;
    }

    private String color(final String texte){
        return ChatColor.translateAlternateColorCodes('&', texte);
    }

}
