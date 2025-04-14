package space.parzival.minecraft.velocity.smartmotd.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

@Data
@NoArgsConstructor
public class PlayerList {
    /**
     * The number of players currently online.
     */
    private @Nullable Integer current;

    /**
     * The maximum number of players that can be online at the same time.
     */
    private @Nullable Integer max;

    /**
     * A list of player names currently online.
     */
    private @Nullable List<String> samples;
}
