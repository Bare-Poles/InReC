package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class InReC_flashOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(217,96,243,220);
	private static final Color COLOR_X = new Color(243,113,217,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
		
		engine.addSwirlyNebulaParticle(point,
				explosion.getVelocity(),
				explosion.getCollisionRadius() * 0.3f,
				1.5f,
				0.6f,
				0.5f,
				MathUtils.getRandomNumberInRange(0.4f, 0.9f),
				new Color(140,65,150,75),
				true);
		
		float angle1 = MathUtils.getRandomNumberInRange(0f, 120f);
		
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                64f,
                0.8f,
                0.1f,
                COLOR_P);
		
        for (int i = 0; i < 3; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 13f), // 11
    				MathUtils.getRandomNumberInRange(23f, 41f), //size  23,61
    				1.85f, //endSizeMult  1.75
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.6f), //dur
    				COLOR_X);
        	
        	float angle2 = (i * 120f) + angle1 + MathUtils.getRandomNumberInRange(0f, 30f);
        	
            for (int j = 0; j < 7; j++) {
            	
            	Global.getCombatEngine().addSmoothParticle(MathUtils.getPointOnCircumference(point, j, angle2 + MathUtils.getRandomNumberInRange(-1f, 1f)),
            			MathUtils.getPointOnCircumference(null, j * 3f, angle2 + MathUtils.getRandomNumberInRange(-1f, 1f)),
            			7f - (j * 0.5f), //size
            			1.0f, //brightness
            			0.6f + (j * 0.1f), //duration
            			COLOR_P);
            	
            }
        }
	}
}



