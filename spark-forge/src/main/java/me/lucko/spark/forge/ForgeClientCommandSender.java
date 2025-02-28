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

import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IChatComponent;

import java.util.UUID;

public class ForgeClientCommandSender extends AbstractCommandSender<EntityPlayerSP> {
	public ForgeClientCommandSender(EntityPlayerSP source) {
		super(source);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public UUID getUniqueId() {
		return delegate.getUniqueID();
	}

	@Override
	public void sendMessage(Component message) {
		IChatComponent component = IChatComponent.Serializer.jsonToComponent(GsonComponentSerializer.gson().serialize(message));
		delegate.addChatMessage(component);
	}

	@Override
	public boolean hasPermission(String permission) {
		return true;
	}
}
