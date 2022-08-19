package bunnyears;

import bunnyears.config.HatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class BEClientEvents {

    public static class ModHandler {

        /**
         * Used to register models that are not associated with an item or block
         * @param event the Model Register Additional Event
         */
        @SubscribeEvent
        public static void registerModel(final ModelEvent.RegisterAdditional event) {
            for(ResourceLocation modelId : HatConfig.instance().getModelsToRegister()) {
                event.register(modelId);
            }
        }

        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(ModHandler::addResources);
        }

        public static void addResources() {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            if (resourceManager instanceof ReloadableResourceManager) {
                // reload hats
                ((ReloadableResourceManager) resourceManager).registerReloadListener(new SimplePreparableReloadListener<ModelBakery>() {

                    @Override
                    protected ModelBakery prepare(ResourceManager manager, ProfilerFiller profilerFiller) {
                        return null;
                    }

                    @Override
                    protected void apply(ModelBakery bakery, ResourceManager manager, ProfilerFiller profilerFiller) {
                        HatConfig.loadConfig(manager);
                    }
                });
            }
        }

    }
}
