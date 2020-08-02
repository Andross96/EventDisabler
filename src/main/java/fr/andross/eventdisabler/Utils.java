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

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Utility class
 * @version 1.4
 * @author Andross
 */
public final class Utils {
    private final String[] packages = new String[] {
            // Bukkit
            "org.bukkit.event.block.", "org.bukkit.event.enchantment.", "org.bukkit.event.entity.",
            "org.bukkit.event.hanging.", "org.bukkit.event.inventory." , "org.bukkit.event.player.",
            "org.bukkit.event.server.", "org.bukkit.event.vehicle.", "org.bukkit.event.weather.",
            "org.bukkit.event.world.",
            // Spigot
            "org.spigotmc.event.entity.", "org.spigotmc.event.player.",
            // Paper
            "com.destroystokyo.paper.event.block.", "com.destroystokyo.paper.event.entity.",
            "com.destroystokyo.paper.event.player.", "com.destroystokyo.paper.event.profile.",
            "com.destroystokyo.paper.event.server."
    };
    private final String prefix = color("&3&l[&e&lEventDisabler&3&l] ");

    /**
     * Colorize a text
     * @param text text to colorize
     * @return the colored text
     */
    protected String color(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Send a colored (with prefix) message to the sender
     * @param sender the command sender
     * @param message the message to send
     */
    protected void sendMessage(@NotNull final CommandSender sender, @NotNull final String message) {
        sender.sendMessage(prefix + color(message));
    }

    /**
     * Trying to get the event class from the event name
     * @param event event name
     * @return the event class, null if not found
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Event> getEventClass(@NotNull final String event) {
        for (final String p : packages) {
            try {
                return (Class<? extends Event>) Class.forName(p + event);
            } catch (final Exception ignored) {
                /* ignored */
            }
        }
        return null;
    }

    /**
     * Check if the event is disabled in the world
     * @param worlds set of worlds
     * @param e the event
     * @return true if the event is disabled in the world, otherwise false
     */
    protected boolean isWorldDisabled(@NotNull final Set<World> worlds, @NotNull final Event e) {
        if (e instanceof BlockEvent) return worlds.contains(((BlockEvent) e).getBlock().getLocation().getWorld());
        else if (e instanceof EntityEvent) return worlds.contains(((EntityEvent) e).getEntity().getLocation().getWorld());
        else if (e instanceof HangingEvent) return worlds.contains(((HangingEvent) e).getEntity().getLocation().getWorld());
        else if (e instanceof PlayerEvent) return worlds.contains(((PlayerEvent) e).getPlayer().getLocation().getWorld());
        else if (e instanceof VehicleEvent) return worlds.contains(((VehicleEvent) e).getVehicle().getLocation().getWorld());
        else if (e instanceof WeatherEvent) return worlds.contains(((WeatherEvent) e).getWorld());
        else if (e instanceof WorldEvent) return worlds.contains(((WorldEvent) e).getWorld());
        else if (e instanceof InventoryEvent) {
            final InventoryEvent ie = (InventoryEvent) e;
            final HumanEntity he = ie.getInventory().getViewers().get(0);
            return he != null && worlds.contains(he.getWorld());
        }
        return false;
    }
}
