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

import com.mojang.realmsclient.util.Pair;
import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ForgeWorldInfoProvider implements WorldInfoProvider {

	protected void fillLevelChunkEntityCounts(World level, ChunksResult<ForgeChunkInfo> data) {
		Map<Pair<Integer, Integer>, ForgeChunkInfo> levelInfos = new HashMap<>();

		for (Entity entity : level.loadedEntityList) {
			ForgeChunkInfo info = levelInfos.computeIfAbsent(
				// TODO: pack ints to long
				Pair.of(entity.chunkCoordX, entity.chunkCoordZ), ForgeChunkInfo::new);
			info.entityCounts.increment(EntityList.getEntityID(entity));
		}

		data.put(level.getWorldInfo().getWorldName(), new ArrayList<>(levelInfos.values()));
	}

	@Override
	public Collection<DataPackInfo> pollDataPacks() {
		return Collections.emptyList();
	}

	public static final class Server extends ForgeWorldInfoProvider {
		private final MinecraftServer server;

		public Server(MinecraftServer server) {
			this.server = server;
		}

		@Override
		public CountsResult pollCounts() {
			int players = this.server.getCurrentPlayerCount();
			int entities = 0;
			int chunks = 0;
			int tileEntities = 0;

			for (WorldServer level : this.server.worldServers) {
				entities += level.loadedEntityList.size();
				chunks += level.theChunkProviderServer.getLoadedChunkCount();
				tileEntities += level.loadedTileEntityList.size();
			}

			return new CountsResult(players, entities, tileEntities, chunks);
		}

		@Override
		public ChunksResult<ForgeChunkInfo> pollChunks() {
			ChunksResult<ForgeChunkInfo> data = new ChunksResult<>();

			for (WorldServer level : this.server.worldServers) {
				fillLevelChunkEntityCounts(level, data);
			}

			return data;
		}

		@Override
		public GameRulesResult pollGameRules() {
			GameRulesResult data = new GameRulesResult();

			for (WorldServer level : this.server.worldServers) {
				String levelName = level.getWorldInfo().getWorldName();

				for (String rule : level.getGameRules().getRules()) {
					data.put(rule, levelName, level.getGameRules().getString(rule));
				}
			}

			return data;
		}

	}

	public static final class Client extends ForgeWorldInfoProvider {
		private final Minecraft client;

		public Client(Minecraft client) {
			this.client = client;
		}

		@Override
		public CountsResult pollCounts() {
			WorldClient level = this.client.theWorld;
			if (level == null) {
				return null;
			}


			int entities = level.loadedEntityList.size();
			int chunks = level.getChunkProvider().getLoadedChunkCount();

			return new CountsResult(-1, entities, -1, chunks);
		}

		@Override
		public ChunksResult<ForgeChunkInfo> pollChunks() {
			WorldClient level = this.client.theWorld;
			if (level == null) {
				return null;
			}

			ChunksResult<ForgeChunkInfo> data = new ChunksResult<>();

			fillLevelChunkEntityCounts(level, data);

			return data;
		}

		@Override
		public GameRulesResult pollGameRules() {
			// Not available on client since 24w39a
			return null;
		}
	}

	public static final class ForgeChunkInfo extends AbstractChunkInfo<Integer> {
		private final CountMap<Integer> entityCounts;

		ForgeChunkInfo(Pair<Integer, Integer> chunkPos) {
			super(chunkPos.first(), chunkPos.second());

			this.entityCounts = new CountMap.Simple<>(new HashMap<>());
		}

		@Override
		public CountMap<Integer> getEntityCounts() {
			return this.entityCounts;
		}

		@Override
		public String entityTypeName(Integer type) {
			return EntityList.getStringFromID(type);
		}
	}


}
