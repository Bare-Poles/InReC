package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
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
		
		INREC_RadialEmitter emitter = new INREC_RadialEmitter((CombatEntityAPI) target);
		emitter.location(point);
		emitter.life(0.4f, 0.69f);
		emitter.size(4f, 8f);
		emitter.velocity(12f, 12f);
		emitter.distance(0f, 200f);
		emitter.color(65,220,195,175);
		emitter.burst(30);
		
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                200f,
                160f,
                projectile.getDamageAmount() * 0.5f, // 50
                projectile.getDamageAmount() * 0.2f, // 20
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
	                    	
	                    	int pCount1 = 1 + (int) (target.getCollisionRadius() * 0.04f);
	                    	int pCount2 = 1 + (int) (target.getCollisionRadius() * 0.03f);
	                    	
	                    	INREC_RadialEmitter emitter1 = new INREC_RadialEmitter((CombatEntityAPI) target);
	                		emitter1.location(target.getLocation());
	                		emitter1.life(0.2f, 0.4f);
	                		emitter1.size(3f, 7f);
	                		emitter1.velocity(-5f, -9f);
	                		emitter1.distance(target.getCollisionRadius() * 0.65f, target.getCollisionRadius() * 0.35f);
	                		emitter1.color(65,220,195,125);
	                		emitter1.burst(pCount1);
	                		
	                		INREC_RadialEmitter emitter2 = new INREC_RadialEmitter((CombatEntityAPI) target);
	                		emitter2.location(target.getLocation());
	                		emitter2.life(0.2f, 0.38f);
	                		emitter2.size(3f, 7f);
	                		emitter2.velocity(-5f, -9f);
	                		emitter2.distance(target.getCollisionRadius() * 0.55f, target.getCollisionRadius() * 0.5f);
	                		emitter2.color(65,220,195,120);
	                		emitter2.burst(pCount2);
	                    	
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
