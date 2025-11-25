// Based on the MagicMissileAI script By Tartiflette.
// A somewhat complex swarming missile AI, big weaving, big spread, intended to look cool and to maybe throw off PD fire.
package org.amazigh.InReC.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicTargeting;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_pesticideMissileAI implements MissileAIPlugin, GuidedMissileAI {

    //////////////////////
    //     SETTINGS     //
    //////////////////////

	//Time to complete a wave in seconds.
    private float WAVE_TIME=1.5f;
    
    //Max angle of the waving in degree (0.8 strength with ECCM). Set to a negative value to avoid all waving.
    private final float WAVE_AMPLITUDE = 40f;
    
    // how far off from direct aim the "swarm spread" should be, big numbers can result in extremely *weird* behaviour
    private float SWARM_OFFSET = 49f;
    
    // degree of accuracy that is needed for the missile to progress into the "LOCKED" state
    private final float ARM_ANGLE=15;
    
    // degree of accuracy that is needed for the missile to willingly accelerate when "LOCKED"
    private final float LOCK_ANGLE=45;
    
    // how long to wait before triggering various phases.
    private float PRIME_DELAY=0.3f; // initial no-burn no-turn phase.
    private float LOCK_DELAY=0.1f; // delay after pointing at the target to be sure we're on-target.
    private float SWITCH_DELAY=0.75f; // delay before swapping offset angle to a new one.
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH=true;
    
    //Does the missile find a random target or aways tries to hit the ship's one?    
    /*
     *  NO_RANDOM,
     * If the launching ship has a valid target within arc, the missile will pursue it.
     * If there is no target, it will check for an unselected cursor target within arc.
     * If there is none, it will pursue its closest valid threat within arc.    
     *
     *  LOCAL_RANDOM, 
     * If the ship has a target, the missile will pick a random valid threat around that one. 
     * If the ship has none, the missile will pursue a random valid threat around the cursor, or itself.
     * Can produce strange behavior if used with a limited search cone.
     * 
     *  FULL_RANDOM, 
     * The missile will always seek a random valid threat within arc around itself.
     * 
     *  IGNORE_SOURCE,
     * The missile will pick the closest target of interest. Useful for custom MIRVs.
     * 
    */
    private final MagicTargeting.targetSeeking seeking = MagicTargeting.targetSeeking.NO_RANDOM;
    
    //Target class priorities
    //set to 0 to ignore that class
    private final int fighters=3;
    private final int frigates=5;
    private final int destroyers=4;
    private final int cruisers=2;
    private final int capitals=1;
    
    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE=360;
    
    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 3600;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=400;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Random starting offset for the waving.
    private final float OFFSET;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private Vector2f targetPoint = new Vector2f();
    private boolean launch=true;
    private float primer=0, timer=0, check=0f, arming=0f, swarmBase=0f, swarmTrue=0f;
    private boolean PRIMED = false;
    private boolean LOCKED = false;
    private boolean SWITCH = false;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public InReC_pesticideMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
        
        swarmBase = MathUtils.getRandomNumberInRange(-SWARM_OFFSET, SWARM_OFFSET);
        WAVE_TIME += MathUtils.getRandomNumberInRange(-0.1f, 0.1f); // slight randomisation
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (TARGET_SWITCH 
                        && ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                                  || !engine.isEntityInPlay(target) || (target instanceof ShipAPI && ((ShipAPI) target).isPhased()))
                   )
                ){
        	
        	LOCKED = false; // unlocking the missile, to force the inital drift phase to happen.
        	
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        seeking,
                        MAX_SEARCH_RANGE,
                        SEARCH_CONE,
                        fighters,
                        frigates, 
                        destroyers,
                        cruisers,
                        capitals, 
                        false
                )
            );
            applySwarmOffset();
            
            if (!PRIMED) {
            	primer += amount; // forcing the initial 0.5s drift with no engine burn
            	if (primer > PRIME_DELAY) {
            		PRIMED = true;
            	} else {
                	return;	
            	}
            }
            
            //forced acceleration by default, once the initial timer is over
            if (PRIMED) {
                MISSILE.giveCommand(ShipCommand.ACCELERATE);
            }
            
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
                            MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            
            swarmTrue = swarmBase * ( Math.min(1f, MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) / PRECISION_RANGE) );
            	// scaling down the swarm offset angle when close, to reduce fucky behaviour
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
            		MISSILE.getLocation(),
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
                        MISSILE.getLocation(),
                        lead
                );
        
        float multiplier=1;
        if(ECCM<=1){
        	multiplier=0.8f;
        }
        correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME)); // weave calculation
        
        
        if (SWITCH) {
        	swarmBase = multiplier * MathUtils.getRandomNumberInRange(-SWARM_OFFSET, SWARM_OFFSET); // re-generate swarm offset after the timer proceeds, for an additional level of weave!
        	
        	SWITCH = false;
        	arming = 0f;
        }
        
        
        if (!PRIMED) {
        	primer += amount; // forcing an initial 0.5s drift with no engine burn
        	if (primer > PRIME_DELAY) {
        		PRIMED = true;
        	} else {
            	return;	
        	}
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle) + swarmTrue;
        
        if (LOCKED) {
        	if (Math.abs(aimAngle) < LOCK_ANGLE){
        		MISSILE.giveCommand(ShipCommand.ACCELERATE);
        		arming += amount;
        		if (arming > (SWITCH_DELAY)) {
        			SWITCH = true;
        			SWITCH_DELAY = MathUtils.getRandomNumberInRange(0.7f, 0.8f); // slightly randomize switch delay
        		}
        	}
        } else {
        	if (Math.abs(aimAngle) < ARM_ANGLE){
                // if we're aimed at the target, and not Primed then increment the arming timer and decel.
        		if (arming < LOCK_DELAY) {
        			arming += amount;
        			MISSILE.giveCommand(ShipCommand.DECELERATE);
        		} else {
        			LOCKED = true;
            		MISSILE.giveCommand(ShipCommand.ACCELERATE);
            		arming = amount; // "resetting" the variable so we can re-use the variable for switching
        		}
        	}
        }
        
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
        
    }
    
    //////////////////////
    //    TARGETING     //
    //////////////////////
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    // Taken from Nicke535's homing projectile script, to emulate MISSILE_SPREAD behaviour. 
 	//Used for getting a swarm target point, IE a random point offset on the target. Should only be used when target != null
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
    
    public void init(CombatEngineAPI engine) {}
}