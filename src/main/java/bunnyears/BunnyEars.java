package bunnyears;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BunnyEars.MODID)
public class BunnyEars {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bunnyears";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public BunnyEars() {
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            LOGGER.error("BunnyEars was detected on the server, this is bad! BunnyEars is a client-side mod.");
        });

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().register(BEClientEvents.ModHandler.class);
            /*MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) ->
                    event.getEntity().displayClientMessage(Component.literal("You are using a beta version of Bunny Ears. Do not distribute.").withStyle(ChatFormatting.AQUA), false)
            );*/
        });
    }
}
