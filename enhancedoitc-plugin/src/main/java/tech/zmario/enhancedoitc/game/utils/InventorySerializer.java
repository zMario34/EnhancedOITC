package tech.zmario.enhancedoitc.game.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import tech.zmario.enhancedoitc.game.objects.Kit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@UtilityClass
public class InventorySerializer {

    public String toBase64(ItemStack[] contents) throws IllegalStateException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(contents.length);

            for (ItemStack content : contents) {
                dataOutput.writeObject(content);
            }

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Error while serializing inventory", e);
        }
    }

    public ItemStack[] fromBase64(String data) {
        try {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] items = new ItemStack[dataInput.readInt()];

                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }

                return items;
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Error while deserializing inventory", e);
        }
    }

    public Kit deserialize(String kitItems, String kitArmor) {
        return new Kit(fromBase64(kitItems), fromBase64(kitArmor));
    }
}
