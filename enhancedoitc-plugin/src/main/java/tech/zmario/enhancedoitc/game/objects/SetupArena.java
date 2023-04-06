package tech.zmario.enhancedoitc.game.objects;

import lombok.Data;
import org.bukkit.entity.Player;
import tech.zmario.enhancedoitc.game.enums.SetupAction;

@Data
public class SetupArena {

    private final Player player;
    private final String name;

    private final ArenaConfig arenaConfig;

    private SetupAction currentAction = null;
}
