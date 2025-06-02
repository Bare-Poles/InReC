package org.amazigh.InReC.scripts;

import java.awt.Color;
import java.util.List;

import org.amazigh.InReC.scripts.InReC_ModPlugin.INREC_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class InReC_guillotineOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(251,130,30,255);
	private static final Color COLOR_X = new Color(173,97,25,255);
	private static final Color COLOR_U = new Color(16,169,91,200); // 169,91,16,200
	private static final Color COLOR_D_C = new Color(145,165,155,255);
	private static final Color COLOR_D_F = new Color(201,107,30,255);
	
//    private static final Color SPARK_COLOR = new Color(60,220,210,235);
	
	public void onHit(final DamagingProjectileAPI projectile, CombatEntityAPI target,
					  final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                150f,
                110f,
                projectile.getDamageAmount(),
                projectile.getDamageAmount() * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                4f,
                0.8f,
                269,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(true);
        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
        blast.setUseDetailedExplosion(true);
        blast.setDetailedExplosionRadius(179f);
        blast.setDetailedExplosionFlashRadius(369f);
        blast.setDetailedExplosionFlashDuration(0.5f);
        
        DamagingProjectileAPI expl = engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        expl.addDamagedAlready(target);
        
        Vector2f fxVel = new Vector2f(); // stationary explosion, no target vel inheritance as i normally would, because the subs are stationary
		
        // "background" nebs
    	for (int i=0; i < 6; i++) {
    		engine.addSwirlyNebulaParticle(MathUtils.getRandomPointInCircle(point, 38f),
    				MathUtils.getRandomPointInCircle(fxVel, 5f),
    				94f,
    				1.9f,
    				0.3f,
    				0.69f,
    				MathUtils.getRandomNumberInRange(3.1f, 3.6f), //1.6,2.1
    				new Color(50,55,45,85),
    				true);
    	}
    	
    	
    	// smoke ring
    	for (int i=0; i < 36; i++) {
    		Vector2f smokePos = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(62f, 75f), i * 10f);
    		
    		Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(59f, 77f), i * 10f);
    		
    		engine.addNebulaParticle(smokePos,
    				smokeVel,
    				MathUtils.getRandomNumberInRange(47f, 53f), //size
    				1.56f, //endSizeMult
    				0.45f, //rampUpFraction
    				0.36f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1.5f, 1.9f), //dur
    				new Color(50,55,45,96));
    	}
    	
    	
    	// extra sparks
    	INREC_RadialEmitter emitterSparks = new INREC_RadialEmitter(null);
    	emitterSparks.location(point);
    	emitterSparks.angle(0);
    	emitterSparks.arc(360f);
    	emitterSparks.life(1.8f, 2.3f);
    	emitterSparks.size(3f, 5f);
        emitterSparks.velocity(22f, 12f);
        emitterSparks.distance(7f, 84.5f);
        emitterSparks.color(60,220,210,235); // SPARK_COLOR
        emitterSparks.velDistLinkage(false);
        emitterSparks.burst(169);
		
        engine.spawnExplosion(point, fxVel, COLOR_U, 189f, 1.1f);
        
		engine.addHitParticle(point, fxVel, 369f, 1f, 0.1f, COLOR_U);
        
		Global.getSoundPlayer().playSound("InReC_guillotine_explosion", 1.0f, 1.0f, point, fxVel);
		
		//TODO
			// more vfx!?
		
		
		// hi! thanks VIC, i stole code again lol!
		engine.addPlugin(new EveryFrameCombatPlugin() {
			
            final int blastCount = 5;
            
            int blastsFired = 0;
            float blastDamage = projectile.getDamageAmount() * 0.2f;
             
            
            final Vector2f mainLoc = new Vector2f(point.x, point.y);
            final Vector2f blastVel = new Vector2f(0f, 0f);
            final IntervalUtil blastTimer = new IntervalUtil(0.18f, 0.25f);
            boolean init = false;
            final IntervalUtil initTimer = new IntervalUtil(0.43f, 0.5f);
            
            float blastAngle = MathUtils.getRandomNumberInRange(0f, 360f); 
            
            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
            	
            }
            
            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (engine.isPaused()) return;
                
                if (!init) {
                    initTimer.advance(amount);
                } else {
                	blastTimer.advance(amount);
                }
                
                if (initTimer.intervalElapsed()) {
                	init = true;
                }
                
                if (blastTimer.intervalElapsed()) {
                	
                	blastAngle += 144f;
                	Vector2f blastLoc = MathUtils.getPointOnCircumference(mainLoc, MathUtils.getRandomNumberInRange(27f, 89f), blastAngle + MathUtils.getRandomNumberInRange(0f, 21f));
                	
                	DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                            90f,
                            60f,
                            blastDamage,
                            blastDamage * 0.7f,
                            CollisionClass.PROJECTILE_FF,
                            CollisionClass.PROJECTILE_FIGHTER,
                            2f,
                            3f,
                            1f,
                            69,
                            new Color(60,220,210,225),
                            new Color(35,173,137,255));
                    blast.setDamageType(DamageType.FRAGMENTATION);
                    blast.setShowGraphic(true);
                    blast.setDetailedExplosionFlashColorCore(new Color(155,155,155,255));
                    blast.setDetailedExplosionFlashColorFringe(new Color(181,176,51,255));
                    blast.setUseDetailedExplosion(true);
                    blast.setDetailedExplosionRadius(93f);
                    blast.setDetailedExplosionFlashRadius(269f);
                    blast.setDetailedExplosionFlashDuration(0.45f);
                    
                    engine.spawnDamagingExplosion(blast,projectile.getSource(),blastLoc,false);
                	
                    blastsFired ++;
                    
            		Global.getSoundPlayer().playSound("InReC_guillotine_sub_explosion", 1.0f, 1.0f, blastLoc, blastVel);
                    
                    for (int i=0; i < 2; i++) {
                		engine.addSwirlyNebulaParticle(MathUtils.getRandomPointInCircle(blastLoc, 19f),
                				MathUtils.getRandomPointInCircle(blastVel, 5f),
                				79f,
                				1.8f,
                				0.3f,
                				0.45f,
                				MathUtils.getRandomNumberInRange(1.25f, 1.75f),
                				new Color(91,69,35,85),
                				true);
                	}
                    
                	INREC_RadialEmitter emitterSpark = new INREC_RadialEmitter(null);
                	emitterSpark.location(blastLoc);
                	emitterSpark.angle(0);
                	emitterSpark.arc(360f);
                	emitterSpark.life(0.9f, 1.15f);
                	emitterSpark.size(3f, 5f);
                    emitterSpark.velocity(14f, 10f);
                    emitterSpark.distance(7f, 20.5f);
                    emitterSpark.color(251,130,30,255);
                    emitterSpark.velDistLinkage(false);
                    emitterSpark.burst(41);
                }
                
                if (blastsFired >= blastCount) engine.removePlugin(this);
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
