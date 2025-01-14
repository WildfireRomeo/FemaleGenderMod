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

package com.wildfire.main;

import com.wildfire.events.*;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import com.wildfire.gui.screen.WildfireFirstTimeSetupScreen;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.config.enums.Pronoun;
import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.ServerboundSyncPacket;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.render.GenderArmorLayer;
import com.wildfire.render.GenderLayer;
import com.wildfire.render.HolidayFeaturesRenderer;
import com.wildfire.render.RenderStateEntityCapture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class WildfireEventHandler {
	private WildfireEventHandler() {
		throw new UnsupportedOperationException();
	}

	private static final KeyBinding CONFIG_KEYBIND;
	private static final KeyBinding TOGGLE_KEYBIND;
	private static int timer = 0;

	public static KeyBinding getConfigKeybind() {
		return CONFIG_KEYBIND;
	}

	static {
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			// this has to be wrapped in a lambda to ensure that a dedicated server won't crash during startup
			// while executing this static block
			CONFIG_KEYBIND = Util.make(() -> {
				KeyBinding keybind = new KeyBinding("key.wildfire_gender.gender_menu", GLFW.GLFW_KEY_G, "category.wildfire_gender.generic");
				KeyBindingHelper.registerKeyBinding(keybind);
				return keybind;
			});
			TOGGLE_KEYBIND = Util.make(() -> {
				KeyBinding keybind = new KeyBinding("key.wildfire_gender.toggle", GLFW.GLFW_KEY_UNKNOWN, "category.wildfire_gender.generic");
				KeyBindingHelper.registerKeyBinding(keybind);
				return keybind;
			});
		} else {
			CONFIG_KEYBIND = null;
			TOGGLE_KEYBIND = null;
		}
	}

	/**
	 * Register all events applicable to the server-side for both a dedicated server and singleplayer
	 */
	public static void registerCommonEvents() {
		EntityTrackingEvents.START_TRACKING.register(WildfireEventHandler::onBeginTracking);
		ServerPlayConnectionEvents.DISCONNECT.register(WildfireEventHandler::playerDisconnected);
		ArmorStandInteractEvents.EQUIP.register(WildfireEventHandler::onEquipArmorStand);
		ArmorStandInteractEvents.REMOVE.register(BreastDataComponent::removeFromStack);
	}

	/**
	 * Register all client-side events
	 */
	@Environment(EnvType.CLIENT)
	public static void registerClientEvents() {
		ClientEntityEvents.ENTITY_UNLOAD.register(WildfireEventHandler::onEntityUnload);
		ClientTickEvents.END_CLIENT_TICK.register(WildfireEventHandler::onClientTick);
		ClientPlayConnectionEvents.DISCONNECT.register(WildfireEventHandler::clientDisconnect);
		ClientPlayConnectionEvents.JOIN.register(WildfireEventHandler::clientJoin);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(WildfireEventHandler::registerRenderLayers);
		HudRenderCallback.EVENT.register(WildfireEventHandler::renderHud);
		ArmorStatsTooltipEvent.EVENT.register(WildfireEventHandler::renderTooltip);
		EntityHurtSoundEvent.EVENT.register(WildfireEventHandler::onEntityHurt);
		EntityTickEvent.EVENT.register(WildfireEventHandler::onEntityTick);
		PlayerNametagRenderEvent.EVENT.register(WildfireEventHandler::onPlayerNametag);
	}

	private static void renderSmallNametag(Text tag, MatrixStack matrixStack, PlayerEntity player, Consumer<Text> renderHelper) {
		matrixStack.push();
		float translationAmt = switch(player.getPose()) {
			case EntityPose.SWIMMING, EntityPose.GLIDING -> 0.333f;
			case EntityPose.CROUCHING -> 0.8f;
			case EntityPose.SLEEPING -> 0.125f;
			case EntityPose.SITTING -> 0.275f;
			default -> 0.95f;
		};
		matrixStack.translate(0f, translationAmt, 0f);
		matrixStack.scale(0.5f, 0.5f, 0.5f);
		renderHelper.accept(tag);
		matrixStack.pop();
		// shift the rest of the name tag up a little bit
		matrixStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
	}

	@Environment(EnvType.CLIENT)
	private static void onPlayerNametag(PlayerEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Consumer<Text> renderHelper) {
		var player = ((RenderStateEntityCapture) state).getEntity() instanceof PlayerEntity p ? p : null;
		if(player == null) return;

		var lines = new ArrayList<Text>();
		var config = WildfireGender.getPlayerById(player.getUuid());

		var pronouns = config != null ? Pronoun.format(config.getConfig().get(Configuration.PRONOUNS)) : null;
		if(pronouns != null) {
			lines.add(Text.literal(pronouns).formatted(Formatting.GRAY));
		}

		var contributorTag = WildfireGenderClient.getNametag(player.getUuid());
		if(contributorTag != null) {
			lines.add(contributorTag);
		}

		var iterator = lines.iterator();
		while(iterator.hasNext()) {
			var line = iterator.next();
			renderSmallNametag(line, matrixStack, player, renderHelper);
			if(iterator.hasNext()) {
				matrixStack.translate(0f, 2.15F * 1.15F * 0.025F, 0f);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private static void renderTooltip(ItemStack item, Consumer<Text> tooltipAppender, @Nullable PlayerEntity player) {
		if(player == null || !GlobalConfig.INSTANCE.get(GlobalConfig.ARMOR_STAT)) return;
		var playerConfig = WildfireGender.getPlayerById(player.getUuid());
		if(playerConfig == null || !playerConfig.getGender().canHaveBreasts()) return;

		var equippableComponent = item.get(DataComponentTypes.EQUIPPABLE);
		if(equippableComponent != null && equippableComponent.slot() == EquipmentSlot.CHEST) {
			float physResistance = (WildfireHelper.getArmorConfig(item).physicsResistance());
			tooltipAppender.accept(Text.translatable("wildfire_gender.armor.tooltip", Math.floor(physResistance * 100f) / 100f).formatted(Formatting.LIGHT_PURPLE));
		}
	}

	@Environment(EnvType.CLIENT)
	private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		var textRenderer = Objects.requireNonNull(MinecraftClient.getInstance().textRenderer, "textRenderer");
		if(MinecraftClient.getInstance().currentScreen instanceof WardrobeBrowserScreen) {
			return;
		}

		/*if(MinecraftClient.getInstance().player != null) {
			PlayerConfig pCfg = WildfireGender.getPlayerById(MinecraftClient.getInstance().player.getUuid());
			if(pCfg != null) {
				context.drawText(textRenderer, "Physics Debug", 5, 5, 0xFFFFFF, true);
				context.drawText(textRenderer, "Position: " + pCfg.getLeftBreastPhysics().getPositionX() + "," + pCfg.getLeftBreastPhysics().getPositionY(), 5, 15, 0xFFFFFF, true);
				context.drawText(textRenderer, "Breast Size: " + pCfg.getLeftBreastPhysics().getBreastSize(tickCounter.getTickDelta(false)), 5, 35, 0xFFFFFF, true);
			}
		}*/
		boolean shouldShow = switch(GlobalConfig.INSTANCE.get(GlobalConfig.ALWAYS_SHOW_LIST)) {
			case MOD_UI_ONLY -> false;
			case TAB_LIST_OPEN -> MinecraftClient.getInstance().options.playerListKey.isPressed();
			case ALWAYS -> true;
		};
		if(!shouldShow) return;

		GuiUtils.drawSyncedPlayers(context, textRenderer, collectPlayerEntries());
	}

	/**
	 * Attach breast render layers to players and armor stands
	 */
	@Environment(EnvType.CLIENT)
	private static void registerRenderLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?, ?> entityRenderer,
	                                         LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper,
	                                         EntityRendererFactory.Context context) {
		if(entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
			registrationHelper.register(new GenderLayer<>(playerRenderer));
			registrationHelper.register(new GenderArmorLayer<>(playerRenderer, context.getEquipmentModelLoader(), context.getEquipmentRenderer()));
			registrationHelper.register(new HolidayFeaturesRenderer(playerRenderer));
		} else if(entityRenderer instanceof ArmorStandEntityRenderer armorStandRenderer) {
			registrationHelper.register(new GenderArmorLayer<>(armorStandRenderer, context.getEquipmentModelLoader(), context.getEquipmentRenderer()));
		}
	}

	/**
	 * Remove (non-player) entities from the client cache when they're unloaded
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityUnload(Entity entity, World world) {
		// note that we don't attempt to unload players; they're instead only ever unloaded once we leave a world,
		// or once they disconnect
		EntityConfig.CACHE.invalidate(entity.getUuid());
	}

	/**
	 * Perform various actions that should happen once per client tick, such as syncing client player settings
	 * to the server.
	 */
	@Environment(EnvType.CLIENT)
	private static void onClientTick(MinecraftClient client) {
		if(client.world == null || client.player == null) return;

		PlayerConfig clientConfig = WildfireGender.getPlayerById(client.player.getUuid());
		timer++;

		// Only attempt to sync if the server will accept the packet, and only once every 5 ticks, or around 4 times a second
		if(ServerboundSyncPacket.canSend() && timer % 5 == 0) {
			// sendToServer will only actually send a packet if any changes have been made that need to be synced,
			// or if we haven't synced before.
			if(clientConfig != null) WildfireSync.sendToServer(clientConfig);
		}

		if(timer % 40 == 0) {
			CloudSync.sendNextQueueBatch();
			if(clientConfig != null) clientConfig.attemptCloudSync();
		}

		if(TOGGLE_KEYBIND.wasPressed() && client.currentScreen == null) {
			GlobalConfig.RENDER_BREASTS ^= true;
		}
		if(CONFIG_KEYBIND.wasPressed() && client.currentScreen == null) {
			if(GlobalConfig.INSTANCE.get(GlobalConfig.FIRST_TIME_LOAD) && CloudSync.isAvailable()) {
				client.setScreen(new WildfireFirstTimeSetupScreen(null, client.player.getUuid()));
			} else {
				client.setScreen(new WardrobeBrowserScreen(null, client.player.getUuid()));
			}
		}
	}

	/**
	 * Clears all caches when the client player disconnects from a server/closes a singleplayer world
	 */
	@Environment(EnvType.CLIENT)
	private static void clientDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient client) {
		WildfireGender.CACHE.invalidateAll();
		EntityConfig.CACHE.invalidateAll();
	}

	@Environment(EnvType.CLIENT)
	private static void clientJoin(ClientPlayNetworkHandler var1, PacketSender var2, MinecraftClient client) {
		if (client.player == null) return;
		/*if (WildfireGender.getPlayerById(client.player.getUuid()) == null) {
			var button = WildfireEventHandler.CONFIG_KEYBIND.getBoundKeyLocalizedText();
			ToastManager toastManager = client.getToastManager();
			toastManager.add(new WildfireToast(MinecraftClient.getInstance().textRenderer, Text.translatable("wildfire_gender.player_list.title"), Text.translatable("toast.wildfire_gender.get_started", button), false, 0));
		}*/
	}

	/**
	 * Removes a disconnecting player from the cache on a server
	 */
	private static void playerDisconnected(ServerPlayNetworkHandler handler, MinecraftServer server) {
		WildfireGender.CACHE.invalidate(handler.getPlayer().getUuid());
	}

	/**
	 * Send a sync packet when a player enters the render distance of another player
	 */
	private static void onBeginTracking(Entity tracked, ServerPlayerEntity syncTo) {
		if(tracked instanceof PlayerEntity toSync) {
			PlayerConfig genderToSync = WildfireGender.getPlayerById(toSync.getUuid());
			if(genderToSync == null) return;
			// Note that we intentionally don't check if we've previously synced a player with this code path;
			// because we use entity tracking to sync, it's entirely possible that one player would leave the
			// tracking distance of another, change their settings, and then re-enter their tracking distance;
			// we wouldn't sync while they're out of tracking distance, and as such, their settings would be out
			// of sync until they relog.
			WildfireSync.sendToClient(syncTo, genderToSync);
		}
	}

	/**
	 * Play the relevant mod hurt sound when a player takes damage
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityHurt(LivingEntity entity, DamageSource damageSource) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player == null || client.world == null) return;
		if(!(entity instanceof PlayerEntity player) || !player.getWorld().isClient()) return;

		PlayerConfig genderPlayer = WildfireGender.getPlayerById(player.getUuid());
		if(genderPlayer == null || !genderPlayer.hasHurtSounds()) return;

		SoundEvent hurtSound = genderPlayer.getGender().getHurtSound();
		if(hurtSound != null) {
			float pitchVariation = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F;
			player.playSound(hurtSound, 1f, pitchVariation + genderPlayer.getVoicePitch());
		}
	}

	/**
	 * Tick breast physics on entity tick
	 */
	@Environment(EnvType.CLIENT)
	private static void onEntityTick(LivingEntity entity) {
		if(EntityConfig.isSupportedEntity(entity)) {
			EntityConfig cfg = EntityConfig.getEntity(entity);
			if(entity instanceof ArmorStandEntity) {
				cfg.readFromStack(entity.getEquippedStack(EquipmentSlot.CHEST));
			}
			cfg.tickBreastPhysics(entity);
		}
	}

	/**
	 * Apply player settings to chestplates equipped onto armor stands
	 */
	private static void onEquipArmorStand(PlayerEntity player, ItemStack item) {
		PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUuid());
		if(playerConfig == null) {
			// while we shouldn't have our tag on the stack still, we're still checking to catch any armor
			// that may still have the tag from older versions, or from potential cross-mod interactions
			// which allow for removing items from armor stands without calling the vanilla
			// #equip and/or #onBreak methods
			BreastDataComponent.removeFromStack(item);
			return;
		}

		// Note that we always attach player data to the item stack as a server has no concept of resource packs,
		// making it impossible to compare against any armor data that isn't registered through the mod API.
		BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
		if(component != null) {
			component.write(player.getWorld().getRegistryManager(), item);
		}
	}


	public static List<PlayerListEntry> collectPlayerEntries() {
		if(MinecraftClient.getInstance().player == null) return new ArrayList<>();
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		return player.networkHandler.getListedPlayerListEntries().stream()
				.filter(entry -> !entry.getProfile().getId().equals(player.getUuid()))
				.filter(entry -> {
					var cfg = WildfireGender.getPlayerById(entry.getProfile().getId());
					return cfg != null && cfg.getSyncStatus() != PlayerConfig.SyncStatus.UNKNOWN;
				})
				.limit(40L)
				.toList();
	}
}
