package bunnyears.config;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HumanoidModelPart implements StringRepresentable {
    HEAD("head_part"),
    CHEST("chest_part"),
    LEFT_ARM("left_arm_part"),
    RIGHT_ARM("right_arm_part"),
    LEFT_LEG("left_leg_part"),
    RIGHT_LEG("right_leg_part");

    public static final Codec<HumanoidModelPart> CODEC = Codec.STRING.xmap(HumanoidModelPart::getByName, HumanoidModelPart::getSerializedName);

    private static final Map<String, HumanoidModelPart> NAME_MAP = ImmutableMap.copyOf(Arrays.stream(values())
            .collect(Collectors.<HumanoidModelPart, String, HumanoidModelPart>toMap(HumanoidModelPart::getSerializedName, Function.identity())));

    private final String name;

    HumanoidModelPart(final String name) {
        this.name = name;
    }

    public static HumanoidModelPart getByName(final String name) {
        return NAME_MAP.getOrDefault(name.toLowerCase(), CHEST);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
