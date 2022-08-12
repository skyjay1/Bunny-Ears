package bunnyears.mixin;

import bunnyears.config.HatConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
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

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, A armorModel, CallbackInfo callback) {
        // do not run mixin when rendering other slots
        if (equipmentSlot != EquipmentSlot.HEAD) {
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
        ResourceLocation modelId = HatConfig.instance().getModel(name, damagePercent);
        // do not run mixin when no model exists for the given name
        if (null == modelId) {
            return;
        }
        // cancel original method
        callback.cancel();
        // prepare the model
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(modelId);
        RenderType rendertype = RenderType.cutout();
        VertexConsumer vertexBuilder = multiBufferSource.getBuffer(rendertype);
        // translate the model
        poseStack.pushPose();
        ((M)((HumanoidArmorLayer)(Object)this).getParentModel()).getHead().translateAndRotate(poseStack);
        //armorModel.getHead().translateAndRotate(poseStack);
        poseStack.translate(0.5D, -0.5, -0.5D);
        Quaternion rotation = Vector3f.XP.rotationDegrees(180.0F);
        rotation.mul(Vector3f.YP.rotationDegrees(180));
        poseStack.mulPose(rotation);
        // render model here
        // note: packed light flag 15728640 uses world light, 15728880 uses constant/full light
        mc.getItemRenderer().renderModelLists(model, itemstack, packedLight, 0, poseStack, vertexBuilder);
        // finish rendering
        poseStack.popPose();
    }
}
