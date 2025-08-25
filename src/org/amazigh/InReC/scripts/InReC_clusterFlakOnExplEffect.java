package org.amazigh.InReC.scripts;

import java.awt.Color;
import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class InReC_clusterFlakOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_X1 = new Color(215,250,123,80);
	private static final Color COLOR_X2 = new Color(145,175,130,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		final CombatEngineAPI engine = Global.getCombatEngine();
		
		engine.addNebulaParticle(explosion.getLocation(),
				explosion.getVelocity(),
				MathUtils.getRandomNumberInRange(30f, 40f), //size
				1.4f, //endSizeMult
				0.05f, //rampUpFraction
				0.69f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.25f, 0.34f), //dur
				COLOR_X1);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(explosion.getLocation(),
    				MathUtils.getRandomPointInCircle(explosion.getVelocity(), 19f),
    				MathUtils.getRandomNumberInRange(32f, 60f), //size
    				1.7f, //endSizeMult
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(0.7f, 1.1f), //dur
    				COLOR_X2);
        }

        INREC_RadialEmitter emitterSparks = new INREC_RadialEmitter(null);
    	emitterSparks.location(explosion.getLocation());
    	emitterSparks.life(0.45f, 0.5f);
    	emitterSparks.size(3f, 6f);
        emitterSparks.velocity(35f, 69f);
        emitterSparks.distance(1f, 20f);
        emitterSparks.color(219,255,125,255);
        // emitterSparks.velDistLinkage(false);
        emitterSparks.burst(45);
	}
}
