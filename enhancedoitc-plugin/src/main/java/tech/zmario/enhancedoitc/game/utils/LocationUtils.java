package tech.zmario.enhancedoitc.game.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class LocationUtils {

    public static double[] deserializeLocationToDouble(String string) {
        String[] split = string.split(";");

        if (split.length > 3) {
            return new double[]{Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])};
        }

        return new double[]{0, 100, 0};
    }

    public static Location deserializeLocation(String string, World world) {
        String[] split = string.split(";");

        if (split.length == 3) {
            return new Location(world, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
        } else if (split.length >= 5) {
            return new Location(world, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                    Float.parseFloat(split[3]), Float.parseFloat(split[4]));
        }

        return new Location(world, 0, 100, 0);
    }

    public static String serializeLocation(Location location) {
        return location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }
}
