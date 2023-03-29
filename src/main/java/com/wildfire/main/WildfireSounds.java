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

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WildfireSounds {
	private static Identifier SND1 = new Identifier(WildfireGender.MODID, "female_hurt1");
	public static SoundEvent FEMALE_HURT1 = SoundEvent.of(SND1);

	private static Identifier SND2 = new Identifier(WildfireGender.MODID, "female_hurt2");
	public static SoundEvent FEMALE_HURT2 = SoundEvent.of(SND2);

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, SND1, FEMALE_HURT1);
		Registry.register(Registries.SOUND_EVENT, SND2, FEMALE_HURT2);
	}
}
