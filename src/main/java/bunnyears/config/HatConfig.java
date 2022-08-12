package bunnyears.config;

import bunnyears.BunnyEars;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HatConfig {

    public static final HatConfig EMPTY = new HatConfig(List.of());
    private static final ResourceLocation FILE_IDENTIFIER = new ResourceLocation(BunnyEars.MODID, "hats.json");

    private static HatConfig instance = EMPTY;

    public static final Codec<HatConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HatConfigSpec.CODEC.listOf().optionalFieldOf("hats", List.of()).forGetter(HatConfig::getHatConfigSpecs)
    ).apply(instance, HatConfig::new));

    private final List<HatConfigSpec> hatConfigSpecs;
    private final Map<String, HatConfigSpec> hatConfigMap;

    public HatConfig(List<HatConfigSpec> hatConfigSpecs) {
        this.hatConfigSpecs = new ArrayList<>(hatConfigSpecs);
        this.hatConfigMap = new HashMap<>();
        // create map
        for (HatConfigSpec spec : hatConfigSpecs) {
            this.hatConfigMap.put(spec.getName(), spec);
        }
    }

    public List<HatConfigSpec> getHatConfigSpecs() {
        return ImmutableList.copyOf(hatConfigSpecs);
    }

    public Map<String, HatConfigSpec> getHatConfigMap() {
        return ImmutableMap.copyOf(hatConfigMap);
    }

    public Collection<ResourceLocation> getModels() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        for (HatConfigSpec spec : hatConfigSpecs) {
            for (HatModel hatModel : spec.getModels()) {
                builder.add(hatModel.getModel());
            }
        }
        return builder.build();
    }

    @Nullable
    public ResourceLocation getModel(final String name, final int damagePercent) {
        HatConfigSpec spec = hatConfigMap.getOrDefault(name, HatConfigSpec.EMPTY);
        for (HatModel model : spec.getModels()) {
            if (damagePercent >= model.getDamage()) {
                return model.getModel();
            }
        }
        return null;
    }

    public void clear() {
        this.hatConfigMap.clear();
        this.hatConfigSpecs.clear();
    }

    public boolean isEmpty() {
        return this.hatConfigSpecs.isEmpty();
    }

    public void concat(final HatConfig other) {
        // create set to ensure no duplicates are added
        Set<HatConfigSpec> set = new HashSet<>(this.hatConfigSpecs);
        set.addAll(other.hatConfigSpecs);
        // clear this list and replace it with the contents of the set
        this.hatConfigSpecs.clear();
        this.hatConfigSpecs.addAll(set);
        // create map
        this.hatConfigMap.clear();
        for (HatConfigSpec spec : this.hatConfigSpecs) {
            this.hatConfigMap.put(spec.getName(), spec);
        }
    }

    public static HatConfig instance() {
        if (instance.isEmpty()) {
            loadConfig(Minecraft.getInstance().getResourceManager());
        }
        return instance;
    }

    public static void loadConfig(ResourceManager manager) {
        Gson gson = new Gson();
        try {
            // locate the resource
            List<Resource> list = manager.getResourceStack(FILE_IDENTIFIER);
            if(list.isEmpty()) {
                BunnyEars.LOGGER.error("Failed to locate HatConfig at " + FILE_IDENTIFIER);
            }
            // remove old resources
            instance.clear();

            // load each resource
            for(Resource inputStream : list) {
                // read the file
                Reader reader = new InputStreamReader(inputStream.open(), "UTF-8");
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                reader.close();

                // convert to HatConfig
                DataResult<HatConfig> result = HatConfig.CODEC.parse(JsonOps.INSTANCE, json);
                HatConfig config = result.resultOrPartial(s -> BunnyEars.LOGGER.error("Failed to parse HatConfig from file: " + s)).orElse(HatConfig.EMPTY);

                // combine with existing
                instance.concat(config);
            }
            BunnyEars.LOGGER.debug("Parsed HatConfig from file: " + instance.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "HatConfig{" + hatConfigMap + '}';
    }
}
