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
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.sampler.source.SourceMetadata;
import me.lucko.spark.common.util.SparkThreadFactory;
import me.lucko.spark.forge.ForgeClassSourceLookup;
import me.lucko.spark.forge.ForgeSparkMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public abstract class ForgeSparkPlugin implements SparkPlugin {

	private final ForgeSparkMod mod;
	private final Logger logger;
	protected final ScheduledExecutorService scheduler;

	protected SparkPlatform platform;

	protected ForgeSparkPlugin(ForgeSparkMod mod) {
		this.mod = mod;
		this.logger = LogManager.getLogger("spark");
		this.scheduler = Executors.newScheduledThreadPool(4, new SparkThreadFactory());
	}

	public void enable() {
		this.platform = new SparkPlatform(this);
		this.platform.enable();
	}

	public void disable() {
		this.platform.disable();
		this.scheduler.shutdown();
	}

	@Override
	public String getVersion() {
		return this.mod.getVersion();
	}

	@Override
	public Path getPluginDirectory() {
		return this.mod.getConfigDirectory();
	}

	@Override
	public void executeAsync(Runnable task) {
		this.scheduler.execute(task);
	}

	@Override
	public void log(Level level, String msg) {
		if (level.intValue() >= 1000) { // severe
			this.logger.error(msg);
		} else if (level.intValue() >= 900) { // warning
			this.logger.warn(msg);
		} else {
			this.logger.info(msg);
		}
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		if (level.intValue() >= 1000) { // severe
			this.logger.error(msg, throwable);
		} else if (level.intValue() >= 900) { // warning
			this.logger.warn(msg, throwable);
		} else {
			this.logger.info(msg, throwable);
		}
	}

	@Override
	public ClassSourceLookup createClassSourceLookup() {
		return new ForgeClassSourceLookup();
	}

	@Override
	public Collection<SourceMetadata> getKnownSources() {
		return SourceMetadata.gather(
			Loader.instance().getActiveModList(),
			ModContainer::getModId,
			ModContainer::getVersion,
			mod -> mod.getMetadata().getAuthorList(), // ?
			mod -> mod.getMetadata().description
		);
	}

	public abstract SparkPlatform getPlatform();

	public abstract CommandSender getCommandSender(ICommandSender sender) ;

	protected void registerCommands(CommandHandler handler, String name, String... aliases) {
		handler.registerCommand(new CommandBase() {
			@Override
			public String getCommandName() {
				return name;
			}

			@Override
			public List<String> getCommandAliases() {
				return Arrays.asList(aliases);
			}

			@Override
			public String getCommandUsage(ICommandSender sender) {
				return name + " help";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender sender) {
				return super.canCommandSenderUseCommand(sender) || getPlatformInfo().getType() == PlatformInfo.Type.CLIENT;
			}

			@Override
			public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
				return getPlatform().tabCompleteCommand(getCommandSender(sender), args);
			}

			@Override
			public void processCommand(ICommandSender sender, String[] args) throws CommandException {
				getPlatform().executeCommand(getCommandSender(sender), args);
			}
		});
	}
}
