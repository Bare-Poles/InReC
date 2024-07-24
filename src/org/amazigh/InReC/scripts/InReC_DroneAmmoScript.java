package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class InReC_DroneAmmoScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
	private IntervalUtil interval1 = new IntervalUtil(0.15f,0.2f); // vfx
	private IntervalUtil interval2 = new IntervalUtil(0.7f,0.75f); // self destruct delay
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	// to summarize, this script causes the weapon to start "smoking" and "sparking" when low on ammo, and when ammo is depleted, causes the ship to selfdestruct after a brief delay 
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		ShipAPI ship = weapon.getShip();
		
		if (weapon.getAmmo() <= 24) {
			interval1.advance(amount);
		}
		
		if (weapon.getAmmo() == 0) {
			interval2.advance(amount);
		}
		
		
		if (interval1.intervalElapsed()) {
			
			int alpha = (int) (75 - (weapon.getAmmo() * 2)); 
			
			Vector2f nebVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(2f, 8f));
    		engine.addSwirlyNebulaParticle(weapon.getLocation(),
    				nebVel,
    				10f,
    				1.5f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.2f, 0.3f),
    				new Color(75,110,105,alpha),
    				true);
    		
    		if (weapon.getAmmo() <= 12) {
    			
    			int sparkAlpha = (int) (alpha * 3);
    			Vector2f sparkVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(12f, 40f));
            	engine.addSmoothParticle(weapon.getLocation(),
            			sparkVel,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			MathUtils.getRandomNumberInRange(0.15f, 0.3f),
            			new Color(60,220,210,sparkAlpha));
    		}
    		
		}
		
		
		if (interval2.intervalElapsed()) {

            weapon.getShip().setHitpoints(1f);
            
			Global.getCombatEngine().applyDamage(ship,
					weapon.getLocation(),
					200f,
					DamageType.ENERGY,
					0f,
					true,
					false,
					ship);
			
            for (int i=0; i < 2; i++) {
            	
    			Vector2f nebVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(2f, 10f));
        		engine.addSwirlyNebulaParticle(weapon.getLocation(),
        				nebVel,
        				23f,
        				1.5f,
        				0.6f,
        				0.5f,
        				MathUtils.getRandomNumberInRange(0.4f, 0.6f),
        				new Color(75,110,105,75),
        				true);
        		
            	for (int j=0; j < 7; j++) {
        			
        			Vector2f sparkVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(15f, 55f));
                	engine.addSmoothParticle(weapon.getLocation(),
                			sparkVel,
                			MathUtils.getRandomNumberInRange(2f, 3f),
                			1f,
                			MathUtils.getRandomNumberInRange(0.35f, 0.6f),
                			new Color(60,220,210,250));
        		}
            }
            
            // engine.removeEntity(weapon.getShip());
    		
		}
		
	}
	
  }