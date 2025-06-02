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

public class InReC_guillotineOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(251,130,30,215);
//    private static final Color SPARK_COLOR = new Color(60,220,210,235);
    
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
        	}
            
            INREC_RadialEmitter emitterCore = new INREC_RadialEmitter((CombatEntityAPI) ship);
            emitterCore.location(proj_location);
            emitterCore.angle(angle -12f);
            emitterCore.arc(24f);
            emitterCore.life(1.1f, 1.4f);
            emitterCore.size(3f, 8f);
    		emitterCore.velocity(5f, 64f);
    		emitterCore.distance(3f, 45f);
    		emitterCore.color(251,130,30,215); // FLASH_COLOR
    		emitterCore.velDistLinkage(false);
    		emitterCore.burst(60);
    		
            
            // "special" sparks
    		
            INREC_RadialEmitter emitterSpc1 = new INREC_RadialEmitter((CombatEntityAPI) ship);
            emitterSpc1.location(proj_location);
            emitterSpc1.angle(angle);
            emitterSpc1.arc(0f);
            emitterSpc1.life(1.01f, 1.93f);
            emitterSpc1.size(2f, 3f);
            emitterSpc1.velocity(70f, -46f);
            emitterSpc1.distance(2f, 94f);
    		emitterSpc1.color(60,220,210,205); // SPARK_COLOR
    		emitterSpc1.coreDispersion(5f);
    		emitterSpc1.emissionOffset(40f, 15f);
    		emitterSpc1.lifeLinkage(true);
    		emitterSpc1.burst(47);
    		
    		INREC_RadialEmitter emitterSpc2 = new INREC_RadialEmitter((CombatEntityAPI) ship);
            emitterSpc2.location(proj_location);
            emitterSpc2.angle(angle);
            emitterSpc2.arc(0f);
            emitterSpc2.life(1.01f, 1.93f);
            emitterSpc2.size(2f, 3f);
            emitterSpc2.velocity(70f, -46f);
            emitterSpc2.distance(2f, 94f);
    		emitterSpc2.color(60,220,210,205); // SPARK_COLOR
    		emitterSpc2.coreDispersion(5f);
    		emitterSpc2.emissionOffset(-55f, 15f);
    		emitterSpc2.lifeLinkage(true);
    		emitterSpc2.burst(47);
            
            
    }
  }
