package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class InReC_gregHullmod extends BaseHullMod {
	
	public static float HEALTH_BONUS = 50f; // +50% engine health
	public static float REPAIR_MULT = 0.75f; // -25% weapon repair time
	public static float EMP_RESIST = 0.7f; // -30% emp damage taken
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, REPAIR_MULT);
		stats.getEmpDamageTakenMult().modifyMult(id, EMP_RESIST);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
