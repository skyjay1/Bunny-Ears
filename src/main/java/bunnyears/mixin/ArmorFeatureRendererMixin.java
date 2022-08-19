package bunnyears.mixin;

import bunnyears.config.HatConfig;
import bunnyears.config.HumanoidModelPart;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Inject(method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void bunnyears$renderArmorPiece(MatrixStack poseStack, VertexConsumerProvider multiBufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, A armorModel, CallbackInfo callback) {
        // do not run mixin when rendering other slots
        if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        // do not run mixin when item does not have custom name
        ItemStack itemstack = entity.getEquippedStack(equipmentSlot);
        if (!itemstack.hasCustomName()) {
            return;
        }
        // determine item name and model id
        String name = itemstack.getName().getString();
        int damagePercent = 0;
        if (itemstack.isDamageable() && itemstack.isDamaged()) {
            damagePercent = MathHelper.floor(100.0F * (float) itemstack.getDamage() / (float) itemstack.getMaxDamage());
        }
        // collect the model parts to use
        Map<HumanoidModelPart, Identifier> models = HatConfig.instance().getModels(equipmentSlot, name, damagePercent);
        // do not run mixin when map is empty
        if(models.isEmpty()) {
            return;
        }

        // cancel original method
        callback.cancel();

        // prepare to render the model
        MinecraftClient mc = MinecraftClient.getInstance();
        RenderLayer rendertype = RenderLayer.getCutout();
        VertexConsumer vertexBuilder = multiBufferSource.getBuffer(rendertype);

        // iterate over each model part and attempt to render
        HumanoidModelPart part;
        Identifier modelId;
        BakedModel model;
        for(Map.Entry<HumanoidModelPart, Identifier> entry : models.entrySet()) {
            part = entry.getKey();
            modelId = entry.getValue();
            // do not render when no model exists
            if(null == modelId) {
                continue;
            }
            // locate the model
            model = BakedModelManagerHelper.getModel(mc.getBakedModelManager(), modelId);
            // do not render when no model exists
            if(null == model) {
                model = mc.getBakedModelManager().getModel(ModelLoader.MISSING_ID);
            }
            // translate the model
            poseStack.push();
            translateAndRotate(poseStack, part);
            Quaternion rotation = Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F);
            rotation.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
            poseStack.multiply(rotation);
            // render the model using item renderer
            // note: packed light flag 15728640 uses world light, 15728880 uses constant/full light
            ((ItemRendererAccessor)mc.getItemRenderer()).bunnyears$invokeRenderBakedItemModel(model, itemstack, packedLight, 0, poseStack, vertexBuilder);
            // finish rendering
            poseStack.pop();
        }
    }


    private void translateAndRotate(final MatrixStack poseStack, final HumanoidModelPart part) {
        switch (part) {
            case HEAD:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).head.rotate(poseStack);
                poseStack.translate(0.5D, -0.5, -0.5D);
                break;
            case CHEST:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).body.rotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            case LEFT_ARM:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).leftArm.rotate(poseStack);
                poseStack.translate(9.0D / 16.0D, 10.0D / 16.0D, -0.5D);
                break;
            case RIGHT_ARM:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).rightArm.rotate(poseStack);
                poseStack.translate(7.0D / 16.0D, 10.0D / 16.0D, -0.5D);
                break;
            case LEFT_LEG:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).leftLeg.rotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            case RIGHT_LEG:
                ((M) ((ArmorFeatureRenderer) (Object) this).getContextModel()).rightLeg.rotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            default:
                break;
        }
    }
}
