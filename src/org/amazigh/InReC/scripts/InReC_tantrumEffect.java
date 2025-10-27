package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_tantrumEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	
	protected String weaponId;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		if (engine.isPaused()) {
			return;
		}
		
		if (!weapon.getShip().isAlive()) {
			return;
		}
		
		ShipAPI ship = weapon.getShip();
		
		// render a glow on the ship to help show hardflux level!
    	SpriteAPI Glow1 = Global.getSettings().getSprite("fx", "InReC_estrondo_glow");
    	SpriteAPI Glow2 = Global.getSettings().getSprite("fx", "InReC_estrondo_glow");
    	Vector2f glowSize = new Vector2f(86f, 38f);
    	
    	Vector2f point = MathUtils.getPointOnCircumference(ship.getLocation(), -8f, ship.getFacing());
		
    	int alpha = 100; // half of 200 : as we scale to max glow at 50% hardflux
    	float alphaMult = Math.min(200f, alpha * (ship.getHardFluxLevel() * 2f)); // capping at 200 for negative timeflow protection
    	double alphaTemp = alphaMult;
		double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
		alpha = (int) Math.ceil(alphaTemp / timeMult);
    	alpha = Math.min(alpha, 255);
		
    	MagicRender.singleframe(Glow1, point, glowSize, ship.getFacing() - 90f, new Color(255,255,255,alpha), true);
    	MagicRender.singleframe(Glow2, MathUtils.getRandomPointInCircle(point, 1f), glowSize, ship.getFacing() - 90f, new Color(220,220,220,alpha), true);
		
	}

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		ShipAPI ship = weapon.getShip();
		float proj_facing = projectile.getFacing();
        Vector2f proj_location = projectile.getLocation();
        
        ProjSpecificData info = (ProjSpecificData) Global.getCombatEngine().getCustomData().get("TANTRUM_DATA_KEY" + projectile.hashCode());
        if (info == null) {
            info = new ProjSpecificData();
        }
        
        INREC_RadialEmitter emitter = new INREC_RadialEmitter((CombatEntityAPI) weapon.getShip());
        emitter.location(proj_location);
        emitter.angle(proj_facing -5f, 10f);
        emitter.life(0.45f, 0.7f);
        emitter.size(2f, 4f);
		emitter.velocity(76f, -69f);
		emitter.distance(0f, 7.5f);
		emitter.color(169,52,197,200);
		emitter.coreDispersion(4f);
		emitter.lifeLinkage(true);
		emitter.burst(12);
		
		engine.addSwirlyNebulaParticle(proj_location,
				MathUtils.getPointOnCircumference(ship.getVelocity(), 8f, proj_facing),
				16f,
				1.69f,
				0.34f,
				0.4f,
				0.7f,
				new Color(95,125,150,189),
				true);
		
		float hardEaten = ship.getFluxTracker().getHardFlux() * 0.03f;
		float softEaten = ship.getFluxTracker().getCurrFlux() * 0.03f;
		
		ship.getFluxTracker().setHardFlux(Math.max(0f, ship.getFluxTracker().getHardFlux() - hardEaten)); // flux drain
		ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() - softEaten));
		
		if (softEaten > 70f) {
			// extra neb 1!
			engine.addSwirlyNebulaParticle(proj_location,
					MathUtils.getPointOnCircumference(ship.getVelocity(), 19f, proj_facing),
					15f,
					1.67f,
					0.34f,
					0.4f,
					0.75f,
					new Color(95,125,150,169),
					true);
			if (softEaten > 140f) {
				// extra neb 2!
				engine.addSwirlyNebulaParticle(proj_location,
						MathUtils.getPointOnCircumference(ship.getVelocity(), 28f, proj_facing),
						14f,
						1.64f,
						0.34f,
						0.4f,
						0.8f,
						new Color(95,125,150,146),
						true);
				// more sparks
				int sparkCount = Math.min(12, (int) (softEaten * 0.05));
				INREC_RadialEmitter emitter2 = new INREC_RadialEmitter((CombatEntityAPI) weapon.getShip());
		        emitter2.location(proj_location);
		        emitter2.angle(proj_facing, 0f);
		        emitter2.life(0.69f, 1.1f);
		        emitter2.size(1.5f, 3f);
				emitter2.velocity(6f, 9f);
				emitter2.distance(2f, 34f + (sparkCount * 2f));
				emitter2.color(87,219,189,200);
				emitter2.coreDispersion(1f);
				emitter2.burst(sparkCount);
				if (softEaten > 200f) {
					// extra neb 3!
					engine.addSwirlyNebulaParticle(proj_location,
							MathUtils.getPointOnCircumference(ship.getVelocity(), 35f, proj_facing),
							13f,
							1.6f,
							0.34f,
							0.4f,
							0.85f,
							new Color(95,125,150,121),
							true);
					// vis arc
			        float angleRandom = proj_facing + MathUtils.getRandomNumberInRange(-10, 10);
			        Vector2f arcEnd = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(34f, 69f), angleRandom);
			        
			        engine.spawnEmpArcVisual(proj_location, projectile.getSource(), arcEnd, ship, 9f,
							new Color(150,70,175,45),
							new Color(200,255,225,52));
			        
					Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.8f, 0.69f, proj_location, ship.getVelocity());
				}
			}
		}
		
		info.FLUX_EATEN = hardEaten;
		Global.getCombatEngine().getCustomData().put("TANTRUM_DATA_KEY" + projectile.hashCode(), info);
	}
	

    private class ProjSpecificData {
        private float FLUX_EATEN = 0f;
    }
    
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
			Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		
		if (target != null) {
			fxVel.set(target.getVelocity());
			
			ProjSpecificData info = (ProjSpecificData) Global.getCombatEngine().getCustomData().get("TANTRUM_DATA_KEY" + projectile.hashCode());
			if (target instanceof ShipAPI) {
		        if (info != null) {
		        	((ShipAPI) target).getFluxTracker().increaseFlux(info.FLUX_EATEN, true);
		        		// hardflux spike!
		        }
			}
		}
		
        float dir = Misc.getAngleInDegrees(target.getLocation(), point);
        
    	float distanceRandom1 = MathUtils.getRandomNumberInRange(9f, 19f);
		float angleRandom1 = dir + MathUtils.getRandomNumberInRange(69, 111);
        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
        
        float distanceRandom2 = MathUtils.getRandomNumberInRange(9f, 19f);
        float angleRandom2 = dir - MathUtils.getRandomNumberInRange(69, 111);
        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
        
        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 9f,
				new Color(150,70,175,45),
				new Color(225,200,255,52));
        
        engine.addHitParticle(point, fxVel, 49f, 1f, 0.1f, new Color(216,66,252,246));
        
        engine.addSwirlyNebulaParticle(point,
        		fxVel,
				20f,
				1.9f,
				0.56f,
				0.5f,
				0.55f,
				new Color(39,151,193,205),
				true);
        
		INREC_RadialEmitter emitterSpk = new INREC_RadialEmitter((CombatEntityAPI) target);
		emitterSpk.location(point);
		emitterSpk.angle(dir -60, 120f);
		emitterSpk.life(0.5f, 0.85f);
		emitterSpk.size(2f, 4f);
		emitterSpk.velocity(15f, 50f);
		emitterSpk.distance(0f, 5f);
		emitterSpk.color(139,52,197,200);
		emitterSpk.burst(18);
		
	}
	
}