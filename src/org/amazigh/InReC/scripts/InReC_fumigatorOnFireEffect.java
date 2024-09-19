package org.amazigh.InReC.scripts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_fumigatorOnFireEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    private int shotCounter = 0;
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        Vector2f proj_location = projectile.getLocation();
        float angle = projectile.getFacing();
        
		shotCounter++;
    	if (shotCounter >= 2) {
    		shotCounter = 0;
    		
    		engine.spawnProjectile(projectile.getSource(),
                    projectile.getWeapon(), "InReC_flare_dumb",
                    proj_location,
                    angle,
                    ship_velocity);

        	engine.removeEntity(projectile);
    	}
    	
    	
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}

  }