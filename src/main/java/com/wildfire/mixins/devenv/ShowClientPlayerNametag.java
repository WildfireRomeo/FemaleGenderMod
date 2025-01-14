/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.mixins.devenv;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import com.wildfire.main.config.GlobalConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfDevEnvironment
@Mixin(LivingEntityRenderer.class)
abstract class ShowClientPlayerNametag {
	@ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"))
	public boolean showClientPlayerNametag(boolean original, @Local(argsOnly = true) LivingEntity entity) {
		return original || (entity instanceof ClientPlayerEntity && GlobalConfig.INSTANCE.get(GlobalConfig.DEBUG_SHOW_NAMETAG));
	}
}
