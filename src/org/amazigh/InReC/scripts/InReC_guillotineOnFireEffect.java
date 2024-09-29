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
    private static final Color SPARK_COLOR = new Color(60,220,210,235);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            float angle = projectile.getFacing();
            engine.addPlugin(new InReC_guillotineTrailScript(projectile));
            
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 69f, 0.23f);
            engine.addHitParticle(proj_location, ship_velocity, 125f, 1f, 0.1f, FLASH_COLOR.brighter());
            
            // "background" nebs
            for (int i=0; i < 3; i++) {
            	Vector2f cloudVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 2f, angle);
                Vector2f cloudPos = MathUtils.getPointOnCircumference(proj_location, i * 10f, angle);
            	
                engine.addNebulaParticle(cloudPos, cloudVel,
                		MathUtils.getRandomNumberInRange(73f, 85f) - (i * 8f),
                		1.69f, //endsizemult
                		0.15f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(2.5f, 3.1f), //totalDuration
                		new Color(50,55,45,101),
                		true);
            }
            
            // "core" muzzle nebs/sparks
            for (int i=0; i < 15; i++) {
    			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                Vector2f smokeLoc = MathUtils.getPointOnCircumference(proj_location, i * 5f, angle1);
                
                engine.addNebulaParticle(smokeLoc,
                		smokeVel,
                		MathUtils.getRandomNumberInRange(33f, 49f) - i,
                		1.6f, //endsizemult
                		0.2f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(2.3f, 2.8f), //totalDuration
                		new Color(50,55,45,111 - i),
                		true);
                
                for (int j=0; j < 4; j++) {

        			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-12f, 12f);
                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(5f, 69f), angle2);
                    Vector2f sparkLoc = MathUtils.getPointOnCircumference(proj_location, i * 3f, angle2);
                    
                    engine.addSmoothParticle(sparkLoc,
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 8f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(1.1f, 1.4f), //duration
            				FLASH_COLOR);
                	}
        	}
            
            // "special" sparks
            for (int i=0; i < 47; i++) {
            	float sparkAngle1 = angle + MathUtils.getRandomNumberInRange(40f, 55f);
            	float sparkAngle2 = angle - MathUtils.getRandomNumberInRange(40f, 55f);
            	
            	Vector2f velocity1 = MathUtils.getPointOnCircumference(ship_velocity, (55f - i) + MathUtils.getRandomNumberInRange(0f, 16f), sparkAngle1);
            	Vector2f velocity2 = MathUtils.getPointOnCircumference(ship_velocity, (55f - i) + MathUtils.getRandomNumberInRange(0f, 16f), sparkAngle2);
            	
            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, i * 2f, angle);
            	Vector2f spawnLocation1 = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
            	Vector2f spawnLocation2 = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
            	
            	engine.addSmoothParticle(spawnLocation1,
            			velocity1,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f - (i * 0.01f),
            			MathUtils.getRandomNumberInRange(1.1f, 1.6f) + (i * 0.01f),
            			SPARK_COLOR);
            	engine.addSmoothParticle(spawnLocation2,
            			velocity2,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f - (i * 0.01f),
            			MathUtils.getRandomNumberInRange(1.1f, 1.6f) + (i * 0.01f),
            			SPARK_COLOR);
            }
            
    }
  }
