package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_bludgeonOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(175,150,30,225);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 11f, 0.3f);
            
            engine.addHitParticle(proj_location, ship_velocity, 50f, 1f, 0.1f, FLASH_COLOR.brighter());
            
        	for (int i=0; i < 9; i++) {
    			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                
                engine.addNebulaParticle(proj_location, smokeVel,
                		MathUtils.getRandomNumberInRange(12f, 20f),
                		1.7f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.2f, 1.8f), //totalDuration
                		new Color(55,50,40,105),
                		true);
                
                for (int j=0; j < 4; j++) {

        			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-41f, 41f);
                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 90f), angle2);

                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(proj_location, 3f),
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 6f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.35f, 0.55f), //duration
            				FLASH_COLOR);
                	}
        	}
        	
    }
  }