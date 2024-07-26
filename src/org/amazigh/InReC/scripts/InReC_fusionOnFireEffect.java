package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_fusionOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
    		// random projectile velocity thing (scales velocity from -15% to +5%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.15f, velScale * 0.05f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    		float angle1 = angle + MathUtils.getRandomNumberInRange(-10f, 10f);
    		Vector2f nebVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(2f, 20f), angle1);
    		engine.addSwirlyNebulaParticle(projectile.getLocation(),
    				nebVel,
    				10f,
    				1.5f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(1.2f, 1.7f),
    				new Color(150,105,95,75),
    				true);
    		
    		for (int i=0; i < 3; i++) {
            	Vector2f velocity = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(18f, 50f), MathUtils.getRandomNumberInRange(angle - 15f, angle + 15f));
            	
            	engine.addSmoothParticle(projectile.getLocation(),
            			velocity,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			MathUtils.getRandomNumberInRange(0.9f, 1.6f),
            			new Color(212,34,34,171));
            }
    		
    }
  }