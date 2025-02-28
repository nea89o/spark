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

package me.lucko.spark.forge.plugin;

import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.forge.ForgeClientCommandSender;
import me.lucko.spark.forge.ForgeClientTickHook;
import me.lucko.spark.forge.ForgeClientTickReporter;
import me.lucko.spark.forge.ForgePlatformInfo;
import me.lucko.spark.forge.ForgeServerCommandSender;
import me.lucko.spark.forge.ForgeSparkMod;
import me.lucko.spark.forge.ForgeWorldInfoProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import java.util.stream.Stream;

public class ForgeClientSparkPlugin extends ForgeSparkPlugin {

	public static ForgeClientSparkPlugin register(ForgeSparkMod mod, Minecraft minecraft) {
		ForgeClientSparkPlugin plugin = new ForgeClientSparkPlugin(mod, minecraft);
		plugin.enable();
		return plugin;
	}

	private final Minecraft minecraft;
	private final ThreadDumper gameThreadDumper;

	public ForgeClientSparkPlugin(ForgeSparkMod mod, Minecraft minecraft) {
		super(mod);
		this.minecraft = minecraft;
		assert minecraft.isCallingFromMinecraftThread();
		this.gameThreadDumper = new ThreadDumper.Specific(Thread.currentThread());
		registerCommands(ClientCommandHandler.instance, "sparkc", "sparkclient");
	}

	@Override
	public void enable() {
		super.enable();

		// register listeners
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public SparkPlatform getPlatform() {
		return platform;
	}

	@Override
	public CommandSender getCommandSender(ICommandSender sender) {
		return sender instanceof EntityPlayerSP
			       ? new ForgeClientCommandSender((EntityPlayerSP) sender)
			       : new ForgeServerCommandSender(sender);
	}

	@Override
	public Stream<ForgeClientCommandSender> getCommandSenders() {
		return Stream.of(new ForgeClientCommandSender(minecraft.thePlayer));
	}

	@Override
	public void executeSync(Runnable task) {
		this.minecraft.addScheduledTask(task);
	}

	@Override
	public ThreadDumper getDefaultThreadDumper() {
		return this.gameThreadDumper;
	}

	@Override
	public TickHook createTickHook() {
		return new ForgeClientTickHook();
	}

	@Override
	public TickReporter createTickReporter() {
		return new ForgeClientTickReporter();
	}

	@Override
	public WorldInfoProvider createWorldInfoProvider() {
		return new ForgeWorldInfoProvider.Client(this.minecraft);
	}

	@Override
	public PlatformInfo getPlatformInfo() {
		return new ForgePlatformInfo(PlatformInfo.Type.CLIENT);
	}

	@Override
	public String getCommandName() {
		return "sparkc";
	}
}
