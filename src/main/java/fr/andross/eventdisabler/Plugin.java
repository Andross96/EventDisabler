package fr.andross.eventdisabler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::load, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eventdisabler.reload")) {
            sender.sendMessage(Utils.color("&cYou do not have permission."));
            return true;
        }

        load();
        sender.sendMessage(Utils.color("&3&l[&e&lEventDisabler&3&l] Reloaded."));
        return true;
    }

    public void load() {
        // Checking config
        if (!(new File(getDataFolder(), "config.yml").isFile())) saveDefaultConfig();
        reloadConfig();

        // Checking events
        final ConfigurationSection disabledSection = getConfig().getConfigurationSection("disabled");
        if (disabledSection == null) {
            getLogger().info("There is no events to disable.");
            return;
        }

        // Registering the events
        HandlerList.unregisterAll(this);
        final Listener listener = new Listener() {};
        int count = 0;
        for (final String eventName : disabledSection.getKeys(false)) {
            // Getting event class
            final Class<? extends Event> eventClass = Utils.getEventClass(eventName);
            if (eventClass == null) {
                getLogger().warning("[!!] Can't disable event '" + eventName + "': event class is not found.");
                continue;
            }

            // Checking if it is a cancellable event
            if (!Cancellable.class.isAssignableFrom(eventClass)) {
                getLogger().warning("[!!] Can't disable event '" + eventName + "': the event is not cancellable.");
                continue;
            }

            // Getting list of applied worlds
            final List<String> worldsName = disabledSection.getStringList(eventName);
            final Set<World> worlds = new HashSet<>();
            // All worlds?
            if (worldsName.contains("*")) worlds.addAll(Bukkit.getWorlds());
            else {
                for (String worldName : worldsName) {
                    final World w = Bukkit.getWorld(worldName);
                    if (w == null) getLogger().warning("[!!] Unknown world '" + worldName + "' for event '" + eventName + "'.");
                    else worlds.add(w);
                }
            }

            // Setting the event executor
            // Worlded event?
            final EventExecutor eventExecutor;
            if (worlds.isEmpty())
                eventExecutor = (bListener, bEvent) -> ((Cancellable) bEvent).setCancelled(true);
            else
                eventExecutor = (bListener, bEvent) -> {
                    if (Utils.isWorldDisabled(worlds, bEvent)) ((Cancellable) bEvent).setCancelled(true);
                };

            // Register event
            getServer().getPluginManager().registerEvent(eventClass, listener, EventPriority.LOWEST, eventExecutor, this, true);
            count++; // one more event registered
        }

        // Result
        if (count == 0) getLogger().warning("There is no events to disable.");
        else getLogger().info("Disabled " + count + " event" + (count > 1 ? "s" : "") + ".");
    }
}
