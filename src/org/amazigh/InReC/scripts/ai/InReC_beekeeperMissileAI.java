// Based on the MagicMissileAI script By Tartiflette.
// This is a "weird" MIRV script, more or less.
package org.amazigh.InReC.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicTargeting;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_beekeeperMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
	//////////////////////
	//     SETTINGS     //
	//////////////////////
	
	//Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
	//  Set to a negative value to disable
	private final float OVERSHOT_ANGLE=60;
	
	//Time to complete a wave in seconds.
	private final float WAVE_TIME=2;
	
	//Max angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
	private final float WAVE_AMPLITUDE=15;
	
	//Damping of the turn speed when closing on the desired aim. The smaller the snappier.
	private final float DAMPING=0.1f;
	
	//Does the missile switch its target if it has been destroyed?
	private final boolean TARGET_SWITCH=true;

	//Target class priorities
	//set to 0 to ignore that class
	private final int fighters=5;
	private final int frigates=4;
	private final int destroyers=3;
	private final int cruisers=2;
	private final int capitals=1;

	//Arc to look for targets into
	//set to 360 or more to ignore
	private final int SEARCH_CONE=360;

	//range in which the missile seek a target in game units.
	private final int MAX_SEARCH_RANGE = 1500;

	//should the missile fall back to the closest enemy when no target is found within the search parameters
	//only used with limited search cones
	private final boolean FAILSAFE = false;

	//range under which the missile start to get progressively more precise in game units.
	private float PRECISION_RANGE=800;

	//Is the missile lead the target or tailchase it?
	private final boolean LEADING=true;

	//Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
	//   1: perfect leading with and without ECCM
	//   2: half precision without ECCM
	//   3: a third as precise without ECCM. Default
	//   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
	private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
	
	
	
	
	// how many shots to fire
	private int AMMO = 40;
	
	// what side to fire from, as we swap between shots
	private boolean SIDE = false;
	
	// to check whether the missile is within "MIRV" range, and whether to fire the missiles
	private boolean TRIGGER = false;
	
	// square of range to start to fire missiles at
	private float DETECT=810000; //900^2
	
	// timer for firing missiles after getting into range
	private IntervalUtil missileInterval = new IntervalUtil(0.05f, 0.05f);
	
	
	
	
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
	private boolean launch=true;
	private float timer=0, check=0f;
	
	//////////////////////
	//  DATA COLLECTING //
	//////////////////////
	
    public InReC_beekeeperMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
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
                                  || !engine.isEntityInPlay(target))
                   )
                ){
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        MagicTargeting.targetSeeking.NO_RANDOM,
                        MAX_SEARCH_RANGE,
                        SEARCH_CONE,
                        fighters,
                        frigates, 
                        destroyers,
                        cruisers,
                        capitals, 
                        FAILSAFE
                )
            );
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
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
            if(LEADING){
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
            } else {
                lead = target.getLocation();
            }
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
                multiplier=0.3f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        if(Math.abs(aimAngle)<OVERSHOT_ANGLE){
        	if (TRIGGER) {
                MISSILE.giveCommand(ShipCommand.DECELERATE);
        	} else {
                MISSILE.giveCommand(ShipCommand.ACCELERATE);
        	}
            
            if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) <= DETECT) {
            	TRIGGER = true;
            }
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        
        if (TRIGGER) {
            missileInterval.advance(amount);
            
        	if (missileInterval.intervalElapsed()) {
            	
        		AMMO --;
        		
            	Vector2f vel = MISSILE.getVelocity();
            	Vector2f loc = MISSILE.getLocation();

				Vector2f smokeVel = new Vector2f(vel.x * 0.8f, vel.y * 0.8f);
				
				float angle = MathUtils.getRandomNumberInRange((40-AMMO) * 0.7f, (40-AMMO) * 1.1f);
				
				if (SIDE) {
					SIDE = false;
					float subFacing = MISSILE.getFacing() + angle;
					
					engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "InReC_beekeeper_sub", loc, subFacing + MathUtils.getRandomNumberInRange(0f, 5f), MathUtils.getRandomPointInCircle(smokeVel, 15f));

					Vector2f puffRandomVel = MathUtils.getPointOnCircumference(smokeVel, MathUtils.getRandomNumberInRange(4f, 12f), subFacing);
                	engine.addSmokeParticle(loc,
                			puffRandomVel,
                			MathUtils.getRandomNumberInRange(7f, 14f),
                			0.8f,
                			0.6f,
                			new Color(90,100,110,150));
					
				} else {
					SIDE = true;
					float subFacing = MISSILE.getFacing() - angle;
					
					engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "InReC_beekeeper_sub", loc, subFacing - MathUtils.getRandomNumberInRange(0f, 5f), MathUtils.getRandomPointInCircle(smokeVel, 15f));
					
					Vector2f puffRandomVel = MathUtils.getPointOnCircumference(smokeVel, MathUtils.getRandomNumberInRange(4f, 12f), subFacing);
                	engine.addSmokeParticle(loc,
                			puffRandomVel,
                			MathUtils.getRandomNumberInRange(7f, 14f),
                			0.8f,
                			0.6f,
                			new Color(90,100,110,150));
				}
				
            	Global.getSoundPlayer().playSound("swarmer_fire", 1.0f, 1.0f, loc, vel);
            	
            	if (AMMO <= 0) {
            		MISSILE.flameOut();
            	}
    		}
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
    
    public void init(CombatEngineAPI engine) {}
}