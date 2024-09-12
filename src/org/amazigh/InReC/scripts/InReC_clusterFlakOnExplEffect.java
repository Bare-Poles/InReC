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
		
		Vector2f point1 = MathUtils.getRandomPointOnCircumference(explosion.getLocation(), MathUtils.getRandomNumberInRange(18f, 30f));
		
		engine.addHitParticle(
                point1,
                explosion.getVelocity(),
                60f,
                0.8f,
                0.1f,
                COLOR_P);
		
		engine.addNebulaParticle(point1,
				explosion.getVelocity(),
				MathUtils.getRandomNumberInRange(40f, 50f), //size
				1.5f, //endSizeMult
				0f, //rampUpFraction
				0.35f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.15f, 0.3f), //dur
				COLOR_X1);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(point1,
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
                explosion.getDamageAmount() * 0.6f,
                explosion.getDamageAmount() * 0.3f,
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
        engine.spawnDamagingExplosion(blast,explosion.getSource(),point1,false);
        
        
        Vector2f point2 = MathUtils.getRandomPointOnCircumference(explosion.getLocation(), MathUtils.getRandomNumberInRange(28f, 40f));
        
        engine.addHitParticle(
                point2,
                explosion.getVelocity(),
                40f,
                0.8f,
                0.1f,
                COLOR_P);
		
		engine.addNebulaParticle(point2,
				explosion.getVelocity(),
				MathUtils.getRandomNumberInRange(30f, 40f), //size
				1.4f, //endSizeMult
				0f, //rampUpFraction
				0.35f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.15f, 0.3f), //dur
				COLOR_X1);
		
        for (int i = 0; i < 3; i++) {
        	
        	engine.addNebulaParticle(point2,
    				MathUtils.getRandomPointInCircle(explosion.getVelocity(), 19f),
    				MathUtils.getRandomNumberInRange(32f, 60f), //size
    				1.7f, //endSizeMult
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(0.7f, 1.1f), //dur
    				COLOR_X2);
        }
        
        DamagingExplosionSpec blast2 = new DamagingExplosionSpec(0.12f,
                30f,
                20f,
                explosion.getDamageAmount() * 0.4f,
                explosion.getDamageAmount() * 0.2f,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                40,
                COLOR_P,
                COLOR_P);
        blast2.setDamageType(DamageType.FRAGMENTATION);
        blast2.setShowGraphic(false);
        engine.spawnDamagingExplosion(blast2,explosion.getSource(),point2,false);
        
	}
}
