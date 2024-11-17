package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotFlares_b extends BaseHullMod {
	
	private static Map<HullSize, Float> count = new HashMap<HullSize, Float>();
	static {
		count.put(HullSize.FRIGATE, 2f);
		count.put(HullSize.DESTROYER, 2f);
		count.put(HullSize.CRUISER, 3f);
		count.put(HullSize.CAPITAL_SHIP, 4f);
	}
	
	private static Map<HullSize, Float> cooldown = new HashMap<HullSize, Float>();
	static {
		cooldown.put(HullSize.FRIGATE, 15f);
		cooldown.put(HullSize.DESTROYER, 20f);
		cooldown.put(HullSize.CRUISER, 15f);
		cooldown.put(HullSize.CAPITAL_SHIP, 10f);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_FLARE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        info.COUNT = (int) ((float) (count.get(ship.getHullSize()))); // so you have to cast from "Float" to "float" in order to cast to int, that's fucked up.
		info.COOLDOWN = (int) ((float) (cooldown.get(ship.getHullSize())));
		
        
		// once a second, check to see if there is an enemy missile within ~1000 range
        if (info.READY) {
        	info.TIMER += amount;
        	if (info.TIMER > 1f) {
        		info.TIMER -= 1f;
        		
        		if (AIUtils.getNearestEnemyMissile(ship) != null) {
        			if (MathUtils.isWithinRange(ship, AIUtils.getNearestEnemyMissile(ship), 1000f)) {
                    	info.READY = false;
                    	info.FIRING = true;
                    	info.TIMER = 0.15f;
                	}
        		}
        		
        	}
        }
        
        // if there is a missile within range, fire flares!
        if (info.FIRING) {
        	info.TIMER += amount;
        	if (info.TIMER > 0.15f) {
            	info.TIMER -= 0.15f;
            	
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
	                	Global.getSoundPlayer().playSound("system_flare_launcher_active", 1f, 0.7f, ship.getLocation(), ship.getVelocity());
	        		}
            	}
            	info.FIRED++;
            	
            	if (info.FIRED >= info.COUNT) {
            		info.FIRING = false;
            		info.FIRED = 0;
            		info.TIMER = 0f;
            	}
            }
        }
        
        // if not ready, start the cooldown timer!
        if (!info.READY) {
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
	        private float COOLDOWN = 15f;
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
		
		LabelAPI label = tooltip.addPara("An automated flare launching system, will automatically fire seeker flares on detecting nearby hostile missiles.", opad);
		
		if (slotCount > 1) {
			label = tooltip.addPara("This vessel has %s automated flare launchers.", opad, h, "two");
			label.setHighlight("two");
			label.setHighlightColors(h);

			label = tooltip.addPara("On detecting a hostile missile within %s range, the flare launcher will fire %s seeker flares.", pad, h, "1000", "" + (int) ((float) (count.get(ship.getHullSize()))));
			label.setHighlight("1000", "" + (int) ((float) (count.get(ship.getHullSize()))));
			label.setHighlightColors(h, h);

			label = tooltip.addPara("The flare launcher takes %s to reload after use.", pad, h, (int) ((float) (cooldown.get(ship.getHullSize()))) + " seconds");
			label.setHighlight((int) ((float) (cooldown.get(ship.getHullSize()))) + " seconds");
			label.setHighlightColors(h);
			
		} else {
			label = tooltip.addPara("This vessel has %s automated flare launcher.", opad, h, "one");
			label.setHighlight("one");
			label.setHighlightColors(h);
			
			label = tooltip.addPara("On detecting a hostile missile within %s range, each flare launcher will fire %s seeker flares.", pad, h, "1000", "" + (int) ((float) (count.get(ship.getHullSize()))));
			label.setHighlight("1000", "" + (int) ((float) (count.get(ship.getHullSize()))));
			label.setHighlightColors(h, h);

			label = tooltip.addPara("The flare launchers take %s to reload after use.", pad, h, (int) ((float) (cooldown.get(ship.getHullSize()))) + " seconds");
			label.setHighlight((int) ((float) (cooldown.get(ship.getHullSize()))) + " seconds");
			label.setHighlightColors(h);
		}
		
	}
	
    @Override
    public Color getBorderColor() {
        return new Color(30,110,105,180);
    }

    @Override
    public Color getNameColor() {
        return new Color(60,220,210,240);
    }
}
