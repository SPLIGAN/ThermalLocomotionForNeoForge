package cofh.thermal.locomotion.common.item;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.common.energy.EnergyStorageCoFH;
import cofh.lib.util.CoFHItemData;
import cofh.thermal.locomotion.common.entity.EnergyMinecart;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

import static cofh.core.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.core.util.helpers.AugmentableHelper.setAttributeFromAugmentMax;
import static cofh.lib.api.ContainerType.ENERGY;
import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.StringHelper.*;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;

public class EnergyMinecartItem extends AugmentableMinecartItem implements IEnergyContainerItem {

    public EnergyMinecartItem(Properties builder) {

        super(EnergyMinecart::new, builder);
        setEnchantability(10);
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        boolean creative = isCreative(stack, ENERGY);
        if (getMaxEnergyStored(stack) > 0) {
            tooltip.add(creative
                    ? getTextComponent("info.cofh.infinite").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC)
                    : getTextComponent(localize("info.cofh.energy") + ": " + getScaledNumber(getEnergyStored(stack)) + " / " + getScaledNumber(getMaxEnergyStored(stack)) + " " + localize("info.cofh.unit_rf")));
        }
        addEnergyTooltip(stack, worldIn, tooltip, flagIn, getExtract(stack), getReceive(stack), creative);
    }

    protected void setAttributesFromAugment(ItemStack container, CompoundTag augmentData) {

        CompoundTag root = CoFHItemData.getTag(container);
        if (!root.contains(TAG_PROPERTIES, TAG_COMPOUND)) {
            return;
        }
        CompoundTag subTag = root.getCompound(TAG_PROPERTIES);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_STORAGE);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_XFER);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_CREATIVE);
    }

    // region IEnergyContainerItem
    @Override
    public CompoundTag getOrCreateEnergyTag(ItemStack container) {

        CompoundTag tag = CoFHItemData.getTag(container);
        if (!tag.contains(TAG_ENERGY_MAX)) {
            CoFHItemData.updateTag(container, root -> new EnergyStorageCoFH(EnergyMinecart.BASE_CAPACITY, EnergyMinecart.BASE_XFER).writeWithParams(root));
        }
        return CoFHItemData.getTag(container);
    }

    @Override
    public int getExtract(ItemStack container) {

        CompoundTag tag = getOrCreateEnergyTag(container);
        return Math.round(tag.getInt(TAG_ENERGY_SEND));
    }

    @Override
    public int getReceive(ItemStack container) {

        CompoundTag tag = getOrCreateEnergyTag(container);
        return Math.round(tag.getInt(TAG_ENERGY_RECV));
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {

        CompoundTag tag = getOrCreateEnergyTag(container);
        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_RF_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(tag.getInt(TAG_ENERGY_MAX) * mod * base));
    }
    // endregion

    // region IAugmentableItem
    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        CoFHItemData.updateTag(container, tag -> tag.put(TAG_PROPERTIES, new CompoundTag()));
        for (ItemStack augment : augments) {
            CompoundTag augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            setAttributesFromAugment(container, augmentData);
        }
        int energyExcess = getEnergyStored(container) - getMaxEnergyStored(container);
        if (energyExcess > 0) {
            setEnergyStored(container, getMaxEnergyStored(container));
        }
    }
    // endregion
}
