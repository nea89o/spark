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

import com.google.common.collect.Queues;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.forge.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Queue;
import java.util.stream.Stream;

public class Forge189ServerSparkPlugin extends Forge189SparkPlugin {
    private final Queue<Runnable> scheduledServerTasks = Queues.newArrayDeque();

    private final ThreadDumper.GameThread gameThreadDumper;

    public static Forge189ServerSparkPlugin register(Forge189SparkMod mod, FMLServerStartingEvent event) {
        Forge189ServerSparkPlugin plugin = new Forge189ServerSparkPlugin(mod, event.getServer());
        plugin.enable();

        FMLCommonHandler.instance().bus().register(plugin);

        // register commands & permissions
        event.registerServerCommand(plugin);

        return plugin;
    }

    @Override
    public void disable() {
        super.disable();
        FMLCommonHandler.instance().bus().unregister(this);
    }

    @SubscribeEvent
    public void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            synchronized(scheduledServerTasks) {
                while (!scheduledServerTasks.isEmpty())
                {
                    scheduledServerTasks.poll().run();
                }
            }
        }
    }

    private final MinecraftServer server;

    public Forge189ServerSparkPlugin(Forge189SparkMod mod, MinecraftServer server) {
        super(mod);
        this.server = server;
        this.gameThreadDumper = new ThreadDumper.GameThread();
        this.gameThreadDumper.setThread(Thread.currentThread());
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper.get();
    }

    @Override
    public boolean hasPermission(ICommandSender sender, String permission) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if(isOp(player))
                return true;
            else {
                String serverOwner = MinecraftServer.getServer().getServerOwner();
                if(player.getGameProfile().getName() != null && serverOwner != null)
                    return serverOwner.equals(player.getGameProfile().getName());
                else
                    return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public Stream<Forge189CommandSender> getCommandSenders() {
        return Stream.concat(
            this.server.getConfigurationManager().playerEntityList.stream(),
            Stream.of(this.server)
        ).map(sender -> new Forge189CommandSender(sender, this));
    }

    @Override
    public TickHook createTickHook() {
        return new Forge189TickHook(TickEvent.Type.SERVER);
    }

    @Override
    public TickReporter createTickReporter() {
        return new Forge189TickReporter(TickEvent.Type.SERVER);
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new Forge189WorldInfoProvider.Server(FMLCommonHandler.instance().getMinecraftServerInstance());
    }

    @Override
    public void executeSync(Runnable task) {
        synchronized (scheduledServerTasks) {
            scheduledServerTasks.add(task);
        }
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new Forge189PlatformInfo(PlatformInfo.Type.SERVER);
    }

    @Override
    public String getCommandName() {
        return "spark";
    }
}
