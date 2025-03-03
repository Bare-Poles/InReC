package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_concOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(65,220,195,175);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	float angle = projectile.getFacing();
    	Vector2f ship_velocity = weapon.getShip().getVelocity();
    	Vector2f proj_location = projectile.getLocation();
    	engine.addHitParticle(proj_location, ship_velocity, 40f, 1f, 0.1f, FLASH_COLOR.darker());
    	
    	for (int i=0; i < 27; i++) {
    		Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, i + MathUtils.getRandomNumberInRange(8f, 20f), MathUtils.getRandomNumberInRange(angle - 4f, angle + 4f));
    		
    		Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, (i+1) * 1.5f, angle);
    		spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, 6f + ((i+1) * 0.16f));
    		
    		engine.addSmoothParticle(spawnLocation,
    				velocity,
    				MathUtils.getRandomNumberInRange(3f, 6f),
    				1f,
    				MathUtils.getRandomNumberInRange(0.4f, 0.7f),
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
    				new Color(100,150,115,69),
    				true);
    	}
    	
    }
  }