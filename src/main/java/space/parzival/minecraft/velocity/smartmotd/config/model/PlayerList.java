package space.parzival.minecraft.velocity.smartmotd.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PlayerList {
    /**
     * The number of players currently online.
     */
    private int current;

    /**
     * The maximum number of players that can be online at the same time.
     */
    private int max;

    /**
     * A list of player names currently online.
     */
    private List<String> samples;
}
