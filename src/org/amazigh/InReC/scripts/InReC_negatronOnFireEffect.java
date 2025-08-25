package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_negatronOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(60,220,210,210);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            float angle = projectile.getFacing();
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.addHitParticle(proj_location, ship_velocity, 45f, 1f, 0.1f, FLASH_COLOR.darker());
            
        	INREC_RadialEmitter emitter = new INREC_RadialEmitter((CombatEntityAPI) weapon.getShip());
            emitter.location(proj_location);
            emitter.angle(angle, 0f);
            emitter.life(0.9f, 0.9f);
            emitter.size(2f, 3f);
    		emitter.velocity(3f, 75f);
    		emitter.distance(0.8f, 9.2f);
    		emitter.color(60,220,210,210); // FLASH_COLOR
    		emitter.emissionOffset(-3, 6);
    		emitter.coreDispersion(4f);
    		emitter.burst(24);
            
            for (int i=0; i < 2; i++) {
            	Vector2f neb_velocity = MathUtils.getPointOnCircumference(ship_velocity, 10f, angle);
        		engine.addSwirlyNebulaParticle(proj_location,
        				neb_velocity,
        				15f * i,
        				1.5f,
        				0.3f,
        				0.4f,
        				MathUtils.getRandomNumberInRange(0.3f, 0.4f),
        				new Color(95,150,125,75),
        				true);
            }
    		
    }
  }