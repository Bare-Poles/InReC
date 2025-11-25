package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.amazigh.InReC.scripts.InReC_quarkOnHitEffect.INREC_QuarkComboMult;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class InReC_headstoneEffect implements OnFireEffectPlugin, OnHitEffectPlugin {
	
    private static final Color FLASH_COLOR_R = new Color(252,111,89,215);
    private static final Color FLASH_COLOR_O = new Color(253,169,84,207);
    private static final Color FLASH_COLOR_Y = new Color(255,226,79,199);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            float angle = projectile.getFacing();
            
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR_R, 58f, 0.3f);
            engine.addHitParticle(proj_location, ship_velocity, 111f, 1f, 0.1f, FLASH_COLOR_O.brighter());
            
            engine.addSmoothParticle(proj_location,
            		ship_velocity,
    				46f, //size
    				0.8f, //brightness
    				0.2f, //duration
    				FLASH_COLOR_Y);
            
            for (int i=0; i < 10; i++) {
    			float angle1 = angle + MathUtils.getRandomNumberInRange(-4f, 4f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship_velocity, i * 2f, angle1);
                Vector2f smokePoint = MathUtils.getPointOnCircumference(proj_location, i * 3f, angle1);
                
                engine.addNebulaParticle(smokePoint, smokeVel,
                		MathUtils.getRandomNumberInRange(31f, 41f) - i,
                		1.6f, //endsizemult
                		0.13f, //rampUpFraction
                		0.35f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.8f, 2.3f), //totalDuration
                		new Color(50,35 + i,40,130 - (i * 5)),
                		true);
        	}
            
            for (int i=0; i < 2; i++) {
            	Vector2f cloudVel = MathUtils.getPointOnCircumference(ship_velocity, i * 2f, angle);
                Vector2f cloudPos = MathUtils.getPointOnCircumference(proj_location, i * 10f, angle);
            	
                engine.addNebulaParticle(cloudPos, cloudVel,
                		MathUtils.getRandomNumberInRange(69f, 75f) - (i * 9f),
                		1.69f, //endsizemult
                		0.16f, //rampUpFraction
                		0.33f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(2f, 2.5f), //totalDuration
                		new Color(69,50,45,101),
                		true);
            }
            
            // "gradient spark wave" THING
        	INREC_RadialEmitter emitterR = new INREC_RadialEmitter((CombatEntityAPI) ship);
            emitterR.location(proj_location);
            emitterR.angle(angle, 0f);
            emitterR.life(0.65f, 1.0f);
            emitterR.size(3f, 7f);
    		emitterR.velocity(8f, 25f);
    		emitterR.distance(3f, 14f);
    		emitterR.color(239,93,81,202);
    		emitterR.velDistLinkage(false);
    		emitterR.emissionOffset(-24, 48);
    		emitterR.burst(15);
            
    		INREC_RadialEmitter emitterO = new INREC_RadialEmitter((CombatEntityAPI) ship);
    		emitterO.location(proj_location);
            emitterO.angle(angle, 0f);
            emitterO.life(0.7f, 1.1f);
            emitterO.size(3f, 6f);
            emitterO.velocity(23f, 25f);
            emitterO.distance(15f, 14f);
            emitterO.color(246,154,77,187);
    		emitterO.velDistLinkage(false);
    		emitterO.emissionOffset(-21, 42);
    		emitterO.burst(15);
    		
    		INREC_RadialEmitter emitterY = new INREC_RadialEmitter((CombatEntityAPI) ship);
    		emitterY.location(proj_location);
    		emitterY.angle(angle, 0f);
    		emitterY.life(0.75f, 1.2f);
    		emitterY.size(3f, 5f);
    		emitterY.velocity(38f, 25f);
    		emitterY.distance(27f, 14f);
    		emitterY.color(253,214,73,172);
    		emitterY.velDistLinkage(false);
    		emitterY.emissionOffset(-18, 36);
    		emitterY.burst(15);
            
    }
    
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
			Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
			
		}
		engine.spawnExplosion(point, fxVel, FLASH_COLOR_R, 139f, 0.77f);
        engine.addHitParticle(point, fxVel, 234f, 1f, 0.1f, FLASH_COLOR_O.brighter());
        
        engine.addSmoothParticle(point,
        		fxVel,
				137f, //size
				0.8f, //brightness
				0.25f, //duration
				FLASH_COLOR_Y);
		
        INREC_RadialEmitter emitterA = new INREC_RadialEmitter(target);
        emitterA.location(point);
        emitterA.life(1.3f, 1.7f);
        emitterA.size(3f, 7f);
		emitterA.velocity(22f, 21f);
		emitterA.distance(1f, 13f);
		emitterA.color(120,47,41,202); // 239,93,81,202
        emitterA.lifeLinkage(true);
		emitterA.burst(52);
		
		INREC_RadialEmitter emitterB = new INREC_RadialEmitter(target);
        emitterB.location(point);
        emitterB.life(0.8f, 1.4f);
        emitterB.size(3f, 6f);
		emitterB.velocity(40f, 25f);
		emitterB.distance(9f, 13f);
		emitterB.color(123,77,39,177); // 246,154,77,177
        emitterB.lifeLinkage(true);
		emitterB.burst(64);
		
		INREC_RadialEmitter emitterC = new INREC_RadialEmitter(target);
        emitterC.location(point);
        emitterC.life(0.5f, 0.9f);
        emitterC.size(3f, 5f);
		emitterC.velocity(58f, 33f);
		emitterC.distance(17f, 13f);
		emitterC.color(127,107,37,152); // 253,214,73,152
        emitterC.lifeLinkage(true);
		emitterC.burst(76);
		
    	for (int i=0; i < 2; i++) {
    		engine.addSwirlyNebulaParticle(point,
        			fxVel,
            		69f, //size
            		2.3f, //endsizemult
            		0.56f, //rampUpFraction
            		0.77f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.8f, 2.2f), //totalDuration
            		new Color(169,32 + (int) (i * 48),54,173),
            		true);
    	}

		engine.addNebulaParticle(point,
    			fxVel,
        		69f, //size
        		1.9f, //endsizemult
        		0.34f, //rampUpFraction
        		0.77f, //fullBrightnessFraction
        		MathUtils.getRandomNumberInRange(2f, 2.25f), //totalDuration
        		new Color(91,69,49,123),
        		true);
		
    	for (int i=0; i < 16; i++) {
    		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    		
        	engine.addNebulaParticle(MathUtils.getPointOnCircumference(point, 9f, angle),
        			MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(9f, 34f), angle),
            		MathUtils.getRandomNumberInRange(39f, 53f),
            		1.8f, //endsizemult
            		0.54f, //rampUpFraction
            		0.69f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.7f, 2.1f), //totalDuration
            		new Color(76,63,45,87),
            		true);
    	}
    	
    	if (target instanceof ShipAPI) {
//			if (!shieldHit) {
				ShipAPI tagShip = (ShipAPI) target;
				
				if (tagShip.isHulk() || !tagShip.isAlive()) {
					return; // no agitating on dead ships/hulks, because they won't have charges!
				}
				
				if (tagShip.hasListenerOfClass(INREC_QuarkComboMult.class)) {
					List<INREC_QuarkComboMult> listeners = tagShip.getListeners(INREC_QuarkComboMult.class);
					if (listeners.isEmpty()) return; // ??? (idk either alex, but sanity checks are a real one)
					
					INREC_QuarkComboMult listener = listeners.get(0);
					listener.agitate();
				}
//			}
    	}
    	
	}
    
  }