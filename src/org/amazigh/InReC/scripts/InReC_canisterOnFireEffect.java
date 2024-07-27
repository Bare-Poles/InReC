package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_canisterOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            Vector2f proj_location = projectile.getLocation();

            float angle1 = angle + MathUtils.getRandomNumberInRange(-11f, 11f);
            Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 31f), angle1);
            Vector2f point1 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(-2f, 23f), angle1);
            
            engine.addNebulaSmokeParticle(point1,
            		smokeVel,
            		10f, //size
            		MathUtils.getRandomNumberInRange(1.3f, 1.75f), //end mult
            		0.6f, //ramp fraction
            		0.5f, //full bright fraction
            		MathUtils.getRandomNumberInRange(1.2f, 1.7f), //duration
            		new Color(90,110,120,75));

            for (int i=0; i < 2; i++) {

    			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-23f, 23f);
                Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 110f), angle2);
                Vector2f point2 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(2f, 10f), angle2);
                
                engine.addSmoothParticle(point2,
                		sparkVel,
        				MathUtils.getRandomNumberInRange(3f, 5f), //size
        				1f, //brightness
        				MathUtils.getRandomNumberInRange(0.45f, 0.65f), //duration
        				new Color(95,120,175,192));
            }
    		
                        
    		// random projectile velocity thing (scales velocity from -18% to +9%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.18f, velScale * 0.09f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    }
  }