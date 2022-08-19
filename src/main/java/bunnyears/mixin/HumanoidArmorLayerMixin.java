package bunnyears.mixin;

import bunnyears.config.HatConfig;
import bunnyears.config.HumanoidModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, A armorModel, CallbackInfo callback) {
        // do not run mixin when rendering other slots
        if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) {
            return;
        }
        // do not run mixin when item does not have custom name
        ItemStack itemstack = entity.getItemBySlot(equipmentSlot);
        if (!itemstack.hasCustomHoverName()) {
            return;
        }
        // determine item name and model id
        String name = itemstack.getHoverName().getString();
        int damagePercent = 0;
        if (itemstack.isDamageableItem() && itemstack.isDamaged()) {
            damagePercent = Mth.floor(100.0F * (float) itemstack.getDamageValue() / (float) itemstack.getMaxDamage());
        }
        // collect the model parts to use
        Map<HumanoidModelPart, ResourceLocation> models = HatConfig.instance().getModels(equipmentSlot, name, damagePercent);
        // do not run mixin when map is empty
        if(models.isEmpty()) {
            return;
        }

        // cancel original method
        callback.cancel();

        // prepare to render the model
        Minecraft mc = Minecraft.getInstance();
        RenderType rendertype = RenderType.cutout();
        VertexConsumer vertexBuilder = multiBufferSource.getBuffer(rendertype);

        // iterate over each model part and attempt to render
        HumanoidModelPart part;
        ResourceLocation modelId;
        BakedModel model;
        for(Map.Entry<HumanoidModelPart, ResourceLocation> entry : models.entrySet()) {
            part = entry.getKey();
            modelId = entry.getValue();
            // do not render when no model exists
            if(null == modelId) {
                continue;
            }
            // locate the model
            model = mc.getModelManager().getModel(modelId);
            // translate the model
            poseStack.pushPose();
            translateAndRotate(poseStack, part);
            Quaternion rotation = Vector3f.XP.rotationDegrees(180.0F);
            rotation.mul(Vector3f.YP.rotationDegrees(180));
            poseStack.mulPose(rotation);
            // render the model using item renderer
            // note: packed light flag 15728640 uses world light, 15728880 uses constant/full light
            mc.getItemRenderer().renderModelLists(model, itemstack, packedLight, 0, poseStack, vertexBuilder);
            // finish rendering
            poseStack.popPose();
        }
    }

    private void translateAndRotate(final PoseStack poseStack, final HumanoidModelPart part) {
        switch (part) {
            case HEAD:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).head.translateAndRotate(poseStack);
                poseStack.translate(0.5D, -0.5, -0.5D);
                break;
            case CHEST:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).body.translateAndRotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            case LEFT_ARM:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).leftArm.translateAndRotate(poseStack);
                poseStack.translate(9.0D / 16.0D, 10.0D / 16.0D, -0.5D);
                break;
            case RIGHT_ARM:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).rightArm.translateAndRotate(poseStack);
                poseStack.translate(7.0D / 16.0D, 10.0D / 16.0D, -0.5D);
                break;
            case LEFT_LEG:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).leftLeg.translateAndRotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            case RIGHT_LEG:
                ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).rightLeg.translateAndRotate(poseStack);
                poseStack.translate(0.5D, 0.75D, -0.5D);
                break;
            default:
                break;
        }
    }
}
