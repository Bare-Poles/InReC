package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotFlares extends BaseHullMod {
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_FLARE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        MutableShipStatsAPI stats = ship.getMutableStats();
        boolean sMod = isSMod(stats);
		if (sMod) {
			info.COUNT = 3;
			info.COOLDOWN = 20f;
		}
        
		
		// once a second, check to see if there is an enemy missile within ~1000 range
        if (info.READY) {
        	info.TIMER += amount;
        	if (info.TIMER > 1f) {
        		info.TIMER -= 1f;
        		
        		if (AIUtils.getNearestEnemyMissile(ship) != null) {
        			if (MathUtils.isWithinRange(ship, AIUtils.getNearestEnemyMissile(ship), 1000f)) {
                    	info.READY = false;
                    	info.FIRING = true;
                    	info.TIMER = 0.1f;
                	}
        		}
        		
        	}
        }
        
        // if there is a missile within range, fire flares!
        if (info.FIRING) {
        	info.TIMER += amount;
        	if (info.TIMER > 0.1f) {
            	info.TIMER -= 0.1f;
            	
            	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	        		if (weapon.isDecorative()) {

	        			Vector2f flarePoint = weapon.computePosition(ship);
	        			Vector2f flareVel = ship.getVelocity();
	        			Float flareAngle = weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-13f, 13f);
	        			
	        	        for (int i = 0; i < 2; i++) {
	        	        	Vector2f puffRandomVel = MathUtils.getPointOnCircumference(flareVel, MathUtils.getRandomNumberInRange(4f, 12f), flareAngle + MathUtils.getRandomNumberInRange(-7f, 7f));
		        			
		    				engine.addSmokeParticle(flarePoint,
		    						puffRandomVel,
		    						MathUtils.getRandomNumberInRange(7f, 15f),
		    						0.8f,
		    						0.9f,
		    						new Color(100,225,255,56));
	        	        }
	        			
	                	engine.spawnProjectile(ship, null, "InReC_flare_smart", flarePoint, flareAngle, flareVel);
	                	info.FIRED++;
	                	
	        			Global.getSoundPlayer().playSound("system_flare_launcher_active", 1f, 0.7f, ship.getLocation(), ship.getVelocity());
	        		}
            	}
            	
            	if (info.FIRED >= info.COUNT) {
            		info.FIRING = false;
            		info.FIRED = 0;
            		info.TIMER = 0f;
            	}
            }
        }
        
        // if not ready, and not firing, start the cooldown timer!
        if (!info.READY && !info.FIRING) {
        	info.TIMER += amount;
        	if (info.TIMER >= info.COOLDOWN) {
        		info.READY = true;
        	}
        }
        
        if (ship == Global.getCombatEngine().getPlayerShip()) {
        	String flareInfo = "";
        	if (info.READY) {
        		flareInfo = "Flares Ready";
        	} else if (info.FIRING) {
        		flareInfo = "Flares Firing";
        	} else {
        		float flarePercent = 100f * (info.TIMER / info.COOLDOWN);
        		flareInfo = "Flares Reloading: " + (int) flarePercent + "%";
        	}
        	// 
        	engine.maintainStatusForPlayerShip("INRECFLAREINFO", "graphics/icons/hullsys/active_flare_launcher.png",  "Automated Protection Unit", flareInfo, false);
		}
        
        engine.getCustomData().put("InReC_FLARE_DATA_KEY" + ship.getId(), info);
	}
	
	 private class ShipSpecificData {
	        private boolean READY = true;
	        private boolean FIRING = false;
	        private float TIMER = 0f;
	        private float COOLDOWN = 25f;
	        private int COUNT = 2;
	        private int FIRED = 0;
	    }
	 
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
    
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		int slotCount = 0;
		for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    		if (weapon.isDecorative()) {
    			slotCount ++;
    		}
		}
		
		LabelAPI label = tooltip.addPara("Installs a set of automated flare launchers, that automatically fire seeker flares on detecting hostile missiles.", opad);
		
		label = tooltip.addPara("Each slot on the vessel will mount an automated flare launcher, this ship has %s slots.", opad, h, "" + slotCount);
		label.setHighlight("" + slotCount);
		label.setHighlightColors(h);
		label = tooltip.addPara("On detecting a hostile missile within %s range, each flare launcher will fire %s seeker flares.", pad, h, "1000", "2");
		label.setHighlight("1000", "2");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("The flare launchers take %s to reload after use.", pad, h, "25 seconds");
		label.setHighlight("25 seconds");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("May only be installed on InReC vessels, and only one slot hullmod may be installed at a time.", opad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "3";
		if (index == 1) return "20 seconds";
		return null;
	}
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") && !ship.getVariant().getHullMods().contains("InReC_slotVent") && !ship.getVariant().getHullMods().contains("InReC_slotShield") && !ship.getVariant().getHullMods().contains("InReC_slotRange");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("InReC_slotVent") || ship.getVariant().getHullMods().contains("InReC_slotShield") || ship.getVariant().getHullMods().contains("InReC_slotRange")) {
			return "May only install one InReC Slot hullmod at a time.";
		}
		if (!ship.getVariant().getHullMods().contains("InReC_slot")) {
			return "Only compatible with InReC vessels.";
		}
		return null;
	}
	
	
    @Override
    public Color getBorderColor() {
        return new Color(60,220,210,180);
    }

    @Override
    public Color getNameColor() {
        return new Color(60,220,210,240);
    }
}
