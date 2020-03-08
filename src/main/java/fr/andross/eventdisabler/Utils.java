package fr.andross.eventdisabler;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;

import java.util.Set;

public class Utils {
    public static final String[] packagelist = new String[] {
            // Bukkit
            "org.bukkit.event.block.", "org.bukkit.event.enchantment.", "org.bukkit.event.entity.",
            "org.bukkit.event.hanging.", "org.bukkit.event.inventory." , "org.bukkit.event.player.",
            "org.bukkit.event.server.", "org.bukkit.event.vehicle.", "org.bukkit.event.weather.",
            "org.bukkit.event.world.",
            // Paper
            "com.destroystokyo.paper.event.block", "com.destroystokyo.paper.event.entity",
            "com.destroystokyo.paper.event.player", "com.destroystokyo.paper.event.profile",
            "com.destroystokyo.paper.event.server"
    };

    public static String color(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Event> getEventClass(final String eName) {
        for (String p : packagelist) {
            try {
                return (Class<? extends Event>) Class.forName(p + eName);
            } catch (Exception e) { /* ignored */ }
        }
        return null;
    }

    public static boolean isWorldDisabled(final Set<World> worlds, final Event e) {
        if (e instanceof BlockEvent) return worlds.contains(((BlockEvent) e).getBlock().getLocation().getWorld());
        else if (e instanceof EntityEvent) return worlds.contains(((EntityEvent) e).getEntity().getLocation().getWorld());
        else if (e instanceof HangingEvent) return worlds.contains(((HangingEvent) e).getEntity().getLocation().getWorld());
        else if (e instanceof PlayerEvent) return worlds.contains(((PlayerEvent) e).getPlayer().getLocation().getWorld());
        else if (e instanceof VehicleEvent) return worlds.contains(((VehicleEvent) e).getVehicle().getLocation().getWorld());
        else if (e instanceof WeatherEvent) return worlds.contains(((WeatherEvent) e).getWorld());
        else if (e instanceof WorldEvent) return worlds.contains(((WorldEvent) e).getWorld());
        return false;
    }
}
