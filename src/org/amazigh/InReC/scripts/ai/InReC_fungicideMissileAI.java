// Based on the MagicMissileAI script By Tartiflette.
// A "flashy" / "advanced" MISSILE_TWO_STAGE_SECOND_UNGUIDED   to sell things short    (this version adds "swarming" behaviour)
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

import java.awt.Color;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_fungicideMissileAI implements MissileAIPlugin, GuidedMissileAI {

    //////////////////////
    //     SETTINGS     //
    //////////////////////

	//Time to complete a wave in seconds.
    private float WAVE_TIME=1.1f;
    
    //Max angle of the waving in degree (0.6 strength with ECCM). Set to a negative value to avoid all waving.
    private final float WAVE_AMPLITUDE=30;
    
    // how far off from direct aim the "swarm spread" should be, big numbers can result in extremely *weird* behaviour
    private float SWARM_OFFSET = 17f;
    
    // degree of accuracy that is needed for the missile to progress into the "ARMED" state
    private final float LOCK_ANGLE=15;

    // how long to wait before starting up the engine after "locking" on to the target. (reduced to 0.69f with ECCM)
    private float DELAY=1.0f;
    
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
    private final int fighters=0;
    private final int frigates=1;
    private final int destroyers=2;
    private final int cruisers=3;
    private final int capitals=4;
    
    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE_START=360;
    private final int SEARCH_CONE_ARMED=90;
    
    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 3600;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=400;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=2;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    
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
    private float timer=0, check=0f, arming=0f, swarmBase=0f, swarmTrue=0f;
    private boolean ARMED = false;
    private boolean FIRED = false;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public InReC_fungicideMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
            DELAY = 0.69f;
        }        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
        
        swarmBase = MathUtils.getRandomNumberInRange(-SWARM_OFFSET, SWARM_OFFSET);
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
        	
        	int SEARCH_CONE = SEARCH_CONE_START;
        	if (ARMED) {SEARCH_CONE = SEARCH_CONE_ARMED;}
        	
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

            //forced deceleration/acceleration by default
            if (ARMED) {
                MISSILE.giveCommand(ShipCommand.ACCELERATE);
            } else {
                MISSILE.giveCommand(ShipCommand.DECELERATE);
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
            if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) < PRECISION_RANGE) {
            	swarmTrue *= 0.5f;
            }
            
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
        
        if(WAVE_AMPLITUDE>0){            
            //waving
            float multiplier=1;
            if(ECCM<=1){
                multiplier=0.6f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        }
        
        if (!ARMED) {
        	swarmTrue = -swarmBase;
        	// forcing inverted spread before starting acceleration, to trick it into doing a bit of weaving, and guaranteeing at least some spread on engine start.
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle) + swarmTrue;;
        
        // if armed, then accelerate/ do the initial boost
        if (ARMED) {
    		MISSILE.giveCommand(ShipCommand.ACCELERATE);
        	if (!FIRED) {
    			FIRED = true;
    			
    			// impulse
    			MISSILE.getVelocity().set(MathUtils.getPointOnCircumference(null, 800f, MISSILE.getFacing()));
    			
    			Vector2f loc = (Vector2f) (MISSILE.getLocation());
    			for (int i=0; i < 5; i++) {
                	// left sparks
                	Vector2f sparkRandomVelL = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(19f, 49f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-119f, -61f));
                	engine.addSmoothParticle(loc,
                			sparkRandomVelL,
                			MathUtils.getRandomNumberInRange(3f, 6f), //size
                			1.0f, //brightness
                			0.8f, //duration
                			new Color(70,123,130,225));
                	// right sparks
                	Vector2f sparkRandomVelR = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(19f, 49f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(119f, 61f));
                	engine.addSmoothParticle(loc,
                			sparkRandomVelR,
                			MathUtils.getRandomNumberInRange(3f, 6f), //size
                			1.0f, //brightness
                			0.8f, //duration
                			new Color(70,123,130,225));
                	// back sparks
                	Vector2f sparkRandomVelB = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(36f, 73f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(170f, 190f));
                	engine.addSmoothParticle(loc,
                			sparkRandomVelB,
                			MathUtils.getRandomNumberInRange(3f, 7f), //size
                			1.0f, //brightness
                			0.9f, //duration
                			new Color(70,123,130,255));
    			}
    			
    		}
    		
        } else {
    		MISSILE.giveCommand(ShipCommand.DECELERATE);
    		
            if (Math.abs(aimAngle) < LOCK_ANGLE){
                // if we're aimed at the target then increment the arming timer.
        		if (arming < DELAY) {
        			arming += amount;
        		} else {
            		ARMED = true;
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
 		int i = 20; //We don't want to take too much time, even if we get unlucky: only try 20 times
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

 		//If we didn't find a point in 20 tries, just choose target center
 		if (!success) {
 			targetPoint = new Vector2f(Misc.ZERO);
 		}
 	}
    
    public void init(CombatEngineAPI engine) {}
}