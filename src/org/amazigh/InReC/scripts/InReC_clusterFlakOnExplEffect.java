package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class InReC_clusterFlakOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(219,255,125,220);
	private static final Color COLOR_X1 = new Color(215,250,123,50);
	private static final Color COLOR_X2 = new Color(145,175,130,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = MathUtils.getRandomPointOnCircumference(explosion.getLocation(), MathUtils.getRandomNumberInRange(18f, 30f));
		
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                60f,
                0.8f,
                0.1f,
                COLOR_P);
		
		engine.addNebulaParticle(point,
				explosion.getVelocity(),
				MathUtils.getRandomNumberInRange(40f, 50f), //size
				1.5f, //endSizeMult
				0f, //rampUpFraction
				0.35f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.15f, 0.3f), //dur
				COLOR_X1);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(explosion.getVelocity(), 19f),
    				MathUtils.getRandomNumberInRange(42f, 75f), //size
    				1.8f, //endSizeMult
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(0.75f, 1.2f), //dur
    				COLOR_X2);
        }
        
        DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                40f,
                30f,
                explosion.getDamageAmount() * 0.8f,
                explosion.getDamageAmount() * 0.4f,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                60,
                COLOR_P,
                COLOR_P);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(false);
        engine.spawnDamagingExplosion(blast,explosion.getSource(),point,false);
        
	}
}
