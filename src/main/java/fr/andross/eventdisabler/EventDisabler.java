/*
 * EventDisabler - easily disable bukkit events
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.eventdisabler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main plugin class
 * @version 1.4
 * @author Andross
 */
public final class EventDisabler extends JavaPlugin {
    private final Utils utils = new Utils();

    @Override
    public void onEnable() {
        // Loading the plugin on next tick, to allow worlds to be loaded by plugins
        Bukkit.getScheduler().runTaskLater(this, () -> load(Bukkit.getConsoleSender()), 20L);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        load(sender);
        return true;
    }

    /**
     * Loading config & listeners
     * @param sender the command sender
     */
    public void load(@NotNull final CommandSender sender) {
        // Config
        saveDefaultConfig();
        reloadConfig();

        // Checking events
        HandlerList.unregisterAll(this);
        final ConfigurationSection disabledSection = getConfig().getConfigurationSection("disabled");
        if (disabledSection == null) {
            utils.sendMessage(sender, "&eThere is no events to disable.");
            return;
        }

        // Registering the listeners
        final Listener listener = new Listener() {};
        int count = 0;
        for (final String eventName : disabledSection.getKeys(false)) {
            // Getting event class
            final Class<? extends Event> eventClass = utils.getEventClass(eventName);
            if (eventClass == null) {
                utils.sendMessage(sender, "&cCan not disable event '&e" + eventName + "&c': event class is unknown.");
                continue;
            }

            // Checking if it is a cancellable event
            if (!Cancellable.class.isAssignableFrom(eventClass)) {
                utils.sendMessage(sender, "&cCan not disable event '&e" + eventName + "&c': this event is not cancellable.");
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
                    if (w == null) utils.sendMessage(sender, "&cUnknown world '&e" + worldName + "&c' for event '&e" + eventName + "&c'.");
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
                    if (utils.isWorldDisabled(worlds, bEvent)) ((Cancellable) bEvent).setCancelled(true);
                };

            // Register event
            getServer().getPluginManager().registerEvent(eventClass, listener, EventPriority.LOWEST, eventExecutor, this, true);
            count++; // one more event registered
        }

        // Result
        if (count == 0)
            utils.sendMessage(sender, "&eThere is no events to disable.");
        else
            utils.sendMessage(sender, "&aDisabled &e" + count + "&a event" + (count > 1 ? "s" : "") + ".");
    }
}
