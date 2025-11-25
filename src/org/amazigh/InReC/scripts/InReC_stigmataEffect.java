package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.util.Misc;

public class InReC_stigmataEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin, DamageDealtModifier {
	
	protected String weaponId;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
	}

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		if (!ship.hasListenerOfClass(InReC_stigmataEffect.class)) {
			ship.addListener(this);
			weaponId = weapon.getId();
		}
		
        float proj_facing = projectile.getFacing();
        Vector2f proj_location = projectile.getLocation();
		//main jet
		INREC_RadialEmitter emitterM = new INREC_RadialEmitter((CombatEntityAPI) ship);
        emitterM.location(proj_location);
        emitterM.angle(proj_facing, 0f);
        emitterM.life(0.4f, 0.5f);
        emitterM.size(2.2f, 4.5f);
		emitterM.velocity(25f, 34f);
		emitterM.distance(7f, 49f);
		emitterM.color(211,134,189,201);
		emitterM.coreDispersion(3f);
		emitterM.burst(24);
		
		float angle1 = proj_facing - MathUtils.getRandomNumberInRange(15f, 28f);
		float angle2 = proj_facing + MathUtils.getRandomNumberInRange(15f, 28f);
		// left/right "jets"
    	INREC_RadialEmitter emitterL = new INREC_RadialEmitter((CombatEntityAPI) ship);
        emitterL.location(proj_location);
        emitterL.angle(angle1, 0f);
        emitterL.life(0.4f, 0.5f);
        emitterL.size(2f, 4f);
		emitterL.velocity(35f, 24f);
		emitterL.distance(5f, 19f);
		emitterL.color(176,74,143,144);
		emitterL.coreDispersion(2f);
		emitterL.emissionOffset(-4f, 8f);
		emitterL.burst(11);
		INREC_RadialEmitter emitterR = new INREC_RadialEmitter((CombatEntityAPI) ship);
        emitterR.location(proj_location);
        emitterR.angle(angle2, 0f);
        emitterR.life(0.4f, 0.5f);
        emitterR.size(2f, 4f);
		emitterR.velocity(35f, 24f);
		emitterR.distance(5f, 19f);
		emitterR.color(176,74,143,144);
		emitterR.coreDispersion(2f);
		emitterR.emissionOffset(-4f, 8f);
		emitterR.burst(11);
		
		
		
        // swirly neb
		engine.addSwirlyNebulaParticle(proj_location, ship.getVelocity(),
				16f, 1.9f, 0.5f, 0.34f, 0.69f,
				new Color(197,69,61,199), true);
		
        for (int i=0; i < 3; i++) {
        	float angleN = proj_facing + MathUtils.getRandomNumberInRange(-2f, 2f);
            Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 4f, angleN);
            Vector2f smokeLoc = MathUtils.getPointOnCircumference(proj_location, i * 7f, angleN);
            // muzzle smoke
            engine.addNebulaParticle(smokeLoc, smokeVel, 18f - i, 1.9f, 0.4f, 0.69f, 1.0f, new Color(55,45,50,123));       	
        }
		
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
			Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		boolean blast = false;
		float dir = Misc.getAngleInDegrees(target.getLocation(), point);
		
		if (target != null) {
			fxVel.set(target.getVelocity());
			
			if (target instanceof ShipAPI) {
				blast = (float) Math.random() < (0.05f + (((ShipAPI) target).getFluxLevel() * 0.45f)); // 5-50% chance on ship targets
			} else {
				blast = (float) Math.random() < 0.2f; // 20% chance to explode on non-ship targets!
			}
			
			if (blast) {
				engine.applyDamage(target, point, projectile.getDamageAmount(), DamageType.FRAGMENTATION, 0, false, false, projectile.getSource());
				
				MagicLensFlare.createSharpFlare(
        			    engine,
        			    projectile.getSource(),
        			    point,
        			    4f,
        			    222f,
        			    dir + MathUtils.getRandomNumberInRange(75f, 105f),
        			    new Color(104,34,30), //69,23,20
        				new Color(125,41,46)); //93,27,31
				
				INREC_RadialEmitter emitterSpk = new INREC_RadialEmitter((CombatEntityAPI) target);
				emitterSpk.location(point);
				emitterSpk.angle(dir -44, 88f);
				emitterSpk.life(0.65f, 0.95f);
				emitterSpk.size(2f, 4f);
				emitterSpk.velocity(16f, 43f);
				emitterSpk.distance(2f, 11f);
				emitterSpk.color(208,67,91,215);
				emitterSpk.burst(23);
				
				engine.addSwirlyNebulaParticle(point, fxVel,
						23f, 2.3f, 0.5f, 0.69f, 0.69f,
						new Color(221,77,69,234), true);
				
				Global.getSoundPlayer().playSound("explosion_from_damage", 1.5f, 0.69f, point, fxVel);
			}
		}
		
		engine.addSmoothParticle(point, fxVel, 45f, 1f, 0.82f, 0.5f, new Color(243,76,215));
		engine.addSmoothParticle(point, fxVel, 27f, 1f, 0.69f, 0.5f, new Color(243,114,187));
		engine.addNebulaParticle(point, fxVel, 27f, 2f, 0.34f, 0.69f, 0.6f, new Color(251,92,143));

		float jet_facing = projectile.getFacing() + 180f;
		
		INREC_RadialEmitter emitterJet = new INREC_RadialEmitter((CombatEntityAPI) target);
		emitterJet.location(point);
		emitterJet.angle(jet_facing, 0f);
		emitterJet.life(0.31f, 0.45f);
        emitterJet.size(2.2f, 4.2f);
        emitterJet.velocity(19f, 27f);
        emitterJet.distance(5f, 37f);
        emitterJet.color(211,134,157,181); // 211,134,189,181
        	//(176,74,143,144);
        emitterJet.coreDispersion(3f);
        emitterJet.burst(18);
		
        INREC_RadialEmitter emitterSub = new INREC_RadialEmitter((CombatEntityAPI) target);
        emitterSub.location(point);
        emitterSub.angle(jet_facing, 0f);
        emitterSub.life(0.3f, 0.42f);
        emitterSub.size(2f, 4f);
		emitterSub.velocity(19f, 19f);
		emitterSub.distance(3f, 19f);
		emitterSub.color(176,74,143,131);
		emitterSub.coreDispersion(2f);
		emitterSub.burst(7);
		
        for (int i=0; i < 3; i++) {
            Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, i * 11f, jet_facing);
            Vector2f smokeLoc = MathUtils.getPointOnCircumference(point, i * 5f, jet_facing);
            engine.addNebulaParticle(smokeLoc, smokeVel, 18f - i, 1.9f, 0.4f, 0.75f, 0.84f, new Color(61,45,50,111));       	
        }
        
		if (!shieldHit) {
			float arc = 150f;
			engine.spawnDebrisSmall(point, fxVel, 7, dir, arc, 20f, 20f, 700f);
			engine.spawnDebrisMedium(point, fxVel, 2, dir, arc, 10f, 20f, 350f);
			if (blast) {
				engine.spawnDebrisLarge(point, fxVel, 1, dir, arc, 10f, 10f, 170f);
			}
		}
		
	}
	
	
	public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
		if (param instanceof DamagingProjectileAPI) {
			DamagingProjectileAPI p = (DamagingProjectileAPI) param;
			if (p.getWeapon() != null && p.getWeapon().getId().equals(weaponId)) {
				if (target instanceof ShipAPI) {
					((ShipAPI)target).setSkipNextDamagedExplosion(true);
				}
				return "InReC_stigmata";
			}
		}
		return null;
	}
}