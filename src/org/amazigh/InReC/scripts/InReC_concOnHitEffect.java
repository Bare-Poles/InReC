package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
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
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class InReC_concOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target, final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		engine.addSmoothParticle(point,
				fxVel,
    			375f,
    			1f,
    			0.15f,
    			new Color(60,220,210,69));
		engine.addSmoothParticle(point,
				fxVel,
    			200f,
    			1f,
    			0.25f,
    			new Color(60,210,220,99));
		
		for (int i = 0; i < 30; i++) {
    		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    		Vector2f sparkPoint = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(0f, 200f), angle);
    		Vector2f sparkVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(12f, 24f), angle);
			engine.addSmoothParticle(sparkPoint,
    				sparkVel,
        			MathUtils.getRandomNumberInRange(4f, 8f),
        			1f,
        			MathUtils.getRandomNumberInRange(0.4f, 0.69f),
        			new Color(65,220,195,175));
    	}
    	
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                200f,
                160f,
                projectile.getDamageAmount() * 0.4f, // 20
                projectile.getDamageAmount() * 0.2f, // 10
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                1,
                new Color(6,22,20,0),
                new Color(6,22,20,0));
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(false);
        
        DamagingProjectileAPI expl = engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
		if (target != null) {
			expl.addDamagedAlready(target);
			
			if (target instanceof ShipAPI) {
				
				String hash = "" + projectile.hashCode();
				((ShipAPI) target).getMutableStats().getMaxSpeed().modifyMult(hash, 0.97f);
				
				((ShipAPI) target).getMutableStats().getAcceleration().modifyMult(hash, 0.97f);
				((ShipAPI) target).getMutableStats().getDeceleration().modifyMult(hash, 0.97f);
				((ShipAPI) target).getMutableStats().getTurnAcceleration().modifyMult(hash, 0.97f);
				((ShipAPI) target).getMutableStats().getMaxTurnRate().modifyMult(hash, 0.97f);
				
				((ShipAPI) target).getMutableStats().getMaxRecoilMult().modifyPercent(hash, 3);
				((ShipAPI) target).getMutableStats().getRecoilPerShotMult().modifyPercent(hash, 3);
				
				((ShipAPI) target).getMutableStats().getWeaponTurnRateBonus().modifyMult(hash, 0.97f);
				
				engine.addPlugin(new EveryFrameCombatPlugin() {
					
	                int lifeTimer = 0;
	                final IntervalUtil FXTimer = new IntervalUtil(0.2f, 0.2f);
	                
	                @Override
	                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
	                	
	                }
	                
	                @Override
	                public void advance(float amount, List<InputEventAPI> events) {
	                    if (engine.isPaused()) return;
	                    FXTimer.advance(amount);
	                    
	                    if (FXTimer.intervalElapsed()) {
	                    	
	                    	int pCount = 2 + (int) (target.getCollisionRadius() * 0.08f);
	                    	float pAngle = 360f / pCount;
	                    	
	                    	for (int i = 0; i < pCount; i++) {
	                    		float angle = (i * pAngle) + MathUtils.getRandomNumberInRange(0f, pAngle);
	                    		Vector2f sparkPoint = MathUtils.getPointOnCircumference(target.getLocation(),target.getCollisionRadius() * MathUtils.getRandomNumberInRange(0.75f, 1f), angle);
	                    		Vector2f sparkVel = MathUtils.getPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(-5f, -13f), angle);
	                			engine.addSmoothParticle(sparkPoint,
	                    				sparkVel,
	                        			MathUtils.getRandomNumberInRange(3f, 7f),
	                        			1f,
	                        			MathUtils.getRandomNumberInRange(0.2f, 0.4f),
	                        			new Color(65,220,195,125));
	                    	}
	                        
	                        lifeTimer ++;
	                    }
	                    
	                    if (lifeTimer >= 50) {
	                    	
	        				String hash = "" + projectile.hashCode();
	                    	((ShipAPI) target).getMutableStats().getMaxSpeed().unmodify(hash);
	        				
	        				((ShipAPI) target).getMutableStats().getAcceleration().unmodify(hash);
	        				((ShipAPI) target).getMutableStats().getDeceleration().unmodify(hash);
	        				((ShipAPI) target).getMutableStats().getTurnAcceleration().unmodify(hash);
	        				((ShipAPI) target).getMutableStats().getMaxTurnRate().unmodify(hash);
	        				
	        				((ShipAPI) target).getMutableStats().getMaxRecoilMult().unmodify(hash);
	        				((ShipAPI) target).getMutableStats().getRecoilPerShotMult().unmodify(hash);
	        				
	        				((ShipAPI) target).getMutableStats().getWeaponTurnRateBonus().unmodify(hash);
	                    	
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
	
}
