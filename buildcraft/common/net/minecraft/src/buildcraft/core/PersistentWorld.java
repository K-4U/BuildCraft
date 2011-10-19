/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import java.util.HashMap;
import java.util.TreeMap;

import net.minecraft.src.IBlockAccess;
import net.minecraft.src.WorldChunkManager;

public class PersistentWorld {
	
	private static HashMap<Long, PersistentWorld> worlds = new HashMap<Long, PersistentWorld>();
	private static Long lastBlockAccess = null;
	private static PersistentWorld lastWorld = null;
	
	private TreeMap<BlockIndex, PersistentTile> tiles = new TreeMap<BlockIndex, PersistentTile>();
	
	public PersistentTile createTile(PersistentTile defaultTile, BlockIndex index) {
		PersistentTile result = null;
		
		if (!tiles.containsKey(index)) {
			tiles.put(index, defaultTile);
			result = defaultTile;
		} else {
			result = tiles.get(index);
			
			if (result == defaultTile) {
			
			} else if (!result.getClass().equals(defaultTile.getClass())) {
				tiles.remove(index);
				tiles.put(index, defaultTile);
				result.destroy ();
				result = defaultTile;
			} else {
				defaultTile.destroy ();
			}			
		}
		
		return result;
	}
	
	public void storeTile(PersistentTile tile, BlockIndex index) {
		if (tiles.containsKey(index)) {
			PersistentTile old = tiles.get (index);
			
			if (old == tile) {
				return;
			}
			
			tiles.remove(index).destroy();			
		}
		
		tiles.put(index, tile);
	}
	
	public PersistentTile getTile(BlockIndex index) {
		return tiles.get(index);
	}
	
	public void removeTile(BlockIndex index) {
		if (!tiles.containsKey(index)) {
			tiles.remove(index).destroy ();
		}
	}
	
	public static PersistentWorld getWorld (IBlockAccess blockAccess) {
		Long hash = CoreProxy.getHash(blockAccess);
		if (!hash.equals(lastBlockAccess)) {
			if (!worlds.containsKey(hash)) {
				worlds.put(hash, new PersistentWorld());
			}
			
			lastBlockAccess = hash;
			lastWorld = worlds.get(hash);
		}
		
		return lastWorld;
	}
	
}
