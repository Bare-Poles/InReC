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
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class InReC_flashProjScript extends BaseEveryFrameCombatPlugin {
	//---Settings: adjust to fill the needs of your implementation---
	
	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE = 28f; //40
	
	//If non-zero, the projectile will sway back-and-forth by this many degrees during its guidance (with a sway period determined by SWAY_PERIOD).
    //High values, as one might expect, give very poor tracking. Also, high values will decrease effective range (as the projectiles travel further) so be careful
    //Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
    private static final float SWAY_AMOUNT_PRIMARY = 5f; //3
    private static final float SWAY_AMOUNT_SECONDARY = 2f; //1.5

    //Used together with SWAY_AMOUNT, determines how fast the swaying happens
    //1f means an entire sway "loop" (max sway right -> min sway -> max sway left -> min sway again) per second, 2f means 2 loops etc.
    //Projectiles start at a random position in their sway loop on spawning
    //Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
    private static final float SWAY_PERIOD_PRIMARY = 1.0f;
    private static final float SWAY_PERIOD_SECONDARY = 1.9f;

    //How fast, if at all, sway falls off with the projectile's lifetime.
    //At 1f, it's a linear falloff, at 2f it's quadratic. At 0f, there is no falloff
    private static final float SWAY_FALLOFF_FACTOR = 0.5f;
    
	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
	private float swayCounter1; // Counter for handling primary sway
    private float swayCounter2; // Counter for handling secondary sway
    private float lifeCounter; // Keeps track of projectile lifetime
    private final float estimateMaxLife; // How long we estimate this projectile should be alive
	private Vector2f offsetVelocity; // Only used for ONE_TURN_DUMB: keeps velocity from the ship and velocity from the projectile separate (messes up calculations otherwise)
	
	//The actual target angle is randomly offset by this much, to simulate inaccuracy
	//2f means up to 2 degrees angle off from the actual target angle
	private float ONE_TURN_DUMB_INACCURACY = 21f;
	// the value we set here doesn't matter, as it is changed to be 70% of the weapons current spread later on 
	
	/**
	 * Initializer for the guided projectile script
	 *
	 * @param proj
	 * The projectile to affect. proj.getWeapon() must be non-null.
	 */
	public InReC_flashProjScript(@NotNull DamagingProjectileAPI proj) {
		this.proj = proj;
		
		swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
        swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        lifeCounter = 0f;
        estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
		
        ONE_TURN_DUMB_INACCURACY = proj.getWeapon().getCurrSpread() * 0.7f;
        
		//For one-turns, we set our target point ONCE and never adjust it
		targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
		
		
		
		offsetVelocity = proj.getSource().getVelocity();
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
		
		swayCounter1 += amount * SWAY_PERIOD_PRIMARY;
        swayCounter2 += amount * SWAY_PERIOD_SECONDARY;
        float swayThisFrame = (float) Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
                ((float) (FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float) (FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
		
		//Start our guidance stuff...
		//A Dumb one-turn that turns toward an angle, and weaves, also needs to compensate for offset velocity to remain straight
		float facingSwayless = proj.getFacing() - swayThisFrame;
        float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
		while (angleDiffAbsolute > 180f) {
			angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);
		}
		facingSwayless += Misc.getClosestTurnDirection(facingSwayless, targetAngle) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
		Vector2f pureVelocity = new Vector2f(proj.getVelocity());
		pureVelocity.x -= offsetVelocity.x;
		pureVelocity.y -= offsetVelocity.y;
		proj.setFacing(facingSwayless + swayThisFrame);
        proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless + swayThisFrame).x + offsetVelocity.x;
        proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless + swayThisFrame).y + offsetVelocity.y;
	}

}