package org.amazigh.InReC.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class InReC_quickCaps extends BaseHullMod {
	
	public static final float ROF_BONUS = 25f;
	public static final float FLUX_BONUS = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getEnergyRoFMult().modifyPercent(id, ROF_BONUS);
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_BONUS);
        
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ROF_BONUS + "%";
		if (index == 1) return "" + (int) FLUX_BONUS + "%";
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
