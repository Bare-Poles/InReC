package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class InReC_guillotineOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(255,175,70,255);
	private static final Color COLOR_X = new Color(175,125,50,255);
	private static final Color COLOR_U = new Color(170,120,30,200);
	private static final Color COLOR_D_C = new Color(155,155,155,255);
	private static final Color COLOR_D_F = new Color(200,140,60,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                150f,
                105f,
                projectile.getDamageAmount(),
                projectile.getDamageAmount() * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                130,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(true);
        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
        blast.setUseDetailedExplosion(true);
        blast.setDetailedExplosionRadius(66f);
        blast.setDetailedExplosionFlashRadius(180f);
        blast.setDetailedExplosionFlashDuration(0.5f);
        
        DamagingProjectileAPI expl = engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        expl.addDamagedAlready(target);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}

    	for (int i=0; i < 2; i++) {
    		engine.addSwirlyNebulaParticle(MathUtils.getRandomPointInCircle(point, 5f),
    				MathUtils.getRandomPointInCircle(fxVel, 5f),
    				30f,
    				1.8f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.75f, 1.25f),
    				new Color(60,50,50,55),
    				true);
    		
    	}
		
        engine.spawnExplosion(point, fxVel, COLOR_U, 80f, 1.1f);

		engine.addHitParticle(point, fxVel, 136f, 1f, 0.1f, COLOR_U);
        
		//Global.getSoundPlayer().playSound("system_canister_flak_explosion", 0.7f, 0.85f, point, fxVel);
		//TODO
		// proper colors for base vfx
			// delayed detonation subs!
			// more vfx!
			// sound?...
		
	}
}
