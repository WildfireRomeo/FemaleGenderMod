package com.wildfire.main.cloud;

import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SyncLog {
	public static final List<Entry> SYNC_LOG = new ArrayList<>();

	public static void add(Text text) {
		SYNC_LOG.add(new Entry(text, Instant.now()));
		if(SYNC_LOG.size() > 6) {
			SYNC_LOG.removeFirst();
		}
	}

	public record Entry(Text text, Instant timestamp) {
		public static final int NEW_COLOR = 0x00FF00;
		public static final int OLD_COLOR = 0x34A100;

		public int color() {
			long secondsPassed = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
			float delta = MathHelper.clamp(secondsPassed / 60f, 0f, 1f);
			return ColorHelper.lerp(delta, NEW_COLOR, OLD_COLOR);
		}
	}
}
