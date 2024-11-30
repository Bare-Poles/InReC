package org.amazigh.InReC.hullmods;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicUI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;

public class InReC_concVanes extends BaseHullMod {

	public static final float PUSH_VALUE = 111f; // the force that is applied by the "concussion pulses"
	
	public static final float VENT_BONUS = 100f; // BIG vent!
	
	public static final float RESONANCE_MULT = 0.3f; // damage!
	
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
        
        if (info.doOnce) {
        	Global.getCombatEngine().addPlugin(new chargeBarManager(ship));
        	
        	info.doOnce = false;
        }

    	info.TIMER += amount;
    	info.CHARGE = Math.max(0f, info.CHARGE -(amount * 2f)); 
    	
    	if (ship.getFluxTracker().isVenting()) {
    		info.TIMER += amount * 0.3f; // faster pulses when venting!
    	}
    	
    	if (info.TIMER > 0.5f) {
    		
    		if (AIUtils.getNearestEnemyMissile(ship) != null) {
    			if (MathUtils.isWithinRange(ship, AIUtils.getNearestEnemyMissile(ship), 320f)) {
    				info.TARGET = true;
            	}
    		}
    		if (ship.getFluxTracker().isVenting()) {
    			info.TARGET = true;
    		}
        	info.TIMER -= 0.1f; // forcing a 0.1s rate limiter, for performance reasons
        	
    		
    		if (info.TARGET) {
            	info.TIMER -= 0.4f;
            	info.TARGET = false;
            	info.CHARGE = Math.min(100f, info.CHARGE + 4f);
            	
            	engine.addSmoothParticle(ship.getLocation(),
            			ship.getVelocity(),
            			(ship.getCollisionRadius() * 2f) + 500f,
            			1f,
            			0.15f,
            			new Color(60,220,210,59));
            	
            	for (int i = 0; i < 69; i++) {
            		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
	        		Vector2f sparkPoint = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(0f, 360f), angle);
	        		Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(14f, 28f), angle);
        			engine.addSmoothParticle(sparkPoint,
	        				sparkVel,
	            			MathUtils.getRandomNumberInRange(4f, 8f),
	            			1f,
	            			MathUtils.getRandomNumberInRange(0.4f, 0.7f),
	            			new Color(65,220,195,175));
	        	}
            	
            	for (MissileAPI target_missile : AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + 360f)) {
            		
            		float shuntAngle = VectorUtils.getAngle(ship.getLocation(), target_missile.getLocation());
            		Vector2f velShunt = MathUtils.getPointOnCircumference(target_missile.getVelocity(), PUSH_VALUE, shuntAngle);
            		target_missile.getVelocity().set(velShunt);
            		
            		engine.applyDamage(target_missile, target_missile.getLocation(), 15f, DamageType.FRAGMENTATION, 0, true, true, ship);
            		// these are more powerful than the damper pulses!
            		
            		engine.addNebulaParticle(target_missile.getLocation(),
            				ship.getVelocity(),
                    		MathUtils.getRandomNumberInRange(20f, 23f),
                    		1.95f, //endsizemult
                    		0.5f, //rampUpFraction
                    		0.45f, //fullBrightnessFraction
                    		0.3f, //totalDuration
                    		new Color(30,50,40,101),
                    		true);
            		
            	}
    		}
        }
    	
    	if (!ship.getSystem().isActive()) {
    		// only while the system isn't active, as for some reason it doesn't turn with the ship properly when teleporting!
    		
        	int alpha = 20 + Math.min(80, (int)(info.TIMER * 160f));
        	Color effectColor = new Color(65,220,195,alpha);
        	// setting up the vfx color
        	
            ship.setJitterUnder(this, effectColor, 1f, 20, 0f, 6f + (info.TIMER * 6f));
    	}
    	
    	MutableShipStatsAPI stats = ship.getMutableStats();
    	stats.getEnergyWeaponDamageMult().modifyPercent(spec.getId(), RESONANCE_MULT * info.CHARGE);
        stats.getEnergyRoFMult().modifyPercent(spec.getId(), RESONANCE_MULT * info.CHARGE);
        
        if (ship == engine.getPlayerShip()) {
 			engine.maintainStatusForPlayerShip("INREC_VANE_DATA", "graphics/icons/hullsys/high_energy_focus.png", "Concussion Resonance Vanes", "Energy Weapon Damage+RoF Increased by: " + (int)(RESONANCE_MULT * info.CHARGE) + "%", false);
 		}
    	
        engine.getCustomData().put("InReC_VANES_DATA_KEY" + ship.getId(), info);
        
        
        // little thing to make the AI more aggressive with venting
        if (Global.getCombatEngine().isPaused() || ship.getShipAI() == null) {
        	return;
        }
        
        if (!ship.getFluxTracker().isOverloadedOrVenting()) {
        	if (ship.getFluxTracker().getFluxLevel() > 0.5f && info.CHARGE < 10f) {
        		// if flux is over 50%, and resonance is below 10: VENT (for resonance)
        		ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
        		// forcing an early vent to build up some charge when it's very low
        	}
        }
        
	}
	
	private class ShipSpecificData {
		private float TIMER = 0f;
		private boolean TARGET = false;
		private float CHARGE = 0f;
		private boolean doOnce = true;
	}
	
	//bar rendering everyframe
    private static class chargeBarManager extends BaseEveryFrameCombatPlugin {

        ShipAPI ship;

        private chargeBarManager(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {

            CombatEngineAPI engine = Global.getCombatEngine();
            
            if (!ship.isAlive()) {
                engine.removePlugin(this);
                return;
            }
            
            if (ship == engine.getPlayerShip()) {
            	
            	ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("InReC_VANES_DATA_KEY" + ship.getId());
            	
            	MagicUI.drawHUDStatusBar(ship,
            			(info.CHARGE * 0.01f),
            			Global.getSettings().getColor("textFriendColor").darker(),
            			null,
            			0f,
            			"RESONANCE: ",
            			"",
            			false);
            }
        }
    }
    //bar rendering everyframe
	
    
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
		 
		 int energy_mult = (int) (100f * RESONANCE_MULT); 
		 
		 LabelAPI label = tooltip.addPara("Specialised vanes are installed on this vessel, that allow for automatic projection of defensive concussive pulses.", opad);
		 
		 label = tooltip.addPara("Upon detecting a nearby hostile missile a pulse is fired that deals %s damage and applies a force to any nearby hostile missiles.", opad, h, "15 Fragmentation");
		 label.setHighlight("15 Fragmentation");
		 label.setHighlightColors(h);
		 label = tooltip.addPara("Pulses can be fired at most once every %s,", pad, h, "Half a Second");
		 label.setHighlight("Half a Second");
		 label.setHighlightColors(h);
		 
		 label = tooltip.addPara("Each time a pulse is fired the vanes build up %s Resonance.", opad, h, "4%");
		 label.setHighlight("5%");
		 label.setHighlightColors(h);
		 label = tooltip.addPara("Energy weapon damage and rate of fire is increased by a value equivalent to %s of current Resonance.", pad, h, energy_mult + "%");
		 label.setHighlight(energy_mult + "%");
		 label.setHighlightColors(h);
		 label = tooltip.addPara("Resonance passively decays at a rate of %s per second.", pad, h, "2%");
		 label.setHighlight("5%");
		 label.setHighlightColors(h);
		 
		 label = tooltip.addPara("The vanes are tied into the vessels active venting systems, causing pulses to automatically fire and increasing active vent rate by %s.", opad, h, (int)VENT_BONUS + "%");
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
