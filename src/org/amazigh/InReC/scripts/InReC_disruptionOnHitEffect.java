package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class InReC_disruptionOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target, final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
		if (target != null) {
			
			if (target instanceof ShipAPI) {
				if (shieldHit) {
					// nothing, cry about it! ;)
				} else {
					
					Vector2f nebVel = MathUtils.getRandomPointInCircle(target.getVelocity(), MathUtils.getRandomNumberInRange(2f, 10f));
            		engine.addSwirlyNebulaParticle(point,
            				nebVel,
            				10f,
            				1.5f,
            				0.6f,
            				0.5f,
            				MathUtils.getRandomNumberInRange(0.2f, 0.6f),
            				new Color(95,125,150,75),
            				true);
					
					// hi! thanks VIC, i stole code again lol!
					engine.addPlugin(new EveryFrameCombatPlugin() {
						
		                final float initialFacing = target.getFacing();
		                final int arcCount = 4;
		                
		                int arcsFired = 0;
		                float arcDamage = projectile.getDamageAmount() * 0.2f;
		                float arcEmp = projectile.getEmpAmount() * 0.2f;
		                
		                final Vector2f shipRefHitLoc = new Vector2f(point.x - target.getLocation().x, point.y - target.getLocation().y);
		                final IntervalUtil arcTimer = new IntervalUtil(0.2f, 0.3f);
		                final IntervalUtil FXTimer1 = new IntervalUtil(0.05f, 0.1f);
		                
		                @Override
		                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
		                	
		                }
		                
		                @Override
		                public void advance(float amount, List<InputEventAPI> events) {
		                    if (engine.isPaused()) return;
		                    arcTimer.advance(amount);
		                    FXTimer1.advance(amount);

		                    Vector2f hitLoc = new Vector2f();
		                    
		                    if (arcTimer.intervalElapsed()) {
		                        hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
		                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);
		                    	
		                    	// we arc!
		                    	engine.spawnEmpArcPierceShields(projectile.getSource(), hitLoc, target, target,
		        						DamageType.ENERGY,
		        						arcDamage, // damage
		        						arcEmp, // emp
		        						1000f, // max range
		        						"tachyon_lance_emp_impact",
		        						22f, // thickness
		        						new Color(25,110,146,255),
		        	    				new Color(255,255,255,255));
		                    	
		                    	Vector2f nebVel = MathUtils.getRandomPointInCircle(target.getVelocity(), MathUtils.getRandomNumberInRange(2f, 10f));
		                		engine.addSwirlyNebulaParticle(hitLoc,
		                				nebVel,
		                				10f,
		                				1.5f,
		                				0.6f,
		                				0.5f,
		                				MathUtils.getRandomNumberInRange(0.2f, 0.6f),
		                				new Color(95,125,150,75),
		                				true);
		                		
		                    	arcsFired++;
		                    }
		                    
		                    
		                    if (FXTimer1.intervalElapsed()) {
		                        hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
		                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);

		                        for (int i=0; i < 3; i++) {
			                    	Vector2f sparkVel = MathUtils.getRandomPointInCircle(target.getVelocity(), MathUtils.getRandomNumberInRange(15f, 55f));
			                    	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(hitLoc, 5f);
			                    	engine.addSmoothParticle(sparkLoc,
			                    			sparkVel,
			                    			MathUtils.getRandomNumberInRange(2f, 5f),
			                    			1f,
			                    			MathUtils.getRandomNumberInRange(0.3f, 0.45f),
			                    			new Color(90,200,225,240));
		                        }
		                    }
		                    
		                    if (arcsFired >= arcCount) engine.removePlugin(this);
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
		
		
	}
	
}
