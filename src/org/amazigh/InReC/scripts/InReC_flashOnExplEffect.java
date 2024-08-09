package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class InReC_flashOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(217,96,243,240);
	private static final Color COLOR_X = new Color(243,113,217,56); // 85 alpha
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
		
        for (int i = 0; i < 2; i++) {
    		engine.addSwirlyNebulaParticle(point,
    				explosion.getVelocity(),
    				10f + (explosion.getCollisionRadius() * 0.95f),
    				1.65f,
    				0.6f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.9f, 1.4f),
    				new Color(65,140,150,75),
    				true);
        }
		
		float angle1 = MathUtils.getRandomNumberInRange(0f, 120f);
		
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                explosion.getCollisionRadius() * 2.2f,
                0.8f,
                0.1f,
                COLOR_P);
		
        for (int i = 0; i < 3; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 13f),
    				6f + (explosion.getCollisionRadius() * 0.95f), //size
    				1.85f, //endSizeMult  1.75
    				0.1f, //rampUpFraction
    				0.4f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1.1f, 1.6f), //dur
    				COLOR_X);
        	
        	float angle2 = (i * 120f) + angle1 + MathUtils.getRandomNumberInRange(0f, 30f);
        	
        	engine.addSwirlyNebulaParticle(MathUtils.getPointOnCircumference(point, explosion.getCollisionRadius() * MathUtils.getRandomNumberInRange(0.3f, 0.5f), angle2),
    				explosion.getVelocity(),
    				3f + (explosion.getCollisionRadius() * 0.7f),
    				1.5f,
    				0.4f,
    				0.2f,
    				MathUtils.getRandomNumberInRange(0.7f, 1.15f),
    				new Color(60,150,160,75),
    				true);
        }
	}
}
