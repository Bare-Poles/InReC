package org.amazigh.InReC.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.ArrayList;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_BurstDriveAI implements ShipSystemAIScript {

	private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    // check five times a second, a balance of optimization and responsiveness
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.2f);
    
    private static final ArrayList<AIFlags> pro = new ArrayList<AIFlags>();
    private static final ArrayList<AIFlags> con = new ArrayList<AIFlags>();
    static {
        pro.add(AIFlags.PURSUING);
        pro.add(AIFlags.HARASS_MOVE_IN);
        pro.add(AIFlags.RUN_QUICKLY);
        pro.add(AIFlags.TURN_QUICKLY);
        pro.add(AIFlags.BACKING_OFF);
        
        con.add(AIFlags.DO_NOT_PURSUE);
    }
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
    	this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        
    	// don't check if paused / can't use the system
    	if (engine.isPaused() || !AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }

        // don't check if timer not up
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }

        boolean useMe = false;
        
        AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);

        if (assignment != null && assignment.getType() == CombatAssignmentType.RETREAT) {
            ship.useSystem();
            return;
        }
        
        Vector2f shipVel = ship.getVelocity();
        
        if (shipVel.length() >= ship.getMaxSpeedWithoutBoost()) {
            useMe = true;
        }
        
        for (AIFlags f : pro) {
            if (flags.hasFlag(f)) useMe = true;
        }
        
        for (AIFlags f : con) {
            if (flags.hasFlag(f)) useMe = false;
        }

        if (useMe) {
            ship.useSystem();
        }
        
    }
}
