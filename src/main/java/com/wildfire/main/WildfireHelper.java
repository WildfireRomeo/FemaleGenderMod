/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.main;

import com.wildfire.api.IGenderArmor;
import com.wildfire.api.WildfireAPI;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.render.armor.SimpleGenderArmor;
import com.wildfire.render.armor.EmptyGenderArmor;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class WildfireHelper {
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max + 1);
    }

    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmptyGenderArmor.INSTANCE;
        }

        if (WildfireAPI.getGenderArmors().get(stack.getItem()) != null) {
            return WildfireAPI.getGenderArmors().get(stack.getItem());
        } else {
            //TODO: Fabric Alternative to Capabilities? Maybe someone can help with this?
            if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST) {
                //Start by checking if it is a vanilla chestplate as we have custom configurations for those we check against
                // the armor material instead of the item instance in case any mods define custom armor items using vanilla
                // materials as then we can make a better guess at what we want the default implementation to be
                ArmorMaterial material = armorItem.getMaterial();
                if (material == ArmorMaterials.LEATHER) {
                    return SimpleGenderArmor.LEATHER;
                } else if (material == ArmorMaterials.CHAIN) {
                    return SimpleGenderArmor.CHAIN_MAIL;
                } else if (material == ArmorMaterials.GOLD) {
                    return SimpleGenderArmor.GOLD;
                } else if (material == ArmorMaterials.IRON) {
                    return SimpleGenderArmor.IRON;
                } else if (material == ArmorMaterials.DIAMOND) {
                    return SimpleGenderArmor.DIAMOND;
                } else if (material == ArmorMaterials.NETHERITE) {
                    return SimpleGenderArmor.NETHERITE;
                }
                //Otherwise just fallback to our default armor implementation
                return SimpleGenderArmor.FALLBACK;
            }
            //If it is not an armor item default as if "nothing is being worn that covers the breast area"
            // this might not be fully accurate and may need some tweaks but in general is likely relatively
            // close to the truth of if it should render or not. This covers cases such as the elytra and
            // other wearables
            return EmptyGenderArmor.INSTANCE;
        }
    }

    /**
     * <p>Write a player's gender config to NBT on the given item stack.</p>
     *
     * <p>This only copies enough data to render breasts similarly to how they'd appear on the given player, which includes:</p>
     * <ul>
     *     <li>{@link EntityConfig#getBustSize() Breast size}</li>
     *     <li>{@link Breasts#getCleavage() Cleavage}</li>
     *     <li>{@link Breasts#isUniboob() Uniboob}</li>
     *     <li>{@link Breasts#getXOffset() X}, {@link Breasts#getYOffset() Y}, and {@link Breasts#getZOffset() Z} offsets</li>
     *     <li>Whether the {@link PlayerEntity#isPartVisible player's jacket layer is visible}</li>
     * </ul>
     *
     * @see EntityConfig#readFromStack
     */
    public static void writeToNbt(@NotNull PlayerEntity player, @NotNull PlayerConfig config, @NotNull ItemStack armor) {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("BreastSize", config.getGender().canHaveBreasts() && config.showBreastsInArmor() ? config.getBustSize() : 0f);
        nbt.putFloat("Cleavage", config.getBreasts().getCleavage());
        nbt.putBoolean("Uniboob", config.getBreasts().isUniboob());
        nbt.putFloat("XOffset", config.getBreasts().getXOffset());
        nbt.putFloat("YOffset", config.getBreasts().getYOffset());
        nbt.putFloat("ZOffset", config.getBreasts().getZOffset());
        // note that we also copy this to properly copy the exact size, as the player model will push the breast armor
        // layer out a bit if they have a visible jacket layer
        nbt.putBoolean("Jacket", player.isPartVisible(PlayerModelPart.JACKET));
        armor.setSubNbt("WildfireGender", nbt);
    }
}