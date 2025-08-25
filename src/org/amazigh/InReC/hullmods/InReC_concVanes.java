package org.amazigh.InReC.hullmods;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.ShipAPI;

public class InReC_concVanes extends BaseHullMod {

	public static final float PUSH_VALUE = 111f; // the force that is applied by the "concussion pulses"
	
	public static final float VENT_BONUS = 100f; // BIG vent!
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_VANES_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
    	info.TIMER += amount; 
    	
    	float pulseDam = 20f;
    	
    	if (! ship.getHullSize().equals(HullSize.FIGHTER)) {
    		// fighter hullsize check because the fighter doesn't have a system!
        	if (ship.getFluxTracker().isVenting() || ship.getSystem().isActive()) {
        		pulseDam = 25f; // stronger pulse damage when venting or system is active!
        		info.TIMER += amount * 0.2f; // also slightly faster pulses!
        	}
    	}
    	
    	if (ship.getFluxTracker().isOverloaded()) {
    		info.TIMER = 0f; // stop it from charging/firing when overloaded!
    	}
    	
    	// jitter
    	int alpha = 20 + Math.min(80, (int)(info.TIMER * 160f));
    	int copies = 20;
    	float jRange = 6f; 
    			
    	if (ship.getHullSize().equals(HullSize.FIGHTER)) {
    		alpha = (int)(alpha * 0.75f);
    		copies = 10;
    		jRange = 8f;
    	}
    	
    	Color effectColor = new Color(65,220,195,alpha);
    	// setting up the vfx color
    	ship.setJitterUnder(this, effectColor, 1f, copies, 0f, jRange + (info.TIMER * 6f));
    	

    	if (! ship.getHullSize().equals(HullSize.FIGHTER)) {
    		// fighter hullsize check because the fighter doesn't have a system!
        	if (ship.getSystem().isActive()) {
                // jitter
                int alpha2 = 18 + (int)(ship.getSystem().getEffectLevel() * 37f);
            	Color effectColor2 = new Color(65,220,195,alpha2);
            	// setting up the vfx color
            	ship.setJitter(this, effectColor2, 1f, 3, 0f, 5f);
        	}
    	}
    	
    	if (info.TIMER > 0.5f) {
    		
        	int count = 69;
        	float range = 320f;
        	if (ship.getHullSize().equals(HullSize.FIGHTER)) {
        		count = 49; // setup so the "fighter version" has slightly shorter range / particle count - we're sharing the hullmod across fighter/ship for 0.98 codex info compatibility!
        		range = 280f;
        	}
        	
    		if (AIUtils.getNearestEnemyMissile(ship) != null) {
    			if (MathUtils.isWithinRange(ship, AIUtils.getNearestEnemyMissile(ship), range)) {
    				info.TARGET = true;
            	}
    		}

        	if (! ship.getHullSize().equals(HullSize.FIGHTER)) {
        		// fighter hullsize check because the fighter doesn't have a system!
        		if (ship.getFluxTracker().isVenting() || ship.getSystem().isActive()) {
        			info.TARGET = true; // forced pulses when venting or system is active!
        		}
        	}
        	info.TIMER -= 0.1f; // forcing a 0.1s rate limiter, for performance reasons
        	
    		if (info.TARGET) {
            	info.TIMER -= 0.4f;
            	info.TARGET = false;
            	
            	engine.addSmoothParticle(ship.getLocation(),
            			ship.getVelocity(),
            			(ship.getCollisionRadius() * 2f) + (range * 1.5625f),
            			1f,
            			0.15f,
            			new Color(60,220,210,59));
            	
            	INREC_RadialEmitter emitter = new INREC_RadialEmitter(ship);
                emitter.location(ship.getLocation());
                emitter.life(0.4f, 0.7f);
                emitter.size(4f, 8f);
        		emitter.velocity(14f, 14f);
        		emitter.distance(ship.getCollisionRadius(), range + 40f);
        		emitter.color(65,220,195,175);
        		emitter.burst(count);
            	
            	for (MissileAPI target_missile : AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + range + 40f)) {
            		
            		float shuntAngle = VectorUtils.getAngle(ship.getLocation(), target_missile.getLocation());
            		Vector2f velShunt = MathUtils.getPointOnCircumference(target_missile.getVelocity(), PUSH_VALUE, shuntAngle);
            		target_missile.getVelocity().set(velShunt);
            		
            		engine.applyDamage(target_missile, target_missile.getLocation(), pulseDam, DamageType.ENERGY, 0, true, true, ship);
            		// these are more powerful than the damper pulses!
            		
            		engine.addNebulaParticle(target_missile.getLocation(),
            				ship.getVelocity(),
                    		MathUtils.getRandomNumberInRange(20f, 23f),
                    		1.95f, //endsizemult
                    		0.5f, //rampUpFraction
                    		0.45f, //fullBrightnessFraction
                    		0.3f, //totalDuration
                    		new Color(45,75,60,101),
                    		true);
            		
            	}
    		}
        }
    	
    	
    	
        engine.getCustomData().put("InReC_VANES_DATA_KEY" + ship.getId(), info);
        
	}
	
	private class ShipSpecificData {
		private float TIMER = 0f;
		private boolean TARGET = false;
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
		 
		 LabelAPI label = tooltip.addPara("Specialised vanes are installed on this vessel, that allow for automatic projection of defensive concussive pulses.", opad);
		 
		 label = tooltip.addPara("Upon detecting a nearby hostile missile a pulse is fired that deals %s damage and applies a force to any nearby hostile missiles.", opad, h, "20 Energy");
		 label.setHighlight("20 Energy");
		 label.setHighlightColors(h);
		 label = tooltip.addPara("Pulses can be fired at most once every %s,", pad, h, "Half a Second");
		 label.setHighlight("Half a Second");
		 label.setHighlightColors(h);
		 
		 label = tooltip.addPara("The vanes are tied into the vessels active venting systems, active vent rate is increased by %s and active venting causes pulses to automatically fire.", opad, h, (int)VENT_BONUS + "%");
		 label.setHighlight((int)VENT_BONUS + "%");
		 label.setHighlightColors(h);
		 
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
