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

import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.stream.Stream;

public class Forge189ClientSparkPlugin extends Forge189SparkPlugin {

    public static void register(Forge189SparkMod mod) {
        Forge189ClientSparkPlugin plugin = new Forge189ClientSparkPlugin(mod, Minecraft.getMinecraft());
        plugin.enable();

        // register listeners
        MinecraftForge.EVENT_BUS.register(plugin);

        // register commands
        ClientCommandHandler.instance.registerCommand(plugin);
    }

    private final Minecraft minecraft;
    private final ThreadDumper gameThreadDumper;

    public Forge189ClientSparkPlugin(Forge189SparkMod mod, Minecraft minecraft) {
        super(mod);
        this.minecraft = minecraft;
        this.gameThreadDumper = new ThreadDumper.Specific((Thread) ReflectionHelper.getPrivateValue(Minecraft.class, minecraft, "mcThread", "field_152352_aC", "aO"));
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper;
    }

    @Override
    public boolean hasPermission(ICommandSender sender, String permission) {
        return true;
    }

    @Override
    public Stream<Forge189CommandSender> getCommandSenders() {
        return Stream.of(new Forge189CommandSender(this.minecraft.thePlayer, this));
    }

    @Override
    public TickHook createTickHook() {
        return new Forge189TickHook(TickEvent.Type.CLIENT);
    }

    @Override
    public TickReporter createTickReporter() {
        return new Forge189TickReporter(TickEvent.Type.CLIENT);
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new Forge189WorldInfoProvider.Client(Minecraft.getMinecraft());
    }

    @Override
    public void executeSync(Runnable task) {
        this.minecraft.addScheduledTask(task);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new Forge189PlatformInfo(PlatformInfo.Type.CLIENT);
    }

    @Override
    public String getCommandName() {
        return "sparkc";
    }

}
