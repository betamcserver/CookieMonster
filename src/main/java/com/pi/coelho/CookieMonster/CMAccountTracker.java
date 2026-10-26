package com.pi.coelho.CookieMonster;

import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class CMAccountTracker {
    private final HashMap<String, TrackingBox> trackingMapByName = new HashMap<>();
    private final HashMap<InetSocketAddress, TrackingBox> trackingMapByIp = new HashMap<>();
    
    public void add(Player player, double amount) {
        // has to run every call because keys eventually get cleaned up, you dont want dangling IP associations
        attach(player);

        trackingMapByName.get(player.getName()).add(amount);
    }

    public boolean isExceeded(Player player) {
        attach(player);

        return trackingMapByName.get(player.getName()).get() >= CookieMonster.config.killTrackingLimit;
    }

    public void attach(Player player) {
        String name = player.getName();
        InetSocketAddress address = player.getAddress();

        // early break, to save on excess checks
        if (trackingMapByName.containsKey(name) && trackingMapByIp.containsKey(address)) return;

        if (!trackingMapByName.containsKey(name)) {
            if (trackingMapByIp.containsKey(address)) {
                // alt on same ip already tracked
                trackingMapByName.put(name, trackingMapByIp.get(address));
            } else {
                trackingMapByName.put(name, new TrackingBox());
            }
        }

        if (!trackingMapByIp.containsKey(address)) {
            // fresh player with new trackingMapByName box
            // or same name already used different IP and is trying to spoof via VPN
            trackingMapByIp.put(address, trackingMapByName.get(name));
        }
    }

    public void clear() {
        trackingMapByName.clear();
        trackingMapByIp.clear();
    }

    private static class TrackingBox {
        private double n;

        public void add(double amount) { n += amount; }
        public double get() { return n; }
    }
}
