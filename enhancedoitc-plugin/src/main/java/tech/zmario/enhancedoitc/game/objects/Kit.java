package tech.zmario.enhancedoitc.game.objects;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class Kit {

    private final ItemStack[] items;
    private final ItemStack[] armor;

}
