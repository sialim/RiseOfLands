package me.sialim.riseoflands.utilities;

import org.bukkit.Location;

public class RMath {
    public static double calculateDistanceTax(Location l1, Location l2) {
        double distance = l1.distance(l2);
        return 0.02 * Math.pow(distance, 1.12);
    }
}
