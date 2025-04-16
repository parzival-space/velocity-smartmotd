package space.parzival.minecraft.velocity.smartmotd.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.function.Function;

public class PlaceholderParser {
    /**
     * Replaces all placeholders in the input string with the values from the placeholders map.
     * Placeholders are defined as {{placeholder_name}}.
     * @param input Replaceable string
     * @param placeholders Map of placeholders with their parameterized function
     * @param placeholderFunctionArgument The argument to be passed to the function
     * @return The input string with all placeholders replaced
     */
    public static <T> String injectPlaceholders(String input, Map<String, Function<T, String>> placeholders, T placeholderFunctionArgument) {
        for (Map.Entry<String, Function<T, String>> entry : placeholders.entrySet()) {
            input = input.replace(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue().apply(placeholderFunctionArgument)
            );
        }
        return input;
    }

    /**
     * Replaces all placeholders in the input string with the values from the placeholders map.
     * @param input Replaceable Adventure Component
     * @param placeholders Map of placeholders with their parameterized function
     * @param placeholderFunctionArgument The argument to be passed to the function
     * @return The input component with all placeholders replaced
     */
    public static <T> Component injectPlaceholders(Component input, Map<String, Function<T, String>> placeholders, T placeholderFunctionArgument) {
        String intermediate = MiniMessage.miniMessage().serialize(input);
        intermediate = injectPlaceholders(intermediate, placeholders, placeholderFunctionArgument);
        return MiniMessage.miniMessage().deserialize(intermediate);
    }
}
