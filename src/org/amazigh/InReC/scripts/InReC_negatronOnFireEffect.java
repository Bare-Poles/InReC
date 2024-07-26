package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_negatronOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(60,220,210,210);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            float angle = projectile.getFacing();
            
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.addHitParticle(proj_location, ship_velocity, 45f, 1f, 0.1f, FLASH_COLOR.darker());
            
            for (int i=0; i < 24; i++) {
            	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, i * 3f + MathUtils.getRandomNumberInRange(0f, 3f), MathUtils.getRandomNumberInRange(angle - 3f, angle + 3f));
            	
            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, (i+1) * 0.4f, angle);
            	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 3f));
            	
            	engine.addSmoothParticle(spawnLocation,
            			velocity,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			0.9f,
            			FLASH_COLOR);
            }
            
            for (int i=0; i < 2; i++) {
            	Vector2f neb_velocity = MathUtils.getPointOnCircumference(ship_velocity, 10f, angle);
        		engine.addSwirlyNebulaParticle(proj_location,
        				neb_velocity,
        				15f * i,
        				1.5f,
        				0.3f,
        				0.4f,
        				MathUtils.getRandomNumberInRange(0.3f, 0.4f),
        				new Color(95,150,125,75),
        				true);
            }
    		
    }
  }