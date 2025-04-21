package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_parasite extends BaseHullMod {

	private static Map<HullSize, Float> mag = new HashMap<HullSize, Float>();
	static {
		mag.put(HullSize.FRIGATE, 2f);
		mag.put(HullSize.DESTROYER, 4f);
		mag.put(HullSize.CRUISER, 6f);
		mag.put(HullSize.CAPITAL_SHIP, 6f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
		ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_PARASITE_DATA_KEY" + ship.getId());
		if (info == null) {
			info = new ShipSpecificData();
		}
		if (info.DONE) {
			return;
		}
		
		float count = (Float) mag.get(ship.getHullSize());
		int owner = ship.getOwner();
		
		for (int i=0; i < count; i++) {
			
			Vector2f posZero = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
			float parasiteFacing = ship.getFacing() + MathUtils.getRandomNumberInRange(-10f, 10f);
			
			CombatFleetManagerAPI FleetManager = engine.getFleetManager(owner);
    		FleetManager.setSuppressDeploymentMessages(true);
    		FleetMemberAPI parasiteMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, "InReC_pulse_drone_g_wing");
    		parasiteMember.getRepairTracker().setCrashMothballed(false);
    		parasiteMember.getRepairTracker().setMothballed(false);
    		parasiteMember.getRepairTracker().setCR(1f);
    		parasiteMember.setOwner(owner);
    		parasiteMember.setAlly(ship.isAlly());
    		
    		ShipAPI parasite = engine.getFleetManager(owner).spawnFleetMember(parasiteMember, posZero, parasiteFacing, 5f);
    		parasite.setCRAtDeployment(0.7f);
    		parasite.setCollisionClass(CollisionClass.FIGHTER);
    		
    		Vector2f parasiteVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(100f, 120f), parasiteFacing);
    		parasite.getVelocity().set(parasiteVel);
    		
    		FleetManager.setSuppressDeploymentMessages(false);
    		
    		info.counted ++;
    		if (info.counted > count) {
    			info.DONE = true;
    		}
    		
		}
		
        engine.getCustomData().put("InReC_PARASITE_DATA_KEY" + ship.getId(), info);
	}
	
	private class ShipSpecificData {
        private boolean DONE = false;
        private float counted = 0.1f; // we start it at 0.1, so it will be properly exceed the threshold reliably (float rounding nonsense)
    }
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("A set of external clamps that allow the ship to carry a set of Veia Drones armed with Pulse Bolters.", opad);
		label = tooltip.addPara("Each drone has %s and %s.", pad, h, "300 Hull", "80 Armour");
		label.setHighlight("300 Hull", "80 Armour");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("The drones feature a solid-state flux capacitor, and will self destruct after firing %s bursts.", pad, h, "15");
		label.setHighlight("15");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("The clamps can carry %s/%s/%s/%s drones, depending on this ship's hull size.", opad, h, "2", "4", "6", "8");
		label.setHighlight("2", "4", "6", "8");
		label.setHighlightColors(h, h, h, h);
		
		label = tooltip.addPara("On entering the battlefield carried drones are automatically launched and will autonomously seek out and engage hostiles.", pad);
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", opad);
		
	}
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		boolean valid = false;
		if (ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b")) {
			valid = true;
		}
		if (!valid) {
			return "Only compatible with InReCo vessels.";
		}
		return null;
	}
	
	
    @Override
    public Color getBorderColor() {
        return new Color(30,110,105,180);
    }

    @Override
    public Color getNameColor() {
        return new Color(60,220,210,240);
    }
}
