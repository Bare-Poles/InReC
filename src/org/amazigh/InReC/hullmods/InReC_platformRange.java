package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.ShipAPI;

public class InReC_platformRange extends BaseHullMod {
	
	public static Map<HullSize, Float> rangeBonus = new HashMap<HullSize, Float>();
	static {
		rangeBonus.put(HullSize.FIGHTER, 0f);
		rangeBonus.put(HullSize.FRIGATE, 0f);
		rangeBonus.put(HullSize.DESTROYER, 0.1f);
		rangeBonus.put(HullSize.CRUISER, 0.3f);
		rangeBonus.put(HullSize.CAPITAL_SHIP, 0.5f);
		rangeBonus.put(HullSize.DEFAULT, 0f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
		if (ship.getWing().getSourceShip() != null) {
			float bonus = 1f + (Float) rangeBonus.get(ship.getWing().getSourceShip().getHullSize());
			
			ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult(spec.getId(), bonus);
			ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(spec.getId(), bonus);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		if (index == 0) return "" + (int) ((float) rangeBonus.get(HullSize.FRIGATE)) + "%"; // casting from:  Float, to float, to int  (in this house we hate Float!)
		if (index == 1) return "" + (int) (rangeBonus.get(HullSize.DESTROYER) * 100f) + "%";
		if (index == 2) return "" + (int) (rangeBonus.get(HullSize.CRUISER) * 100f) + "%";
		if (index == 3) return "" + (int) (rangeBonus.get(HullSize.CAPITAL_SHIP) * 100f) + "%";
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
