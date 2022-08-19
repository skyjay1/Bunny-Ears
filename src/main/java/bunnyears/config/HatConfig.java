package bunnyears.config;

import bunnyears.BunnyEars;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HatConfig {

    public static final HatConfig EMPTY = new HatConfig(Map.of());
    private static final Identifier FILE_IDENTIFIER = new Identifier(BunnyEars.MODID, "hats.json");

    private static HatConfig instance = EMPTY;

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = Codec.STRING.xmap(EquipmentSlot::byName, EquipmentSlot::getName);

    public static final Codec<HatConfig> CODEC = Codec.unboundedMap(EQUIPMENT_SLOT_CODEC, Codec.unboundedMap(Codec.STRING, HatConfigSpec.CODEC))
            .xmap(HatConfig::new, HatConfig::getHatConfigSpecs);

    private final Map<EquipmentSlot, Map<String, HatConfigSpec>> hatConfigSpecs;

    public HatConfig(Map<EquipmentSlot, Map<String, HatConfigSpec>> hatConfigSpecs) {
        this.hatConfigSpecs = new HashMap<>(hatConfigSpecs);
    }

    public Map<EquipmentSlot, Map<String, HatConfigSpec>> getHatConfigSpecs() {
        return ImmutableMap.copyOf(hatConfigSpecs);
    }

    public Collection<Identifier> getModelsToRegister() {
        ImmutableSet.Builder<Identifier> builder = ImmutableSet.builder();
        for(Map<String, HatConfigSpec> map : getHatConfigSpecs().values()) {
            for(HatConfigSpec spec : map.values()) {
                for(List<HatModel> hatModelList : spec.getModels().values()) {
                    for(HatModel hatModel : hatModelList) {
                        builder.add(hatModel.getModel());
                    }
                }
            }
        }
        return builder.build();
    }

    public Map<HumanoidModelPart, Identifier> getModels(final EquipmentSlot slot, final String name, final int damagePercent) {
        // create a map to add return values
        Map<HumanoidModelPart, Identifier> value = new HashMap<>();
        // locate the map for the given equipment slot
        Map<String, HatConfigSpec> map = hatConfigSpecs.getOrDefault(slot, Map.of());
        // iterate through the hat models to find one that has the correct damage percent
        HatConfigSpec spec = map.getOrDefault(name, HatConfigSpec.EMPTY);
        for(Map.Entry<HumanoidModelPart, List<HatModel>> entry : spec.getModels().entrySet()) {
            for (HatModel model : entry.getValue()) {
                if (damagePercent >= model.getDamage()) {
                    // found the model to use, add it to the map
                    value.put(entry.getKey(), model.getModel());
                    break;
                }
            }
        }
        return value;
    }

    public void clear() {
        this.hatConfigSpecs.clear();
    }

    public boolean isEmpty() {
        return this.hatConfigSpecs.isEmpty();
    }

    public void concat(final HatConfig other) {
        // add each entry to this map
        for (Map.Entry<EquipmentSlot, Map<String, HatConfigSpec>> entry : other.getHatConfigSpecs().entrySet()) {
            Map<String, HatConfigSpec> map = this.hatConfigSpecs.computeIfAbsent(entry.getKey(), e -> new HashMap<>());
            map.putAll(entry.getValue());
        }
    }

    public static HatConfig instance() {
        if (instance.isEmpty()) {
            loadConfig(MinecraftClient.getInstance().getResourceManager());
        }
        return instance;
    }

    public static void loadConfig(ResourceManager manager) {
        Gson gson = new Gson();
        try {
            // locate the resource
            List<Resource> list = manager.getAllResources(FILE_IDENTIFIER);
            if(list.isEmpty()) {
                BunnyEars.LOGGER.error("Failed to locate HatConfig at " + FILE_IDENTIFIER);
            }
            // remove old resources
            instance.clear();

            // load each resource
            for(Resource inputStream : list) {
                // read the file
                Reader reader = new InputStreamReader(inputStream.getInputStream(), "UTF-8");
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
        return "HatConfig{" + hatConfigSpecs + '}';
    }
}
