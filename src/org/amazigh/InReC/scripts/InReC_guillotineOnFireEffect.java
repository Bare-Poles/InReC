package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_guillotineOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(251,130,30,215);
    private static final Color SPARK_COLOR = new Color(251,130,30,235);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            float angle = projectile.getFacing();
            		
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 69f, 0.2f);
            engine.addHitParticle(proj_location, ship_velocity, 125f, 1f, 0.1f, FLASH_COLOR.brighter());
            
            for (int i=0; i < 3; i++) {
            	Vector2f cloudVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 2f, angle);
                Vector2f cloudPos = MathUtils.getPointOnCircumference(proj_location, i * 10f, angle);
            	
                engine.addNebulaParticle(cloudPos, cloudVel,
                		MathUtils.getRandomNumberInRange(45f, 50f) - (i * 5f),
                		1.7f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.8f, 2.3f), //totalDuration
                		new Color(55,34,10,105),
                		true);
            }
            
            
            for (int i=0; i < 9; i++) {
    			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                
                engine.addNebulaParticle(proj_location, smokeVel,
                		MathUtils.getRandomNumberInRange(32f, 48f),
                		1.6f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.8f, 2.3f), //totalDuration
                		new Color(50,40,35,110),
                		true);
                
                for (int j=0; j < 5; j++) {

        			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-12f, 12f);
                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(5f, 120f), angle2);

                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(proj_location, 3f),
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 8f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.7f, 0.9f), //duration
            				FLASH_COLOR);
                	}
        	}
            
            //TODO
            // more sparks and smoke and idkkk!
            	// sort of "angled" jets of smoke?
            	// additional wide spray of "allmind" sparks
        	
    }
  }