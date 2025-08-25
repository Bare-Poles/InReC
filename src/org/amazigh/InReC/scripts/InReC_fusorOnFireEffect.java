package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_fusorOnFireEffect implements OnFireEffectPlugin {

	private static final Color FLASH_COLOR = new Color(145,100,200,215);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            float angle = projectile.getFacing();
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.addHitParticle(proj_location, ship_velocity, 39f, 0.6f, 0.1f, FLASH_COLOR.darker());
            
            INREC_RadialEmitter emitter = new INREC_RadialEmitter((CombatEntityAPI) weapon.getShip());
    		emitter.location(proj_location);
    		emitter.angle(angle -3f, 6f);
    		emitter.life(0.45f, 0.6f);
    		emitter.size(2f, 3f);
    		emitter.velocity(4f, 44f);
    		emitter.distance(0f, 10.5f);
    		emitter.color(145,100,200,215); // FLASH_COLOR
    		emitter.coreDispersion(4f);
    		emitter.burst(20);
    		
    }
  }