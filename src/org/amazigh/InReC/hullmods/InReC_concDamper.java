package org.amazigh.InReC.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;

public class InReC_concDamper extends BaseHullMod {
	
	private static Map<HullSize, Float> mag = new HashMap<HullSize, Float>();
	static {
		mag.put(HullSize.FRIGATE, 56f);
		mag.put(HullSize.DESTROYER, 69f); // ?
		mag.put(HullSize.CRUISER, 82f);
		mag.put(HullSize.CAPITAL_SHIP, 95f); // ?
	}
	
	public static final float PUSH_VALUE = 100f; // the force that is applied by the "concussion pulses"
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_DAMPER_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        // if system is active, run the effect!
        if (ship.getSystem().isActive()) {
        	info.TIMER += amount;
        	if (info.TIMER > 0.3f) {
            	info.TIMER -= 0.3f;
            	
            	float effectLevel = ship.getSystem().getEffectLevel();
            	
            	engine.addSmoothParticle(ship.getLocation(),
            			ship.getVelocity(),
            			(ship.getCollisionRadius() * 2f) + 500f,
            			1f,
            			0.15f,
            			new Color(60,220,210,59));
            	
            	INREC_RadialEmitter emitter = new INREC_RadialEmitter(ship);
                emitter.location(ship.getLocation());
                emitter.life(0.4f, 0.7f);
                emitter.size(4f, 8f);
        		emitter.velocity(14f, 14f);
        		emitter.distance(ship.getCollisionRadius(), 300f);
        		emitter.color(65,220,195,175);
        		emitter.burst((int) ((float) mag.get(ship.getHullSize()))); // "Float" is a bitch ass, and should be killed with hammers (in minecraft)
        		
            	for (MissileAPI target_missile : AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + 300f)) {
            		
            		float shuntAngle = VectorUtils.getAngle(ship.getLocation(), target_missile.getLocation());
            		Vector2f velShunt = MathUtils.getPointOnCircumference(target_missile.getVelocity(), PUSH_VALUE * effectLevel, shuntAngle);
            		target_missile.getVelocity().set(velShunt);
            		
            		engine.applyDamage(target_missile, target_missile.getLocation(), 15f, DamageType.FRAGMENTATION, 0, true, true, ship);
            		// added damage to make it more "useful" compared to a regular damper
            		
            		engine.addNebulaParticle(target_missile.getLocation(),
            				ship.getVelocity(),
                    		MathUtils.getRandomNumberInRange(18f, 21f),
                    		1.9f, //endsizemult
                    		0.5f, //rampUpFraction
                    		0.4f, //fullBrightnessFraction
                    		0.3f, //totalDuration
                    		new Color(45,75,60,101),
                    		true);
            		
            		// CombatUtils.applyForce((CombatEntityAPI) target_missile, VectorUtils.getAngle(ship.getLocation(), target_missile.getLocation()), 10f * effectLevel);
                	// was the initial idea, but lead to some missiles getting YEETED in a very silly manner, so i swapped to a "manual" push
            		
            	}
            	
            }
        }
        
        
        engine.getCustomData().put("InReC_DAMPER_DATA_KEY" + ship.getId(), info);
	}
	
	 private class ShipSpecificData {
	        private float TIMER = 0f;
	    }
    
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
}
