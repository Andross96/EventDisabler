package fr.eventdisabler;

import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

class Events {

    Events(final Plugin pl){
        // Checking config
        if(!(new File(pl.getDataFolder(), "config.yml").isFile())) pl.saveDefaultConfig();
        pl.reloadConfig();

        // Checking events
        final List<String> events = pl.getConfig().getStringList("disabled");
        if(events.isEmpty()){
            pl.getLogger().info("There is no events to disable.");
            return;
        }

        // Registering the events
        HandlerList.unregisterAll(pl);
        int count = 0;
        for(String event : events){
            // Getting event class
            final Class<? extends Event> eventClass = getEventClass(event);
            if(eventClass == null){
                pl.getLogger().warning("[!!] Can't disable event '" + event + "': event class is not found.");
                continue;
            }

            // Checking if it is a cancellable event
            if(!Cancellable.class.isAssignableFrom(eventClass)){
                pl.getLogger().warning("[!!] Can't disable event '" + event + "': the event is not cancellable.");
                continue;
            }

            // Setting the event executor
            final EventExecutor eventExecutor = (bListener,bEvent) -> {
                Cancellable c = (Cancellable) bEvent;
                c.setCancelled(true);
            };

            // Register event
            pl.getServer().getPluginManager().registerEvent(eventClass, new Listener() {}, EventPriority.LOWEST, eventExecutor, pl, false);
            count++; // one more event registered
        }

        // Result
        if(count == 0) pl.getLogger().warning("There is no events to disable.");
        else if(count == 1) pl.getLogger().info("Disabled 1 event.");
        else pl.getLogger().info("Disabled " + count + " events.");
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> getEventClass(final String eName) {
        final String[] packagelist = new String[] {
                "org.bukkit.event.block.", "org.bukkit.event.enchantment.", "org.bukkit.event.entity.",
                "org.bukkit.event.hanging.", "org.bukkit.event.inventory." , "org.bukkit.event.player.",
                "org.bukkit.event.server.", "org.bukkit.event.vehicle.", "org.bukkit.event.weather.",
                "org.bukkit.event.world."
        };
        for(String p : packagelist) {
            try {  return (Class<? extends Event>) Class.forName(p + eName);
            }catch(Exception e) { /* NoThing */ }
        }
        return null;
    }
}
