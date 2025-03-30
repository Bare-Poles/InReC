package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;

public class InReC_quarkOnHitEffect implements OnHitEffectPlugin {

	public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target,
					  final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
        if (target instanceof ShipAPI) {
			if (shieldHit) {
				// nothing, cry about it! ;)
			} else {
				
				ShipAPI tagShip = (ShipAPI) target;
				
				if (tagShip.isHulk() || !tagShip.isAlive()) {
					return; // no embedding on dead ships/hulks, because it can cause crashes!
				}
				
				
				
				if (!tagShip.hasListenerOfClass(INREC_QuarkComboMult.class)) {
					tagShip.addListener(new INREC_QuarkComboMult(tagShip));
				}
				
				List<INREC_QuarkComboMult> listeners = tagShip.getListeners(INREC_QuarkComboMult.class);
				if (listeners.isEmpty()) return; // ??? (idk either alex, but sanity checks are a real one)
				
				INREC_QuarkComboMult listener = listeners.get(0);
				listener.notifyHit(projectile.hashCode());
				
				
				engine.addPlugin(new EveryFrameCombatPlugin() {
					
	                final float initialFacing = target.getFacing();
	                final float baseDamage = projectile.getDamageAmount();
	                final Vector2f shipRefHitLoc = new Vector2f(point.x - target.getLocation().x, point.y - target.getLocation().y);
	                
	                final float impactAngle = projectile.getFacing() - target.getFacing();
	                
	                final IntervalUtil FXTimer = new IntervalUtil(0.1f, 0.1f);
	                final IntervalUtil burstTimer = new IntervalUtil(5f, 5f);
	                
	                @Override
	                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
	                	
	                }
	                
	                @Override
	                public void advance(float amount, List<InputEventAPI> events) {
	                    if (engine.isPaused()) return;
	                    burstTimer.advance(amount);
	                    FXTimer.advance(amount);
	                    
	                    Vector2f hitLoc = new Vector2f();
	                    if (burstTimer.intervalElapsed() || FXTimer.intervalElapsed()) {
	                        hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
	                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);
	                    }
		                
	                    if (FXTimer.intervalElapsed()) {
	                    	
	                    	if (((ShipAPI) target).isHulk() || !((ShipAPI) target).isAlive()) {
	                    		Global.getSoundPlayer().playSound("hit_glancing_energy", 0.84f, 1.0f, hitLoc, target.getVelocity());
		                    	
		                    	engine.spawnExplosion(hitLoc, target.getVelocity(), new Color(207,171,60,225), 13f, 0.3f);
		                    	
		                    	for (int i=0; i < 30; i++) {
		                    		float angle = (i * 12f) + MathUtils.getRandomNumberInRange(0f, 10f);
		                    		Vector2f sparkVel = MathUtils.getPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(10f, 57f), angle);
		                    		
			                    	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(hitLoc, 2f),
			                        		sparkVel,
			                				MathUtils.getRandomNumberInRange(3f, 6f), //size
			                				1f, //brightness
			                				MathUtils.getRandomNumberInRange(0.35f, 0.55f), //duration
			                				new Color(229,108,66,222));
		                    	}
		                    	
		                    	for (int i=0; i < 2; i++) {
			                    	engine.addNebulaParticle(hitLoc, target.getVelocity(),
			                        		MathUtils.getRandomNumberInRange(16f, 21f),
			                        		1.8f, //endsizemult
			                        		0.18f, //rampUpFraction
			                        		0.56f, //fullBrightnessFraction
			                        		MathUtils.getRandomNumberInRange(0.5f, 0.7f), //totalDuration
			                        		new Color(104,85,30,111),
			                        		true);
		                    	}
		                    	
		                    	engine.removePlugin(this);
	                    	}
	                    	
	                    	float ejectAngle = (target.getFacing() - initialFacing) + impactAngle;
	                    	
	                    	for (int i=0; i < 3; i++) {
	                    		
	                    		float angle = ejectAngle + MathUtils.getRandomNumberInRange(77f, 103f);
	                    		Vector2f sparkVel = MathUtils.getPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(19f, 69f), angle);
	                    		
		                    	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(hitLoc, 2f),
		                        		sparkVel,
		                				MathUtils.getRandomNumberInRange(2.5f, 5.2f), //size
		                				1f, //brightness
		                				MathUtils.getRandomNumberInRange(0.3f, 0.6f), //duration
		                				new Color(229,108,66,222));
	                    	}
	                    	
	                    	engine.addNebulaParticle(hitLoc, target.getVelocity(),
	                        		MathUtils.getRandomNumberInRange(14f, 19f),
	                        		1.81f, //endsizemult
	                        		0.2f, //rampUpFraction
	                        		0.69f, //fullBrightnessFraction
	                        		MathUtils.getRandomNumberInRange(0.4f, 0.6f), //totalDuration
	                        		new Color(60,34,30,111),
	                        		true);
	                    	
