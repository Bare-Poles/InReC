package org.amazigh.InReC.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_MicroBurstDriveStats extends BaseShipSystemScript {
	
	private float smokeCount = 3f;
	private float fxDelay = 0.2f;
	private boolean boosted = false;
    private static final String failstate = "ENGINES OFFLINE, CANNOT BOOST";
    
    public static final float SPEED_BONUS = 200f;
    public static final float ACCEL_BONUS = 180f;
    public static final float DECEL_BONUS = 70f;
    public static final float TURN_BONUS = 60f;
    public static final float TURN_ACCEL_BONUS = 180f;
    
    public static final float TIME_MULT = 0.6f; // add one to get the actual timescale multiplier
	public static final float DAMAGE_REDUCTION = 0.15f;
	
	 // Boosted RoF for energy weps while they are disabled, to reload them (so it's also a sort of energy FMR as well as mobility)
	public static final float RELOAD_MULT = 8.5f; //  ~4.0375s cut from reload times
	
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
    	
    	ShipAPI ship0 = (ShipAPI)stats.getEntity();
    	CombatEngineAPI engine = Global.getCombatEngine();
    	float timer = engine.getElapsedInLastFrame() * effectLevel;
    	float timerFlat = engine.getElapsedInLastFrame();
    	
    	// first we modify ship stats
    	stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
    	stats.getAcceleration().modifyPercent(id, ACCEL_BONUS * effectLevel);
    	stats.getDeceleration().modifyPercent(id, DECEL_BONUS * effectLevel);
    	stats.getMaxTurnRate().modifyPercent(id, TURN_BONUS * effectLevel);
    	stats.getTurnAcceleration().modifyPercent(id, TURN_ACCEL_BONUS * effectLevel);
    	
    	stats.getHullDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
			// up to 15% damage resist while system is active :)
    	
    	// timescale!!
    	boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship0 = (ShipAPI) stats.getEntity();
			player = ship0 == Global.getCombatEngine().getPlayerShip();
		}
		
    	float shipTimeMult = 1f + (TIME_MULT * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
    	
		if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
			stats.getEnergyRoFMult().modifyMult(id, 1f + (RELOAD_MULT * effectLevel));
		} else {
			stats.getEnergyRoFMult().unmodify(id);
		}
		
    	
        // then we BOOST
        if (!boosted && state == ShipSystemStatsScript.State.ACTIVE) {
        	Vector2f direction = new Vector2f();
        	if (ship0.getEngineController().isAccelerating()) {
        		direction.y += 0.8f;
        	} else if (ship0.getEngineController().isAcceleratingBackwards() || ship0.getEngineController().isDecelerating()) {
        		direction.y -= 0.6f; // 0.2 weaker when in reverse
        	}
        	if (ship0.getEngineController().isStrafingLeft()) {
        		direction.x -= 0.9f; // strafing is stronger
        	} else if (ship0.getEngineController().isStrafingRight()) {
        		direction.x += 0.9f; // strafing is stronger
        	}
        	if (direction.length() <= 0f) {
        		direction.y = 0.6f; // if no input, then you get a weak forwards dash, because lol
        	}
        	Misc.normalise(direction);
        	VectorUtils.rotate(direction, ship0.getFacing() - 90f, direction);
        	direction.scale(ship0.getMaxSpeedWithoutBoost() * 1.5f);
        	Vector2f.add(ship0.getVelocity(), direction, ship0.getVelocity());
        	boosted = true;
        }
        
        
        // then we spawn smoke
        while (smokeCount >= 0f) {
        	smokeCount -= 1f;
        	
        	for (int i=0; i < 4; i++) {
        		
    			Vector2f smokeVel = new Vector2f();
    			smokeVel.x += (ship0.getVelocity().x * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
    			smokeVel.y += (ship0.getVelocity().y * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
    			
    			engine.addSwirlyNebulaParticle(MathUtils.getRandomPointInCircle(ship0.getLocation(), ship0.getCollisionRadius() - 15f),
    					smokeVel,
    					MathUtils.getRandomNumberInRange(27f, 36f), //size
    					1.9f, //end mult
    					0.6f, //ramp fraction
    					0.45f, //full bright fraction
    					0.3f, //duration
    					new Color(60,100,90,46),
    					true);
    			
    			engine.addNebulaParticle(MathUtils.getRandomPointInCircle(ship0.getLocation(), ship0.getCollisionRadius() - 20f),
    					smokeVel,
    					MathUtils.getRandomNumberInRange(35f, 43f), //size
    					2.1f, //end mult
    					0.6f, //ramp fraction
    					0.65f, //full bright fraction
    					0.4f, //duration
    					new Color(60,100,90,69),
    					true);
        		
        		for (int j=0; j < 3; j++) {
        			
        			float angle = MathUtils.getRandomNumberInRange(0f, 360f);
	        		Vector2f sparkPoint = MathUtils.getPointOnCircumference(ship0.getLocation(), MathUtils.getRandomNumberInRange(0f, ship0.getCollisionRadius() - 10f), angle);
	        		Vector2f sparkVel = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(9f, 18f), angle);
	        		
	        		sparkVel.x += (ship0.getVelocity().x * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
    				sparkVel.y += (ship0.getVelocity().y * MathUtils.getRandomNumberInRange(0.5f, 0.6f));
    				
        			engine.addSmoothParticle(sparkPoint,
	        				sparkVel,
	            			MathUtils.getRandomNumberInRange(4f, 8f),
	            			1f,
	            			MathUtils.getRandomNumberInRange(0.35f, 0.55f),
	            			new Color(65,220,195,210));
        			
        		}
        	}
        	
        }
        
        // here we do a timer to re-trigger smoke spawning
        if (fxDelay >= 0f) {
        	fxDelay -= timer;
        } else {
        	fxDelay = 0.1f;
        	smokeCount += 1f;
        }
        
        // this is to slow the ship down towards normal max speed during the OUT state
        if (state == ShipSystemStatsScript.State.OUT) {
        	if (ship0.getVelocity().lengthSquared() > (ship0.getMaxSpeed() * ship0.getMaxSpeed())) {
        		float decelMult = Math.max(0.5f, Math.min(2f, stats.getDeceleration().getModifiedValue() / stats.getDeceleration().getBaseValue()));
        		float adjFalloffPerSec = 0.25f * (float) Math.pow(decelMult, 0.5);
        		ship0.getVelocity().scale((float) Math.pow(adjFalloffPerSec, timerFlat));
        	}
        }
        
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    	stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		smokeCount = 3f;
		fxDelay = 0.2f;
		boosted = false;
		
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);

		stats.getEnergyRoFMult().unmodify(id);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (ship != null) {
            if (ship.getEngineController().isFlamedOut()) {
                return failstate;
            }
        }
        return null;
    }
    
}
