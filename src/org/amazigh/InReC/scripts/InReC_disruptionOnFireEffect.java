package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_disruptionOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(90,200,225,240);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            float angle = projectile.getFacing();
            
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.addHitParticle(proj_location, ship_velocity, 42f, 1f, 0.1f, FLASH_COLOR.brighter());
            
            // the i fuckery means: particles that spawn further out, have a shorter lifetime, causing an "inwards" sparkle fade
            
            for (int i=0; i < 24; i++) {
            	float arcPoint = MathUtils.getRandomNumberInRange(angle - 3f, angle + 3f);
            	
            	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(0f, 8f), MathUtils.getRandomNumberInRange(angle - 85f, angle + 85f));
            	
            	float sparkRange = 30 - i; // 30 - (1-24) =  6-29
            	
            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, sparkRange, arcPoint);
            	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
            	
            	engine.addSmoothParticle(spawnLocation,
            			velocity,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			(i * 0.03f) + MathUtils.getRandomNumberInRange(0.52f, 0.63f), // (0.03-0.72) + (0.52-0.63) = (0.55-1.35)
            			FLASH_COLOR);
            }
            
            for (int i=0; i < 3; i++) {
        		float angle1 = angle + MathUtils.getRandomNumberInRange(-5f, 5f);
        		Vector2f nebVel = MathUtils.getPointOnCircumference(ship_velocity, (i * 4) + MathUtils.getRandomNumberInRange(2f, 10f), angle1);
        		engine.addSwirlyNebulaParticle(proj_location,
        				nebVel,
        				10f,
        				1.5f,
        				0.6f,
        				0.5f,
        				MathUtils.getRandomNumberInRange(0.2f, 0.6f),
        				new Color(95,125,150,75),
        				true);
            }
    		
    }
  }