	                    	engine.addSmoothParticle(hitLoc, target.getVelocity(),
	                    			27f, //size
                    				0.9f, //brightness
                    				0.15f, //duration
                    				new Color(207,171,60,169));
	                    }
	                    
	                    if (burstTimer.intervalElapsed()) {
	                    	
	                    	ShipAPI tagShip = (ShipAPI) target;
	                    	List<INREC_QuarkComboMult> listeners = tagShip.getListeners(INREC_QuarkComboMult.class);
	        				
	        				int count = 1;
	        				float comboMult = 1f;
	        				
	        				if (listeners.isEmpty())  {
	        					// this is a sanity check, so we don't try and look at the listener if it's somehow not there!
	        				} else {
	        					INREC_QuarkComboMult listener = listeners.get(0);
	        					count = listener.recentHits.getItems().size();
		        				comboMult = Math.max(1f, 1f + Math.min(2f, (count - 1) * 0.05f));	        					
	        				}
	        				int fxScale = Math.max(40, count);
	                    	// we cap the scaling effects at 40x (for comboMult: 40*0.05 = 2)
	        				
	                    	float typeMult = 1f; // this script is used across three weapons! determining the mult to apply based on weapon type, so all have functionally the same onHit damage
	                    	if (projectile.getWeapon().getType() == WeaponType.BALLISTIC) {
	                    		typeMult /= 3f;
	                    	} else {
	                    		typeMult *= 0.5f;
	                    	}
	                    	
//	                    	engine.addFloatingTextAlways(hitLoc,
//	                    			 "Count: " + count + " Mult: " + comboMult,
//	        						NeuralLinkScript.getFloatySize((ShipAPI) target), new Color(250,215,69,255), target,
//	        						12f, // flashFrequency
//	        						4f, // flashDuration
//	        						0.5f, // durInPlace
//	        						2f, // durFloatingUp
//	        						1.5f, // durFadingOut
//	        						1f); // baseAlpha
	                    	
	                    	engine.applyDamage(target, hitLoc, baseDamage * typeMult * comboMult, DamageType.FRAGMENTATION, 0, true, true, projectile.getSource());
	                    	
	                    	Global.getSoundPlayer().playSound("hit_glancing_energy", 0.84f, 1.0f + (fxScale * 0.01f), hitLoc, target.getVelocity());
	                    	
	                    	engine.spawnExplosion(hitLoc, target.getVelocity(), new Color(207,171,60,225), 13f + (0.5f * fxScale), 0.3f);
	                    	engine.addHitParticle(hitLoc, target.getVelocity(), 34f + fxScale, 1f, 0.1f, new Color(207,134,60,225));
	                    	
	                    	for (int i=0; i < 36; i++) {
	                    		float angle = (i * 10f) + MathUtils.getRandomNumberInRange(0f, 8f);
	                    		Vector2f sparkVel = MathUtils.getPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(10f, 57f), angle);
	                    		
		                    	engine.addSmoothParticle(MathUtils.getPointOnCircumference(hitLoc, MathUtils.getRandomNumberInRange(2f, 7f), angle),
		                        		sparkVel,
		                				MathUtils.getRandomNumberInRange(3f, 6f), //size
		                				1f, //brightness
		                				MathUtils.getRandomNumberInRange(0.35f, 0.55f), //duration
		                				new Color(232,101,66,222));
	                    	}
	                    	
	                    	for (int i=0; i < 2; i++) {
		                    	engine.addNebulaParticle(hitLoc, target.getVelocity(),
		                        		MathUtils.getRandomNumberInRange(16f, 21f) + fxScale,
		                        		1.8f, //endsizemult
		                        		0.18f, //rampUpFraction
		                        		0.56f, //fullBrightnessFraction
		                        		MathUtils.getRandomNumberInRange(0.5f, 0.7f), //totalDuration
		                        		new Color(104,85,30,111),
		                        		true);
	                    	}
	                    	
	                    	engine.removePlugin(this);
	                    }
	                    
	                }

	                @Override
	                public void renderInWorldCoords(ViewportAPI viewport) {

	                }

	                @Override
	                public void renderInUICoords(ViewportAPI viewport) {

	                }

	                @Override
	                public void init(CombatEngineAPI engine) {

	                }
	            });
			}
    	}
		
	}
	
	// taken from the graviton beam effect! we're being cute!
	public static class INREC_QuarkComboMult implements AdvanceableListener {
		protected ShipAPI ship;
		
		protected TimeoutTracker<Integer> recentHits = new TimeoutTracker<Integer>();
		public INREC_QuarkComboMult(ShipAPI ship) {
			this.ship = ship;
		}
		
		public void notifyHit(int hash) {
			recentHits.add(hash, 5.1f, 5.1f); // it crashed one time when this was set to a flat 5 (matching actual lifetime) so i bumped it up to hopefully catch any issues there.
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);

			int count = recentHits.getItems().size();
			
			if (count < 1) {
				ship.removeListener(this);
			}
			
			if (ship.isHulk() || !ship.isAlive()) {
				ship.removeListener(this);
			}
		}
		
		public String modifyDamageTaken(Object param,
				CombatEntityAPI target, DamageAPI damage,
				Vector2f point, boolean shieldHit) {
			return null;
		}
	}
	
}
