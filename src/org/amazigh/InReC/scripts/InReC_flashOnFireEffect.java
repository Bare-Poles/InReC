package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.ai.InReC_flashProjScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_flashOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
            // attach the homing script
        	engine.addPlugin(new InReC_flashProjScript(projectile));
            
    		// random projectile velocity thing (scales velocity from -18% to +9%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.18f, velScale * 0.09f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    		float angle1 = angle + MathUtils.getRandomNumberInRange(-10f, 10f);
    		Vector2f nebVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(2f, 20f), angle1);
    		engine.addSwirlyNebulaParticle(projectile.getLocation(),
    				nebVel,
    				12f,
    				1.6f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.35f, 0.7f),
    				new Color(155,65,170,95),
    				true);
    		
    }
  }