package bunnyears.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class HatConfigSpec {

    public static final HatConfigSpec EMPTY = new HatConfigSpec(Map.of());

    /**
     * HatConfigSpec is defined the same way as a map where
     * key=HumanoidModelPart and value=one or more HatModels
     */
    public static final Codec<HatConfigSpec> CODEC = Codec.unboundedMap(HumanoidModelPart.CODEC,
                    Codec.either(HatModel.CODEC, HatModel.CODEC.listOf())
                            .xmap(either -> either.map(ImmutableList::of, Function.identity()),
                                    list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)))
            .xmap(HatConfigSpec::new, HatConfigSpec::getModels);

    private final Map<HumanoidModelPart, List<HatModel>> models;

    public HatConfigSpec(Map<HumanoidModelPart, List<HatModel>> models) {
        ImmutableMap.Builder<HumanoidModelPart, List<HatModel>> builder = ImmutableMap.builder();
        for (Map.Entry<HumanoidModelPart, List<HatModel>> entry : models.entrySet()) {
            // sort the list to ensure it can be used with durability values
            List<HatModel> list = new ArrayList<>(entry.getValue());
            list.sort(HatModel::compareTo);
            builder.put(entry.getKey(), list);
        }
        this.models = builder.build();
    }

    public Map<HumanoidModelPart, List<HatModel>> getModels() {
        return models;
    }

    public List<HatModel> getModels(HumanoidModelPart part) {
        return models.getOrDefault(part, List.of());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HatConfigSpec that = (HatConfigSpec) o;
        return Objects.equals(models, that.models);
    }

    @Override
    public int hashCode() {
        return Objects.hash(models);
    }

    @Override
    public String toString() {
        return "HatConfigSpec{" +
                "models=" + models +
                '}';
    }
}
