package bunnyears.config;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class HatConfigSpec {

    public static final HatConfigSpec EMPTY = new HatConfigSpec("empty", List.of());

    public static final Codec<HatConfigSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(HatConfigSpec::getName),
            Codec.either(HatModel.CODEC, HatModel.CODEC.listOf())
                    .xmap(either -> either.map(ImmutableList::of, Function.identity()),
                            list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                    .optionalFieldOf("hat", List.of()).forGetter(HatConfigSpec::getModels)
    ).apply(instance, HatConfigSpec::new));


    private final String name;
    private final List<HatModel> models;

    public HatConfigSpec(String name, List<HatModel> models) {
        this.name = name;
        List<HatModel> list = new ArrayList<>(models);
        list.sort(HatModel::compareTo);
        this.models = ImmutableList.copyOf(list);
    }

    public String getName() {
        return name;
    }

    public List<HatModel> getModels() {
        return models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HatConfigSpec that = (HatConfigSpec) o;
        return Objects.equals(name, that.name) && Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, models);
    }

    @Override
    public String toString() {
        return "HatConfigSpec{" +
                "name='" + name + '\'' +
                ", models=" + models +
                '}';
    }
}
