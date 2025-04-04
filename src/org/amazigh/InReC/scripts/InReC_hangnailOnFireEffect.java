package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_hangnailOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(207,171,60,225);
    private static final Color SPARK_COLOR = new Color(229,108,66,222);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            float proj_facing = projectile.getFacing();
            
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 13f, 0.3f);
            engine.addHitParticle(proj_location, ship_velocity, 52f, 1f, 0.1f, FLASH_COLOR.brighter());
            
        	for (int i=0; i < 12; i++) {
    			float angle1 = proj_facing + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                
                Vector2f smokeoint = MathUtils.getPointOnCircumference(proj_location, i * 3f, angle1);
                
                int alpha = 140 - (i * 4);
                
                engine.addNebulaParticle(smokeoint, smokeVel,
                		MathUtils.getRandomNumberInRange(20f, 22f) - i,
                		1.69f, //endsizemult
                		0.17f, //rampUpFraction
                		0.4f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.34f, 1.69f), //totalDuration
                		new Color(56,46,41,alpha),
                		true);
                
                for (int j=0; j < 3; j++) {
                	
        			float angle2 = proj_facing + MathUtils.getRandomNumberInRange(-21f, 21f);
        			Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 90f), angle2);
        			
        			Vector2f sparkPoint = MathUtils.getPointOnCircumference(proj_location, i*j, proj_facing);
        			
                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(sparkPoint, 2f),
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 6f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.35f, 0.55f), //duration
            				SPARK_COLOR);
                	}
        	}
        	
    }
  }