package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_fusorOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(145,100,200,215);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            float angle = projectile.getFacing();
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.addHitParticle(proj_location, ship_velocity, 39f, 0.6f, 0.1f, FLASH_COLOR.darker());
            
            for (int i=0; i < 20; i++) {
            	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, i * 4f + MathUtils.getRandomNumberInRange(0f, 4f), MathUtils.getRandomNumberInRange(angle - 3f, angle + 3f));
            	
            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, (i+1) * 0.5f, angle);
            	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 4f));
            	
            	engine.addSmoothParticle(spawnLocation,
            			velocity,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			MathUtils.getRandomNumberInRange(0.45f, 0.6f),
            			FLASH_COLOR);
            }
            
    }
  }