//By Nicke535, licensed under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
// massively trimmed (and tweaked) to be just a (weaving) one-turn-dumb
package org.amazigh.InReC.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class InReC_gluonProjScript extends BaseEveryFrameCombatPlugin {
	//---Settings: adjust to fill the needs of your implementation---
	
	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE = 320f; //360
	
	//The actual target angle is randomly offset by this much, to simulate inaccuracy
	//2f means up to 2 degrees angle off from the actual target angle
	private static final float ONE_TURN_DUMB_INACCURACY = 3f; //9
	
	//Delays the activation of the script by a random amount of seconds between this MIN and MAX.
	//Note that shots will still decide on target angle/point at spawn-time, not when this duration is up
	private static final float GUIDANCE_DELAY_MAX = 0.15f;
	private static final float GUIDANCE_DELAY_MIN = 0.05f;

	//If non-zero, the projectile will sway back-and-forth by this many degrees during its guidance (with a sway period determined by SWAY_PERIOD).
    //High values, as one might expect, give very poor tracking. Also, high values will decrease effective range (as the projectiles travel further) so be careful
    //Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
    private static final float SWAY_AMOUNT_PRIMARY = 6f;
    private static final float SWAY_AMOUNT_SECONDARY = 13f;

    //Used together with SWAY_AMOUNT, determines how fast the swaying happens
    //1f means an entire sway "loop" (max sway right -> min sway -> max sway left -> min sway again) per second, 2f means 2 loops etc.
    //Projectiles start at a random position in their sway loop on spawning
    //Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
    private static final float SWAY_PERIOD_PRIMARY = 1.1f;
    private static final float SWAY_PERIOD_SECONDARY = 2.3f;

    //How fast, if at all, sway falls off with the projectile's lifetime.
    //At 1f, it's a linear falloff, at 2f it's quadratic. At 0f, there is no falloff
    private static final float SWAY_FALLOFF_FACTOR = 0.4f;
    
	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
	private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float lifeCounter; // Keeps track of projectile lifetime
    private final float estimateMaxLife; // How long we estimate this projectile should be alive
	private float delayCounter; // Counter for delaying targeting
	private Vector2f offsetVelocity; // Keeps velocity from the ship and velocity from the projectile separate (messes up calculations otherwise)
	private float baseVelocity; // the core projectile velocity, needed for convergence calc 
	private Vector2f firePoint; // Where the projectile originated from, used to "converge" the two streams 
	private float actualGuidanceDelay; // The actual guidance delay for this specific projectile
	

//	private IntervalUtil intervalTEST = new IntervalUtil(0.05f,0.05f);
	

	/**
	 * Initializer for the guided projectile script
	 *
	 * @param proj
	 * The projectile to affect. proj.getWeapon() must be non-null.
	 *
	 * @param target
	 * The target missile/asteroid/ship for the script's guidance.
	 * Can be null, if the script does not follow a target ("ONE_TURN_DUMB") or to instantly activate secondary guidance mode.
	 */
	public InReC_gluonProjScript(@NotNull DamagingProjectileAPI proj) {
		this.proj = proj;
		
		swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
        swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        lifeCounter = 0f;
        estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
		
		delayCounter = 0f;
		actualGuidanceDelay = MathUtils.getRandomNumberInRange(GUIDANCE_DELAY_MIN, GUIDANCE_DELAY_MAX);
		
		//For one-turns, we set our target point ONCE and never adjust it
		targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
		offsetVelocity = proj.getSource().getVelocity();
		baseVelocity = proj.getWeapon().getProjectileSpeed() + 50f; // boosting it to get around some edge-case issues
		firePoint = proj.getWeapon().getLocation();
	}


	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}
		
		//Checks if our script should be removed from the combat engine
		if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}
		
		lifeCounter += amount;
        if (lifeCounter > estimateMaxLife) {
            lifeCounter = estimateMaxLife;
        }
        
		//Delays targeting if we have that enabled
		if (delayCounter < actualGuidanceDelay) {
			delayCounter+=amount;
			return;
		}
		
		
		swayCounter1 += amount * SWAY_PERIOD_PRIMARY;
        swayCounter2 += amount * SWAY_PERIOD_SECONDARY;
        float lifeMult = lifeCounter / estimateMaxLife;
        
        float swayThisFrame = (float) Math.pow(1f - lifeMult, SWAY_FALLOFF_FACTOR) *
                ((float) (FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float) (FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
		
		//Start our guidance stuff...
		//A Dumb one-turn that turns toward an angle, and weaves, also needs to compensate for offset velocity to remain straight
        	// with an added feature, where the projectile aims at a point that moves forwards, with the intent of converging the "split streams" that the weapon would otherwise produce 
		float facingSwayless = proj.getFacing() - swayThisFrame;
        
			// float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
		
		// some fucked up evil shit to compensate for the velocity of the firing ship, i think i have to do it this way because of how fire angle is different to aim angle 
		Vector2f trackPoint = MathUtils.getPointOnCircumference(firePoint, 50f + (lifeCounter * baseVelocity), targetAngle);
		trackPoint.setX(trackPoint.x + (offsetVelocity.x * lifeMult));
		trackPoint.setY(trackPoint.y + (offsetVelocity.y * lifeMult)); // use of lifeMult here is so that you don't get Fucked Up Bullshit when flying backwards.
		
		//TODO TEMP - debug rendering of aimpoint stuff
//		intervalTEST.advance(amount);
//		if (intervalTEST.intervalElapsed()) {
//			Vector2f fxVel = new Vector2f();
//			
//			Global.getCombatEngine().addSmoothParticle(trackPoint,
//					fxVel,
//	    			5f,
//	    			1f,
//	    			0.1f,
//	    			new Color(255,255,255,200));
//		}
		//TODO TEMP
		
		float angleToHit = VectorUtils.getAngle(proj.getLocation(), trackPoint);
		float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
		
		while (angleDiffAbsolute > 180f) {
			angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);
		}
			// facingSwayless += Misc.getClosestTurnDirection(facingSwayless, targetAngle) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
		facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
		Vector2f pureVelocity = new Vector2f(proj.getVelocity());
		pureVelocity.x -= offsetVelocity.x;
		pureVelocity.y -= offsetVelocity.y;
		proj.setFacing(facingSwayless + swayThisFrame);
        proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless + swayThisFrame).x + offsetVelocity.x;
        proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless + swayThisFrame).y + offsetVelocity.y;
	}

}