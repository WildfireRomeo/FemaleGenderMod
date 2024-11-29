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

package com.wildfire.main.entitydata;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.physics.BreastPhysics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>A stripped down version of a {@link PlayerConfig player's config}, intended for use with non-player entities.</p>
 *
 * <p>Unlike players, this has very minimal configuration support.</p>
 *
 * <p>Currently only used for {@link ArmorStandEntity armor stands}, and as a superclass for {@link PlayerConfig player configs}.</p>
 */
public class EntityConfig {

	public static final LoadingCache<UUID, EntityConfig> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(5))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull EntityConfig load(@NotNull UUID key) {
					return new EntityConfig(key);
				}
			});

	public final UUID uuid;
	protected final Configuration cfg;

	// note: hurt sounds, armor physics override, and show in armor are not defined here, as they have no relevance
	// to entities, and are instead entirely in PlayerConfig

	// TODO ideally these physics objects would be made entirely client-sided, but this class is
	//      used on both the client and server (primarily through PlayerConfig), making it very
	//      difficult to do so without some major changes to split this up further into a common class
	//      with a client extension class (e.g. the PlayerEntity & AbstractClientPlayerEntity classes)
	protected final BreastPhysics lBreastPhysics, rBreastPhysics;
	protected boolean jacketLayer = true;
	protected @Nullable BreastDataComponent fromComponent;

	protected EntityConfig(UUID uuid, Configuration config) {
		this.uuid = uuid;
		this.cfg = config;
		lBreastPhysics = new BreastPhysics(this);
		rBreastPhysics = new BreastPhysics(this);
	}

	protected EntityConfig(UUID uuid) {
		this(uuid, new Configuration(uuid.toString(), false));
	}

	/**
	 * Copy gender settings included in the given {@link ItemStack item NBT} to the current entity
	 *
	 * @see BreastDataComponent
	 */
	public void readFromStack(@NotNull ItemStack chestplate) {
		NbtComponent component = chestplate.get(DataComponentTypes.CUSTOM_DATA);
		if(chestplate.isEmpty() || component == null) {
			this.fromComponent = null;
			cfg.gender.set(Gender.MALE);
			return;
		} else if(fromComponent != null && Objects.equals(component, fromComponent.nbtComponent())) {
			// nothing's changed since the last time we checked, so there's no need to read from the
			// underlying nbt tag again
			return;
		}

		fromComponent = BreastDataComponent.fromComponent(component);
		if(fromComponent == null) {
			cfg.gender.set(Gender.MALE);
			return;
		}

		float size;
		cfg.physics.set(false);
		cfg.bustSize.set(size = fromComponent.breastSize());
		cfg.gender.set(size >= 0.02f ? Gender.FEMALE : Gender.MALE);
		cfg.breastsCleavage.set(fromComponent.cleavage());
		Breasts.updateOffsets(cfg, fromComponent.offsets());
		this.jacketLayer = fromComponent.jacket();
	}

	/**
	 * Get the configuration for a given entity
	 *
	 * @apiNote Configuration settings for {@link PlayerConfig}s may not be immediately available upon being
	 *          returned, and may take several seconds to be populated if loaded from the
	 *          {@link com.wildfire.main.cloud.CloudSync cloud sync server}.
	 *
	 * @return The relevant {@link EntityConfig}, or {@link PlayerConfig} if given a {@link PlayerEntity player}
	 */
	public static @NotNull EntityConfig getEntity(@NotNull LivingEntity entity) {
		if(entity instanceof PlayerEntity) {
			return WildfireGender.getOrAddPlayerById(entity.getUuid());
		}
		return CACHE.getUnchecked(entity.getUuid());
	}

	public @NotNull Gender getGender() {
		return cfg.gender.get();
	}

	public @NotNull Breasts getBreasts() {
		return new Breasts(cfg);
	}

	public float getBustSize() {
		return cfg.bustSize.get();
	}

	public boolean hasBreastPhysics() {
		return cfg.physics.get();
	}

	public boolean getArmorPhysicsOverride() {
		return false;
	}

	public boolean showBreastsInArmor() {
		return true;
	}

	public float getBounceMultiplier() {
		return cfg.bounceMultiplier.get();
	}

	public float getFloppiness() {
		return cfg.floppyMultiplier.get();
	}

	public float getVoicePitch() {
		return cfg.voicePitch.get();
	}

	public @NotNull BreastPhysics getLeftBreastPhysics() {
		return lBreastPhysics;
	}
	public @NotNull BreastPhysics getRightBreastPhysics() {
		return rBreastPhysics;
	}

	/**
	 * Only used in the case of {@link ArmorStandEntity armor stands}; returns {@code true} if the player who equipped
	 * the armor stand's chestplate has their jacket layer visible.
	 */
	public boolean hasJacketLayer() {
		return jacketLayer;
	}

	@Environment(EnvType.CLIENT)
	public void tickBreastPhysics(@NotNull LivingEntity entity) {
		IGenderArmor armor = WildfireHelper.getArmorConfig(entity.getEquippedStack(EquipmentSlot.CHEST));

		getLeftBreastPhysics().update(entity, armor);
		getRightBreastPhysics().update(entity, armor);
	}

	@Override
	public String toString() {
		return "%s(uuid=%s, gender=%s)".formatted(getClass().getCanonicalName(), uuid, cfg.gender.get());
	}
}
