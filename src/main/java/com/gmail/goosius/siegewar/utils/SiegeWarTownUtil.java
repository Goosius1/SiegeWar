package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.listeners.SiegeWarCannonsListener;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Util class containing methods related to town flags/permssions.
 */
public class SiegeWarTownUtil {
    public static void disableTownPVP(Town town) {
		if (town.isPVP())
				town.setPVP(false);

		for (TownBlock plot : town.getTownBlocks()) {
			if (plot.getPermissions().pvp) {
				if (plot.getType() == TownBlockType.ARENA)
					plot.setType(TownBlockType.RESIDENTIAL);
			
				plot.getPermissions().pvp = false;
				plot.save();
			}
		}
		town.save();
    }

    /**
	 * Wrapper method to set pvp flags in a town to the desired 
	 * setting, as well as the nation if All-Nation-Sieges are enabled
	 * and the town has a nation.
	 * 
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setTownPvpFlags(Town town, boolean desiredSetting) {
		
		if(SiegeWarSettings.isAllNationSiegesEnabled() && town.hasNation()) {
			for(Town nationTown: TownyAPI.getInstance().getTownNationOrNull(town).getTowns()) {
				//Only non-peaceful towns are affected by the PVP change
				if(!nationTown.isNeutral())
					setPvpFlag(nationTown, desiredSetting);
			}
		} else {
			setPvpFlag(town, false);
		}
	}
	
    /**
	 * Sets pvp flags in a town to the desired setting.
	 * 
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	private static void setPvpFlag(Town town, boolean desiredSetting) {
		
		if (town.getPermissions().pvp != desiredSetting && SiegeWarSettings.getWarSiegePvpAlwaysOnInBesiegedTowns()) {
			town.getPermissions().pvp = desiredSetting;
			town.save();
		}
	}

	public static void warnPlayersInBesiegedTownArenaPlots() {
		for(Player player: Bukkit.getServer().getOnlinePlayers()) {
			Location playerLocation = player.getLocation();
			TownBlock townBlockAtPlayerLocation = TownyAPI.getInstance().getTownBlock(playerLocation);

			if (townBlockAtPlayerLocation != null
				&& townBlockAtPlayerLocation.hasTown()
				&& TownySettings.getKeepInventoryInArenas()
				&& townBlockAtPlayerLocation.getType() == TownBlockType.ARENA
				&& SiegeWarDistanceUtil.isLocationInActiveSiegeZone(playerLocation)) {

				//Warn player now TODO
				//"Arena-inventory-protection is deactivated at this location because you are in a siege zone".
				throw new RuntimeException("MISSING EXCEPTION");
			}
		}
	}
}