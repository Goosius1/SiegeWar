package com.gmail.goosius.siegewar.utils;

import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.object.TownyPermission.PermLevel;
import com.palmergames.bukkit.towny.object.TownyPermissionChange.Action;
import com.palmergames.bukkit.towny.object.PlotGroup;


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
    
    public static void disableNationPerms(Town town) {
        town.getPermissions().change(Action.PERM_LEVEL, false, PermLevel.NATION);

        for (TownBlock plot : town.getTownBlocks()) {
            if (plot.hasResident())
                continue;

			TownyPermission plotPerm = plot.getPermissions();
			if (plotPerm.getNationPerm(ActionType.BUILD) || plotPerm.getNationPerm(ActionType.DESTROY)
				|| plotPerm.getNationPerm(ActionType.ITEM_USE) || plotPerm.getNationPerm(ActionType.SWITCH)) {
			
				plot.getPermissions().change(Action.PERM_LEVEL, false, PermLevel.NATION);
				plot.save();
			}
        }
        town.save();
    }

	/**
	 * Sets pvp flags in a town to the desired setting.
	 *
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setTownPvpFlags(Town town, boolean desiredSetting) {
		//Set it in the town
		if (town.getPermissions().pvp != desiredSetting)
			town.getPermissions().pvp = desiredSetting;
		//Set it in all plots
		for(TownBlock townBlock: town.getTownBlocks()) {
			if (townBlock.getPermissions().pvp != desiredSetting)
				townBlock.getPermissions().pvp = desiredSetting;
		}
		//Set it in all plot groups
		if(town.getPlotGroups() != null) {
			for (PlotGroup plotGroup : town.getPlotGroups()) {
				if (plotGroup.getPermissions().pvp != desiredSetting)
					plotGroup.getPermissions().pvp = desiredSetting;
			}
		}
		town.save();
	}

	/**
	 * Sets explosion flags in a town to the desired setting.
	 *
	 * @param town The town to set the flags for.
	 * @param desiredSetting The value to set pvp and explosions to.
	 */
	public static void setTownExplosionFlags(Town town, boolean desiredSetting) {
		//Set it in the town
		if (town.getPermissions().explosion != desiredSetting)
			town.getPermissions().explosion = desiredSetting;
		//Set it in all plots
		for(TownBlock townBlock: town.getTownBlocks()) {
			if (townBlock.getPermissions().explosion != desiredSetting)
				townBlock.getPermissions().explosion = desiredSetting;
		}
		//Set it in all plot groups
		if(town.getPlotGroups() != null) {
			for (PlotGroup plotGroup : town.getPlotGroups()) {
				if (plotGroup.getPermissions().explosion != desiredSetting)
					plotGroup.getPermissions().explosion = desiredSetting;
			}
		}
		town.save();
	}
}