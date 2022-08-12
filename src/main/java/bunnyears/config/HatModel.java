package bunnyears.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HatModel implements Comparable<HatModel> {
    public static final HatModel EMPTY = new HatModel(0, new Identifier("empty"));

    public static final Codec<HatModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("damage", 0).forGetter(HatModel::getDamage),
            Identifier.CODEC.fieldOf("model").forGetter(HatModel::getModel)
    ).apply(instance, HatModel::new));

    private final int damage;
    private final Identifier model;

    public HatModel(int damage, Identifier model) {
        this.damage = damage;
        this.model = new Identifier(model.getNamespace(), "hat/" + model.getPath());
    }

    public int getDamage() {
        return damage;
    }

    public Identifier getModel() {
        return model;
    }

    @Override
    public int compareTo(@NotNull HatModel o) {
        return o.damage - this.damage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HatModel hatModel = (HatModel) o;
        return damage == hatModel.damage && Objects.equals(model, hatModel.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(damage, model);
    }

    @Override
    public String toString() {
        return "HatModel{" +
                "damage=" + damage +
                ", model=" + model +
                '}';
    }
}
