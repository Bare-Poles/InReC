package org.amazigh.InReC.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class InReC_gregStats extends BaseShipSystemScript {

	public String WING_NAME = "InReC_greg_wing";
	
	private boolean DEPLOYED = false;
	private boolean MULTI = false;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		}
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipVariantAPI variant = ship.getVariant();
        
        if (!DEPLOYED) {
        	DEPLOYED = true;
        	
            if (variant.getHullMods().contains("InReC_greg_a")) {
            	WING_NAME = "InReC_greg_a_wing";
            } else if (variant.getHullMods().contains("InReC_greg_s")) {
        		WING_NAME = "InReC_greg_s_wing";
    		} else if (variant.getHullMods().contains("InReC_greg_p")) {
        		WING_NAME = "InReC_pulse_drone_g_wing";
        		MULTI = true;
    		}
    		
    		for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if (weapon.isSystemSlot()) {
            		int owner = ship.getOwner();
            		
            		Vector2f posZero = weapon.computePosition(ship);
            		
            		if (MULTI) {
            			for (int i=0; i < 2; i++) {
            				CombatFleetManagerAPI FleetManager = engine.getFleetManager(owner);
                    		FleetManager.setSuppressDeploymentMessages(true);
                    		FleetMemberAPI gregMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, WING_NAME);
                    		gregMember.getRepairTracker().setCrashMothballed(false);
                    		gregMember.getRepairTracker().setMothballed(false);
                    		gregMember.getRepairTracker().setCR(1f);
                    		gregMember.setOwner(owner);
                    		gregMember.setAlly(ship.isAlly());
                    		
                    		ShipAPI greg = engine.getFleetManager(owner).spawnFleetMember(gregMember, posZero, weapon.computeMidArcAngle(ship), 0f);
                    		greg.setCRAtDeployment(0.7f);
                    		greg.setCollisionClass(CollisionClass.FIGHTER);
                    		
                    		Vector2f gregVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(100f, 120f), weapon.computeMidArcAngle(ship) - 24f + (i * 16f));
                    		greg.getVelocity().set(gregVel);
                    		
                    		float gregAngle = weapon.computeMidArcAngle(ship);
                    		greg.setFacing(gregAngle);
                    		
                    		FleetManager.setSuppressDeploymentMessages(false);
                		}
            		} else {
            			CombatFleetManagerAPI FleetManager = engine.getFleetManager(owner);
                		FleetManager.setSuppressDeploymentMessages(true);
                		FleetMemberAPI gregMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, WING_NAME);
                		gregMember.getRepairTracker().setCrashMothballed(false);
                		gregMember.getRepairTracker().setMothballed(false);
                		gregMember.getRepairTracker().setCR(1f);
                		gregMember.setOwner(owner);
                		gregMember.setAlly(ship.isAlly());
                		
                		ShipAPI greg = engine.getFleetManager(owner).spawnFleetMember(gregMember, posZero, weapon.computeMidArcAngle(ship), 0f);
                		greg.setCRAtDeployment(0.7f);
                		greg.setCollisionClass(CollisionClass.FIGHTER);
                		
                		Vector2f gregVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(100f, 120f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-4f, 4f));
                		greg.getVelocity().set(gregVel);
                		
                		float gregAngle = weapon.computeMidArcAngle(ship);                		
                		greg.setFacing(gregAngle);
                		
                		FleetManager.setSuppressDeploymentMessages(false);
                		
            			}
            		
            		engine.addSmoothParticle(posZero, //position
            				ship.getVelocity(), //velocity
                			44f, //size
                			0.8f, //brightness
                			0.05f, //duration
                			new Color(250,205,80,255));
            		
        			for (int i=0; i < 10; i++) {
                        Vector2f smokeVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(4f, 60f));
        	            float randomSize2 = MathUtils.getRandomNumberInRange(20f, 25f);
        	            engine.addSmokeParticle(MathUtils.getRandomPointInCircle(posZero, 30f), smokeVel, randomSize2, 0.9f, MathUtils.getRandomNumberInRange(0.5f, 1.0f), new Color(110,110,100,180));
        	            
        	            for (int j=0; j < 3; j++) {
        	            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(posZero, 15f), //position
        	            			MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 120f)), //velocity
        	            			MathUtils.getRandomNumberInRange(4f, 9f), //size
        	            			MathUtils.getRandomNumberInRange(0.5f, 0.7f), //brightness
        	            			MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
        	            			new Color(255,195,90,255));
        	            }
            		}
                }
    		}
    		
        }
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		DEPLOYED = false;
	}
}
