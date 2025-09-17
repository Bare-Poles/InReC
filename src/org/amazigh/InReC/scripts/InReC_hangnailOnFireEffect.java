package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_hangnailOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(207,171,60,225);
//    private static final Color SPARK_COLOR = new Color(229,108,66,222);
    
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
                
                Vector2f smokePoint = MathUtils.getPointOnCircumference(proj_location, i * 3f, angle1);
                
                int alpha = 140 - (i * 4);
                
                engine.addNebulaParticle(smokePoint, smokeVel,
                		MathUtils.getRandomNumberInRange(20f, 22f) - i,
                		1.69f, //endsizemult
                		0.17f, //rampUpFraction
                		0.4f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.34f, 1.69f), //totalDuration
                		new Color(56,46,41,alpha),
                		true);
                
        	}
        	
        	INREC_RadialEmitter emitter = new INREC_RadialEmitter((CombatEntityAPI) ship);
            emitter.location(proj_location);
            emitter.angle(proj_facing, 0f);
            emitter.life(0.35f, 0.55f);
            emitter.size(3f, 6f);
    		emitter.velocity(10f, 80f);
    		emitter.distance(3f, 36f);
    		emitter.color(229,108,66,222); // SPARK_COLOR
    		emitter.velDistLinkage(false);
    		emitter.emissionOffset(-21, 42);
    		emitter.burst(36);
        	
    }
  }