package bunnyears;

import bunnyears.config.HatConfig;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.function.Consumer;


public class BunnyEars implements ClientModInitializer {
    public static final String MODID = "bunnyears";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        addResources();
        addModels();
    }

    private static void addModels() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider(new ExtraModelProvider() {
            @Override
            public void provideExtraModels(ResourceManager manager, Consumer<Identifier> out) {
                for(Identifier modelId : HatConfig.instance().getModelsToRegister()) {
                    out.accept(modelId);
                    LOGGER.debug("Registering model: " + modelId);
                }
            }
        });
    }

    private static void addResources() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {

                    final Identifier id = new Identifier(BunnyEars.MODID, "hat_config");

                    @Override
                    public Identifier getFabricId() {
                        return id;
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        HatConfig.loadConfig(manager);
                    }
                }
        );
    }
}