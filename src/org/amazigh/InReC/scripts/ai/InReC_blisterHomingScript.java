package org.amazigh.InReC.scripts.ai;

// A simple (missile spread) custom missile AI, done as an everyframe, idk it was the easiest way i could think of to pass missile target from a mirv.

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;

import java.util.List;

public class InReC_blisterHomingScript extends BaseEveryFrameCombatPlugin {
	
	private MissileAPI projectile;
	private CombatEntityAPI target;
    private Vector2f targetPoint = new Vector2f();
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, ECCM=2, MAX_SPEED=300, swarmBase=0f, swarmTrue=0f;
    private float SWARM_OFFSET = 21f;
    private float PRECISION_RANGE=640000; // 800^2
    
    private boolean init=false;
    private IntervalUtil delayInterval = new IntervalUtil(0.1f, 0.1f);
    		
	public InReC_blisterHomingScript(@NotNull MissileAPI projectile, CombatEntityAPI target, float angle, float delay) {
		this.projectile = projectile;
        this.target = target;
        
        delayInterval = new IntervalUtil(delay, delay);
        
		MAX_SPEED = projectile.getMaxSpeed();
		
		if (angle > 0) {
        	swarmBase = (angle * 0.5f) + MathUtils.getRandomNumberInRange(0, SWARM_OFFSET);
        } else {
        	swarmBase = (angle * 0.5f) - MathUtils.getRandomNumberInRange(0, SWARM_OFFSET);
        }
		
        if (projectile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
            swarmBase *= 0.8f;
        }
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
				
		if (Global.getCombatEngine().isPaused() || projectile.isFading() || projectile.isFizzling()) {return;}
		
		if (!init) {
			delayInterval.advance(amount);
			projectile.giveCommand(ShipCommand.DECELERATE);
			if (delayInterval.intervalElapsed()) {
				init = true;
			} else {
				return;				
			}
		}
		
		if (target == null
				|| ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                		|| !engine.isEntityInPlay(target) || (target instanceof ShipAPI && ((ShipAPI) target).isPhased()))
				){
			target = MagicTargeting.pickTarget(
					projectile,
					MagicTargeting.targetSeeking.IGNORE_SOURCE,
					3000,	// MAX_SEARCH_RANGE
					360,
					1,
					5, 
					4,
					3,
					2, 
					false);
			
			applySwarmOffset();
            //forced acceleration if no target
            projectile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
		
		timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(projectile.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            
            swarmTrue = swarmBase * MathUtils.getRandomNumberInRange(0.6f, 1.2f)*(Math.min(1f, MathUtils.getDistanceSquared(projectile.getLocation(), target.getLocation()) / PRECISION_RANGE));
            	// swarm offset is randomly 0.6-1.2x the base generated offset, and also scales down as the missile closes with the target (under 800 SU)
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
            		projectile.getLocation(),
            		MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
            		target.getLocation(),
            		target.getVelocity()
            		);
            //null pointer protection
            if (lead == null) {
            	lead = target.getLocation(); 
            }
            
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(lead, targetPointRotated, lead);
            
        }
		
      //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
        		projectile.getLocation(),
        		lead);
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( projectile.getFacing(), correctAngle) + swarmTrue;
        
        // accel if within 15 degrees of aimed at target, decel if over 50 degrees off.
        if (Math.abs(aimAngle) < 15){
    		projectile.giveCommand(ShipCommand.ACCELERATE);
    	} else if (Math.abs(aimAngle) > 50){
			projectile.giveCommand(ShipCommand.DECELERATE);
    	}
        
        if (aimAngle < 0) {
        	projectile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
        	projectile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(projectile.getAngularVelocity()) * 0.1f) {
        	projectile.setAngularVelocity(aimAngle / 0.1f);
        }		
		   
	}
	
	private void applySwarmOffset() {
 		int i = 13; //We don't want to take too much time, even if we get unlucky: only try 13 times
 		boolean success = false;
 		while (i > 0 && target != null) {
 			i--;

 			//Get a random position and check if its valid
 			Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
 			if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
 				//If the point is valid, convert it to an offset and store it
 				potPoint.x -= target.getLocation().x;
 				potPoint.y -= target.getLocation().y;
 				potPoint = VectorUtils.rotate(potPoint, -target.getFacing());
 				targetPoint = new Vector2f(potPoint);
 				success = true;
 				break;
 			}
 		}

 		//If we didn't find a point in 13 tries, just choose target center
 		if (!success) {
 			targetPoint = new Vector2f(Misc.ZERO);
 		}
 	}
	
}