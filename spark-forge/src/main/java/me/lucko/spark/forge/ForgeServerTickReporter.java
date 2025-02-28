/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.forge;

import me.lucko.spark.common.tick.SimpleTickReporter;
import me.lucko.spark.common.tick.TickReporter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeServerTickReporter extends SimpleTickReporter implements TickReporter {

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e) {
		switch (e.phase) {
			case START:
				onStart();
				break;
			case END:
				onEnd();
				break;
		}
	}

	@Override
	public void start() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void close() {
		MinecraftForge.EVENT_BUS.unregister(this);
		super.close();
	}

}
