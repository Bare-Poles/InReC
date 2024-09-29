package org.amazigh.InReC.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;
import java.util.List;

public class InReC_guillotineTrailScript extends BaseEveryFrameCombatPlugin {
	
	private DamagingProjectileAPI projectile;
	final IntervalUtil FXTimer = new IntervalUtil(0.05f, 0.05f);
	int fadeVal = 0;
	private Vector2f parentVel;
	
	public InReC_guillotineTrailScript(@NotNull DamagingProjectileAPI projectile) {
		this.projectile = projectile;
		parentVel = new Vector2f(projectile.getSource().getVelocity());
	}
	
	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}
		
		//Checks if our script should be removed from the combat engine
		if (projectile == null || projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
			engine.removePlugin(this);
			return;
		}
		
		// spawn a fancy trail
		FXTimer.advance(amount);
        
        if (FXTimer.intervalElapsed()) {
        	
        	if (projectile.isFading()) {
        		fadeVal += 15;
        	}
        	
        	Vector2f proj_location = projectile.getLocation();
        	float angle = projectile.getFacing();
        	
        	Vector2f smokeVel = MathUtils.getPointOnCircumference(parentVel, MathUtils.getRandomNumberInRange(11f, 26f), angle + MathUtils.getRandomNumberInRange(-2f, 2f));
        	
        	int nebAlpha = Math.max(1, 120 - fadeVal);
        	
        	engine.addNebulaParticle(proj_location,
        			smokeVel,
            		MathUtils.getRandomNumberInRange(29f, 33f),
            		1.9f, //endsizemult
            		0.5f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.5f, 1.7f), //totalDuration
            		new Color(40,50,35,nebAlpha),
            		true);
        	
        	for (int i=0; i < 9; i++) {
        		
        		Vector2f sparkLocation1 = MathUtils.getPointOnCircumference(proj_location, 12f - (i * 7f) + MathUtils.getRandomNumberInRange(-3f, 3f), angle);
        		Vector2f sparkLocation2 = MathUtils.getPointOnCircumference(proj_location, 12f - (i * 7f) + MathUtils.getRandomNumberInRange(-3f, 3f), angle);
        		
        		float sparkAngle1 = angle + MathUtils.getRandomNumberInRange(45, 59);
        		float sparkAngle2 = angle - MathUtils.getRandomNumberInRange(45, 59);
        		
        		sparkLocation1 = MathUtils.getPointOnCircumference(sparkLocation1, 4f, sparkAngle1);
        		sparkLocation2 = MathUtils.getPointOnCircumference(sparkLocation2, 4f, sparkAngle2);
        		
        		Vector2f sparkVel1 = MathUtils.getPointOnCircumference(parentVel, MathUtils.getRandomNumberInRange(9f, 29f), sparkAngle1);
        		Vector2f sparkVel2 = MathUtils.getPointOnCircumference(parentVel, MathUtils.getRandomNumberInRange(9f, 29f), sparkAngle2);
        		
        		int sparkAlpha = (int)Math.max(1, 197 - (fadeVal * 1.6f));
        		
        		engine.addSmoothParticle(sparkLocation1,
        				sparkVel1,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			MathUtils.getRandomNumberInRange(1.05f, 1.35f),
            			new Color(60,220,210,sparkAlpha));
        		engine.addSmoothParticle(sparkLocation2,
        				sparkVel2,
            			MathUtils.getRandomNumberInRange(2f, 3f),
            			1f,
            			MathUtils.getRandomNumberInRange(1.05f, 1.35f),
            			new Color(60,220,210,sparkAlpha));
                
			}
        }
        
		
	}
}