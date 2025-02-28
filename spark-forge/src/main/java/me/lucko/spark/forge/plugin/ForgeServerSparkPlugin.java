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
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.forge.ForgePlatformInfo;
import me.lucko.spark.forge.ForgePlayerPingProvider;
import me.lucko.spark.forge.ForgeServerCommandSender;
import me.lucko.spark.forge.ForgeServerConfigProvider;
import me.lucko.spark.forge.ForgeSparkMod;
import me.lucko.spark.forge.ForgeServerTickHook;
import me.lucko.spark.forge.ForgeServerTickReporter;
import me.lucko.spark.forge.ForgeWorldInfoProvider;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.Arrays;
import java.util.stream.Stream;

public class ForgeServerSparkPlugin extends ForgeSparkPlugin {

	public static ForgeServerSparkPlugin register(ForgeSparkMod mod, MinecraftServer server) {
		ForgeServerSparkPlugin plugin = new ForgeServerSparkPlugin(mod, server);
		plugin.enable();
		return plugin;
	}

	private final MinecraftServer server;
	private final ThreadDumper gameThreadDumper;

	public ForgeServerSparkPlugin(ForgeSparkMod mod, MinecraftServer server) {
		super(mod);
		this.server = server;
		this.gameThreadDumper = new ThreadDumper.Specific(server.getServerThread());
	}

	@Override
	public void enable() {
		super.enable();

		// register commands
		registerCommands((ServerCommandManager) this.server.getCommandManager(), "spark", "sparkf");

		// register listeners
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		super.disable();

		// unregister listeners
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@Override
	public SparkPlatform getPlatform() {
		return platform;
	}

	@Override
	public CommandSender getCommandSender(ICommandSender sender) {
		return new ForgeServerCommandSender(sender);
	}

	@Override
	public Stream<ForgeServerCommandSender> getCommandSenders() {
		return Stream.concat(
			Arrays.stream(server.worldServers).flatMap(it -> it.playerEntities.stream()),
			Stream.of(server)
		).map(ForgeServerCommandSender::new);
	}

	@Override
	public void executeSync(Runnable task) {
		this.server.addScheduledTask(task);
	}

	@Override
	public ThreadDumper getDefaultThreadDumper() {
		return this.gameThreadDumper;
	}

	@Override
	public TickHook createTickHook() {
		return new ForgeServerTickHook();
	}

	@Override
	public TickReporter createTickReporter() {
		return new ForgeServerTickReporter();
	}

	@Override
	public PlayerPingProvider createPlayerPingProvider() {
		return new ForgePlayerPingProvider(this.server);
	}

	@Override
	public ServerConfigProvider createServerConfigProvider() {
		return new ForgeServerConfigProvider();
	}

	@Override
	public WorldInfoProvider createWorldInfoProvider() {
		return new ForgeWorldInfoProvider.Server(this.server);
	}

	@Override
	public PlatformInfo getPlatformInfo() {
		return new ForgePlatformInfo(PlatformInfo.Type.SERVER);
	}

	@Override
	public String getCommandName() {
		return "spark";
	}
}
