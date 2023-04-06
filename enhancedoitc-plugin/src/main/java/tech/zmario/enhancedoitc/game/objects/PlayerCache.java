package tech.zmario.enhancedoitc.game.objects;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerCache {

    private final UUID uuid;
    private final String name;

    private int kills = 0, deaths = 0;

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }
}
