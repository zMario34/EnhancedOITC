package tech.zmario.enhancedoitc.common.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class User {

    private final UUID uuid;

    private int kills, deaths, wins, losses;

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }
}
