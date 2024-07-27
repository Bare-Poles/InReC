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
	private IntervalUtil interval2 = new IntervalUtil(1.5f,1.75f); // self destruct delay
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	// to summarize, this script causes the weapon to start "smoking" and "sparking" when low on ammo, and when ammo is depleted, causes the ship to selfdestruct after a brief delay 
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		ShipAPI ship = weapon.getShip();
		
		if (!ship.isAlive() ) {
			return;
		}
		
		if (weapon.getAmmo() <= 30) {
			interval1.advance(amount);
		}
		
		if (weapon.getAmmo() == 0) {
			interval2.advance(amount);
		}
		
		if (interval1.intervalElapsed()) {
			
			int alpha = (int) (150 - (weapon.getAmmo() * 2)); 
			
			Vector2f nebVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(2f, 8f));
    		engine.addSwirlyNebulaParticle(ship.getLocation(),
    				nebVel,
    				20f,
    				1.5f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.2f, 0.4f),
    				new Color(75,130,124,alpha),
    				true);
    		
    		engine.addNebulaParticle(ship.getLocation(),
    				nebVel,
    				20f, //size
    				1.8f, //endSizeMult
    				0.4f, //rampUpFraction
    				0.4f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(0.3f, 0.5f),
    				new Color(95,110,105,60));
    		
    		if (weapon.getAmmo() <= 15) {

    	        for (int i = 0; i < 2; i++) {
        			int sparkAlpha = Math.min(255, (int) (alpha * 2));
        			float angle = MathUtils.getRandomNumberInRange(0f, 360f);
        			engine.addSmoothParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 4f, angle),
                			MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(12f, 40f), angle),
                			MathUtils.getRandomNumberInRange(4f, 9f),
                			1f,
                			MathUtils.getRandomNumberInRange(0.2f, 0.4f),
                			new Color(60,220,210,sparkAlpha));
    	        }
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
        				33f,
        				1.5f,
        				0.6f,
        				0.75f,
        				MathUtils.getRandomNumberInRange(0.7f, 0.9f),
        				new Color(60,163,155,175),
        				true);
        		
            	for (int j=0; j < 7; j++) {
        			
        			Vector2f sparkVel = MathUtils.getRandomPointInCircle(ship.getVelocity(), MathUtils.getRandomNumberInRange(15f, 55f));
                	engine.addSmoothParticle(weapon.getLocation(),
                			sparkVel,
                			MathUtils.getRandomNumberInRange(2f, 3f),
                			1f,
                			MathUtils.getRandomNumberInRange(0.35f, 0.6f),
                			new Color(60,220,210,255));
        		}
            }
            
            // engine.removeEntity(weapon.getShip());
    		
		}
		
	}
	
  }