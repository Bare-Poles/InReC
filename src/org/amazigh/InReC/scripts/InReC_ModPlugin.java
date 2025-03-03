package org.amazigh.InReC.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.amazigh.InReC.scripts.ai.InReC_defoliantMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_fungicideMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_beekeeperMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_apiaristMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_herbicideMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_pesticideMissileAI;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;

public class InReC_ModPlugin extends BaseModPlugin {
	
	public static final String DEFOLIANT_MISSILE_ID = "InReC_defoliant";
	public static final String BEEKEEPER_MISSILE_ID = "InReC_beekeeper";
	public static final String APIARIST_MISSILE_ID = "InReC_apiarist";
	public static final String HERBICIDE_MISSILE_ID = "InReC_herbicide";
	public static final String FUNGICIDE_MISSILE_ID = "InReC_fungicide_mssl";
	public static final String PESTICIDE_MISSILE_ID = "InReC_pesticide";


	public boolean HAS_GRAPHICSLIB = false;
    
    //New game stuff
    @Override
    public void onNewGameAfterProcGen() {
    }
    
    public void onApplicationLoad() throws Exception {

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            LightData.readLightDataCSV((String)"data/config/InReC_lights_data.csv");
        }
    	
	}

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case DEFOLIANT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_defoliantMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case BEEKEEPER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_beekeeperMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case APIARIST_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_apiaristMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case HERBICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_herbicideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FUNGICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_fungicideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case PESTICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_pesticideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
                return null;
        }
    }
}