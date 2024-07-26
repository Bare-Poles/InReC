package org.amazigh.InReC.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

public class InReC_heartThrobOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            int owner = projectile.getOwner();
    		Vector2f posZero = projectile.getLocation();
    		
    		CombatFleetManagerAPI FleetManager = engine.getFleetManager(owner);
    		FleetManager.setSuppressDeploymentMessages(true);
    		FleetMemberAPI droneMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, "InReC_pulse_drone_w_wing");
    		droneMember.getRepairTracker().setCrashMothballed(false);
    		droneMember.getRepairTracker().setMothballed(false);
    		droneMember.getRepairTracker().setCR(1f);
    		droneMember.setOwner(owner);
    		droneMember.setAlly(ship.isAlly());
    		
    		ShipAPI drone = engine.getFleetManager(owner).spawnFleetMember(droneMember, posZero, angle, 0f);
    		drone.setCRAtDeployment(0.7f);
    		drone.setCollisionClass(CollisionClass.FIGHTER);
    		
    		Vector2f droneVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(100f, 120f), angle + MathUtils.getRandomNumberInRange(-5f, 5f));
    		drone.getVelocity().set(droneVel);
    		drone.setFacing(angle);
    		FleetManager.setSuppressDeploymentMessages(false);
    		
    		engine.removeEntity(projectile);
    		
    }
  }