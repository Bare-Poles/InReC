package org.amazigh.InReC.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
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
        	if (info.TIMER > 0.4f) {
            	info.TIMER -= 0.4f;
            	
            	// float effectLevel = ship.getSystem().getEffectLevel();
            	
            	engine.addSmoothParticle(ship.getLocation(),
            			ship.getVelocity(),
            			ship.getCollisionRadius() + 300f,
            			1f,
            			0.05f,
            			new Color(60,220,210,77));
            	
            	for (int i = 0; i < 56; i++) {
            		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
	        		Vector2f sparkPoint = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(0f, 300f), angle);
	        		Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(6f, 17f), angle);
        			engine.addSmoothParticle(sparkPoint,
	        				sparkVel,
	            			MathUtils.getRandomNumberInRange(3f, 7f),
	            			1f,
	            			MathUtils.getRandomNumberInRange(0.4f, 0.7f),
	            			new Color(65,220,195,175));
	        		
	        	}
            	
            	for (MissileAPI target_missile : AIUtils.getNearbyEnemyMissiles(ship, ship.getCollisionRadius() + 300f)) {
            		
                	engine.applyDamage(target_missile, target_missile.getLocation(), 20f, DamageType.FRAGMENTATION, 0, true, true, ship);
            		
                	engine.addSmoothParticle(target_missile.getLocation(),
                			ship.getVelocity(),
                			30f,
                			1f,
                			0.05f,
                			new Color(60,220,210,37));
                	
                	for (int i = 0; i < 2; i++) {
                		engine.addSwirlyNebulaParticle(target_missile.getLocation(),
                    			ship.getVelocity(),
                				14f,
                				2.3f,
                				0.6f,
                				0.4f,
                				MathUtils.getRandomNumberInRange(0.4f, 0.7f),
                				new Color(55,155,125,75),
                				true);
                	}
                	
                	
                	
            		// CombatUtils.applyForce((CombatEntityAPI) target_missile, VectorUtils.getAngle(ship.getLocation(), target_missile.getLocation()), 10f * effectLevel);
                	// was the initial idea, but lead to some missiles getting YEETED in a very silly manner
            		
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
