package bunnyears.config;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HumanoidModelPart implements StringIdentifiable {
    HEAD("head_part"),
    CHEST("chest_part"),
    LEFT_ARM("left_arm_part"),
    RIGHT_ARM("right_arm_part"),
    LEFT_LEG("left_leg_part"),
    RIGHT_LEG("right_leg_part");

    public static final com.mojang.serialization.Codec<HumanoidModelPart> CODEC = com.mojang.serialization.Codec.STRING.xmap(HumanoidModelPart::getByName, HumanoidModelPart::asString);

    private static final Map<String, HumanoidModelPart> NAME_MAP = ImmutableMap.copyOf(Arrays.stream(values())
            .collect(Collectors.<HumanoidModelPart, String, HumanoidModelPart>toMap(HumanoidModelPart::asString, Function.identity())));

    private final String name;

    HumanoidModelPart(final String name) {
        this.name = name;
    }

    public static HumanoidModelPart getByName(final String name) {
        return NAME_MAP.getOrDefault(name.toLowerCase(), CHEST);
    }

    @Override
    public String asString() {
        return this.name;
    }
}
