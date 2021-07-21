package com.gmail.goosius.siegewar.tasks;

import com.gmail.goosius.siegewar.SiegeWar;
import com.gmail.goosius.siegewar.settings.ConfigNodes;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.utils.SiegeWarMapUtil;
import net.pl3x.map.api.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pl3xMapTask {

    static Plugin pl3xMapPlugin;
    private static final Map<String, SimpleLayerProvider> worldProviders = new ConcurrentHashMap<>();
    static boolean stop;
    private static Map<UUID, Marker> townUUDToSiegeMarkerMap = new HashMap<>();
    final static String PEACEFUL_BANNER_ICON_ID = "fire";
    final static String BATTLE_BANNER_ICON_ID = "siegewar.battle";

    public static void setupTask(Plugin plugin) {
        pl3xMapPlugin = plugin;

        // Load world providers
        
        //Setup the siegewar layer
        Pl3xMap api = Pl3xMapProvider.get();
        for (String worldName : SiegeWarSettings.getWarSiegeWorlds()) {
        
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                SiegeWar.getSiegeWar().getLogger().severe("Error accessing world " + worldName + "!");
                continue;
            }

            MapWorld mapWorld = api.getWorldIfEnabled(world).orElse(null);
            if (mapWorld == null) {
                SiegeWar.getSiegeWar().getLogger().severe(worldName + " is not an enabled world for Pl3xMap!");
                continue;
            }

            SimpleLayerProvider siegeWarLayerProvider = 
                SimpleLayerProvider
                .builder("SiegeWar")
                .showControls(true)
                .defaultHidden(false)
                .layerPriority(5)
                .zIndex(250)
                .build();
            
            mapWorld.layerRegistry().register(Key.of("siegewar"), siegeWarLayerProvider);
            worldProviders.put(world.getName(), siegeWarLayerProvider);
        }

        //Load the dormant siege icon
        BufferedImage dormantSiegeIcon = plugin.config().loadTownIcon(plugin.getLogger());
        if (dormantSiegeIcon != null)
            api.iconRegistry().register( Key.of("siegewar_dormant_siege"), dormantSiegeIcon);
            
        //Load the active siege icon
        BufferedImage activeSiegeIcon = plugin.config().loadTownIcon(plugin.getLogger());
        if (activeSiegeIcon != null)
            api.iconRegistry().register( Key.of("siegewar_active_siege"), activeSiegeIcon);


            
             Key.of("towny_town_icon");

        //pl3xMapPlugin = plugin;
        //Pl3xMap 
        //pl3xMapApi
        /*
        
        markerapi = dynmapAPI.getMarkerAPI();
        
        if (markerapi == null) {
            System.err.println(SiegeWar.prefix + "Error loading dynmap marker API!");
            return;
        }

        //Create siegewar marker set
        siegeWarMarkerSet = markerapi.getMarkerSet("siegewar.markerset");
        if (siegeWarMarkerSet == null) {
            siegeWarMarkerSet = markerapi.createMarkerSet("siegewar.markerset", "SiegeWar", null, false);
        } else
            siegeWarMarkerSet.setMarkerSetLabel("SiegeWar");

        if (siegeWarMarkerSet == null) {
            System.err.println(SiegeWar.prefix + "Error creating dynmap marker set");
            return;
        }

        //Get towny marker set
        townyMarkerSet = markerapi.getMarkerSet("towny.markerset");

        //Create battle banner marker icon
        InputStream png = SiegeWar.getSiegeWar().getResource(Settings.BATTLE_BANNER_FILE_NAME);
        markerapi.createMarkerIcon(BATTLE_BANNER_ICON_ID, "BattleBanner", png);
        */
        //startTask();
        System.out.println(SiegeWar.prefix + "Plx3 task setup.");
    }

    public static void startTask() {
        stop = false;
        Bukkit.getScheduler().runTaskTimerAsynchronously(SiegeWar.getSiegeWar(), () -> {
            if (!stop) {
                applyMapHidingToPlayers();
                displaySieges();
            }
        }, 40l, 300l);
        System.out.println(SiegeWar.prefix + "Plx3 task setup.");
    }

    public static void endDynmapTask() {
        stop = true;
    }

    /**
     * Remove markers belonging to sieges that have ended
     * Also change any icons if required (between peaceful icon & battle icon)
     */
    private static void displaySieges() {



        /*
        Map<UUID, Marker> townUUDToSiegeMarkerMapCopy = new HashMap<>(townUUDToSiegeMarkerMap);

        {
        
        
        
        
        
            //Cleanup current siege markers
            UUID townUUID = null;
            Marker marker = null;
            for (Map.Entry<UUID, Marker> mapEntry : townUUDToSiegeMarkerMapCopy.entrySet()) {
                try {
                    marker = null;
                    townUUID = null;
                    townUUID = mapEntry.getKey();
                    marker = mapEntry.getValue();
                    Siege siege = SiegeController.getSiegeByTownUUID(townUUID);

                    if (siege == null || siege.getStatus() != SiegeStatus.IN_PROGRESS) {
                        //Delete marker if siege is not in progress
                        marker.deleteMarker();
                        townUUDToSiegeMarkerMap.remove(townUUID);

                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(PEACEFUL_BANNER_ICON_ID)) {
                        
                         // Change to battle icon if battle is active.          
                        if (BattleSession.getBattleSession().isActive()
                                && (siege.getAttackerBattlePoints() > 0
                                || siege.getDefenderBattlePoints() > 0
                                || siege.getBannerControllingSide() != SiegeSide.NOBODY
                                || siege.getBannerControlSessions().size() > 0)) {
                            marker.setMarkerIcon(markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID));
                        }

                    } else if (marker.getMarkerIcon().getMarkerIconID().equals(BATTLE_BANNER_ICON_ID)) {
                        
                         //Change to peaceful icon if battle is no longer active.
                         
                        if (!BattleSession.getBattleSession().isActive()
                                || (siege.getAttackerBattlePoints() == 0
                                && siege.getDefenderBattlePoints() == 0
                                && siege.getBannerControllingSide() == SiegeSide.NOBODY
                                && siege.getBannerControlSessions().size() == 0)) {
                            marker.setMarkerIcon(markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID));
                        }
                    }
                } catch (Exception e) {
                    if (marker != null)
                        marker.deleteMarker();
                    townUUDToSiegeMarkerMap.remove(townUUID);
                }
            }
        }

        {
            //Add siege marker if required
            for (Siege siege : SiegeController.getSieges()) {

                String name = Translation.of("dynmap_siege_title", siege.getAttackerNameForDisplay(), siege.getDefenderNameForDisplay());
                try {
                    if (siege.getStatus().isActive()) {
                        //If anyone is in a BC session or on the BC list, it is a fire & swords icon
                        //otherwise just fire
                        MarkerIcon siegeIcon;
                        if (siege.getBannerControllingSide() == SiegeSide.NOBODY
                                && siege.getBannerControlSessions().size() == 0) {
                            siegeIcon = markerapi.getMarkerIcon(PEACEFUL_BANNER_ICON_ID);
                        } else {
                            siegeIcon = markerapi.getMarkerIcon(BATTLE_BANNER_ICON_ID);
                        }
                        List<String> lines = new ArrayList<>();
                        lines.add(Translation.of("dynmap_siege_town", siege.getTown().getName()));
                        lines.add(Translation.of("dynmap_siege_type", siege.getSiegeType().getName()));
                        lines.add(Translation.of("dynmap_siege_balance", siege.getSiegeBalance()));
                        lines.add(Translation.of("dynmap_siege_time_left", siege.getTimeRemaining()));
                        if(TownyEconomyHandler.isActive()) {
                            lines.add(Translation.of("dynmap_siege_war_chest", TownyEconomyHandler.getFormattedBalance(siege.getWarChestAmount())));
                        }
                        lines.add(Translation.of("dynmap_siege_banner_control", siege.getBannerControllingSide().name().charAt(0) + siege.getBannerControllingSide().name().substring(1).toLowerCase()));
                        lines.add(Translation.of("dynmap_siege_battle_points", siege.getFormattedAttackerBattlePoints(), siege.getFormattedDefenderBattlePoints()));
                        lines.add(Translation.of("dynmap_siege_battle_time_left", siege.getFormattedBattleTimeRemaining()));

                        String desc = "<b>" + name + "</b><hr>" + StringMgmt.join(lines, "<br>");
                        Location siegeLoc = siege.getFlagLocation();
                        double siegeX = siegeLoc.getX();
                        double siegeZ = siegeLoc.getZ();
                        String siegeMarkerId = siege.getTown().getUUID().toString();
                        Marker siegeMarker = siegeWarMarkerSet.findMarker(siegeMarkerId);
                        if (siegeMarker == null) {
                            siegeWarMarkerSet.createMarker(siegeMarkerId, name, siegeLoc.getWorld().getName(), siegeX, 64,
                                    siegeZ, siegeIcon, false);

                            siegeMarker = siegeWarMarkerSet.findMarker(siegeMarkerId);
                            siegeMarker.setLabel(name);
                            siegeMarker.setDescription(desc);
                        } else {
                            siegeMarker.setLabel(name);
                            siegeMarker.setDescription(desc);
                        }
                        townUUDToSiegeMarkerMap.put(siege.getTown().getUUID(), siegeMarker);
                    }
                } catch (Exception ex) {
                    System.err.println(SiegeWar.prefix + "Problem adding siege marker for siege: " + name);
                    ex.printStackTrace();
                }
            }
        }
        */
    }

    /**
     * This method hides players who have the map hiding metadata tag,
     * and shows players who do not have it.
     */
    private static void applyMapHidingToPlayers() {
        if (!SiegeWarSettings.getWarSiegeMapHidingEnabled())
            return;

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player player : onlinePlayers) {
            if (player.hasMetadata(SiegeWarMapUtil.MAP_HIDING_METADATA_ID)) {
                pl3xMapPlugin.playerManager().hide(player.getUniqueId());
            } else {
                pl3xMapPlugin.playerManager().show(player.getUniqueId());
            }
        }
    }
    
        private BufferedImage loadIcon(String type, String urlStr, Logger errorLogger) {
            URL url;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException ex) {
                errorLogger.log(Level.SEVERE, "Cannot load " + type + " icon due to an invalid URL!", ex);
                return null;
            }
        
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                errorLogger.log(Level.SEVERE, "Error while loading " + type + " image icon!", e);
                return null;
            }
        }
}