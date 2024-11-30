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

package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.GlobalConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Environment(EnvType.CLIENT)
public class WildfireFirstTimeSetupScreen extends BaseWildfireScreen {

	private static final Text TITLE = Text.translatable("wildfire_gender.first_time_setup.title").formatted(Formatting.UNDERLINE);
	private static final Text DESCRIPTION = Text.translatable("wildfire_gender.first_time_setup.description");
	private static final Text NOTICE = Text.translatable("wildfire_gender.first_time_setup.notice");

	private static final Text ENABLE_CLOUD_SYNCING = Text.translatable("wildfire_gender.first_time_setup.enable").formatted(Formatting.GREEN);
	private static final Text DISABLE_CLOUD_SYNCING = Text.translatable("wildfire_gender.first_time_setup.disable").formatted(Formatting.RED);

	private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/first_time_bg.png");

	public WildfireFirstTimeSetupScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;

		// why must Java be?
		final var ref = new Object() {
			WildfireButton no = null;
		};

		this.addDrawableChild(new WildfireButton(x, y + 74, 128 - 5 - 1, 20,
				ENABLE_CLOUD_SYNCING,
				button -> {
					var config = GlobalConfig.INSTANCE;
					//Enable both settings, they can always disable automatic later? TBD
					config.cloudSyncEnabled.set(true);
					config.automaticCloudSync.set(true);
					config.firstTimeLoad.set(false);

					button.active = false;
					button.setMessage(Text.literal("..."));
					ref.no.setActive(false);

					final var nextScreen = new WardrobeBrowserScreen(null, client.player.getUuid());
					doInitialSync().thenRun(() -> client.execute(() -> client.setScreen(nextScreen)));
				}));


		this.addDrawableChild(ref.no = new WildfireButton(x - 128 + 6, y + 74, 128 - 5 - 1, 20,
				DISABLE_CLOUD_SYNCING,
				button -> {
					var config = GlobalConfig.INSTANCE;
					config.cloudSyncEnabled.set(false);
					config.automaticCloudSync.set(false);
					config.firstTimeLoad.set(false);

					client.setScreen(new WardrobeBrowserScreen(null, client.player.getUuid()));
				}));

		super.init();
	}

	private CompletableFuture<Void> doInitialSync() {
		var client = Objects.requireNonNull(this.client);
		var clientUUID = client.player.getUuid();
		return CompletableFuture.runAsync(() -> {
			var clientConfig = WildfireGender.getOrAddPlayerById(clientUUID);
			if(!clientConfig.hasLocalConfig()) {
				try {
					// note that we wait for this to ensure that we don't have any inconsistencies with the synced
					// data once we open the main menu
					WildfireGenderClient.loadGenderInfo(clientUUID, false, true).join();
				} catch(CompletionException ignored) {
					// loadGenderInfo should log any errors for us
					return;
				} catch(Exception e) {
					WildfireGender.LOGGER.error("Failed to perform initial sync from the cloud", e);
					return;
				}
				clientConfig.save();
				// don't immediately re-sync the data we just got back to the cloud
				clientConfig.needsCloudSync = false;
			} else {
				clientConfig.needsCloudSync = true;
			}
		});
	}

	@Override
	protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
		// TODO this makes the narration very long, but it's a lot better than having no narration.
		messageBuilder.put(NarrationPart.TITLE, TITLE);
		messageBuilder.put(NarrationPart.HINT, Text.empty().append(DESCRIPTION).append("\n\n").append(NOTICE));
		this.addElementNarrations(messageBuilder);
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);
		ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, (this.width - 256) / 2, (this.height - 200) / 2, 0, 0, 256, 200, 256, 256);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if(client == null || client.world == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		MatrixStack mStack = ctx.getMatrices();

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, x, y - 20, 0x000000);

		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, DESCRIPTION, x, y - 5, (int) ((256-10)), 4210752);


		mStack.push();
			mStack.translate(x, y + 47, 0);
			mStack.scale(0.8f, 0.8f, 1);
			mStack.translate(-x, -y - 47, 0);
		GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, NOTICE, x, y + 65, (int) ((256-10) * 1.2f), 4210752);
		mStack.pop();


	}

	@Override
	public void removed() {
		GlobalConfig.INSTANCE.save();
	}
}