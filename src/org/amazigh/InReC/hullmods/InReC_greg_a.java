package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_greg_a extends BaseHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
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
		
		LabelAPI label = tooltip.addPara("Replaces the Lacayo drones deployed by the ships system with a variant suited to an anti-ship assault role.", opad);

		label = tooltip.addPara("Deployed Lacayo drones have their weapon replaced with a %s.", opad, h, "Fusion Bolter");
		label.setHighlight("Fusion Bolter");
		label.setHighlightColors(h);
		label = tooltip.addPara("Drones feature a solid-state flux capacitor, and will self destruct after firing %s bursts.", pad, h, "15");
		label.setHighlight("15");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Deployed Lacayo drones have their %s and %s increased to %s and %s respectively.", pad, h, "Hull", "Armour", "750", "200");
		label.setHighlight("Hull", "Armour", "750", "200");
		label.setHighlightColors(h, h, h, h);
		
		label = tooltip.addPara("May only be installed on InReCo vessels that feature a Lacayo deployment system, and only one Lacayo Variant may be installed at a time.", opad);
		
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_gregSlot") && !ship.getVariant().getHullMods().contains("InReC_greg_s") && !ship.getVariant().getHullMods().contains("InReC_greg_p");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_gregSlot");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("InReC_greg_s") || ship.getVariant().getHullMods().contains("InReC_greg_p")) {
			return "May only install one Lacayo Variant at a time.";
		}
		if (!ship.getVariant().getHullMods().contains("InReC_gregSlot")) {
			return "Only compatible with InReCo vessels that feature a Lacayo deployment system.";
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
