package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class InReC_slot extends BaseHullMod {
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		// so this is just a hidden dummy hullmod script that does nothing
		// but the hullmods that use this use it as a check for the various InReC "slot" hullmods to determine if the ship is valid to mount them
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
