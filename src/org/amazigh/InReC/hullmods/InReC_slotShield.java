package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotShield extends BaseHullMod {
	
	public static final int HARD_FLUX_DISSIPATION_PERCENT = 20;
	public static final float SHIELD_MALUS = 20f;
	// public static float SOFT_FLUX_CONVERSION = 0.1f;
	
	public static final float SHIELD_UPKEEP_BONUS = 25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHardFluxDissipationFraction().modifyFlat(id, (float)HARD_FLUX_DISSIPATION_PERCENT * 0.01f);
		
		// stats.getShieldSoftFluxConversion().modifyFlat(id, SSOFT_FLUX_CONVERSION);
		// this might be a fun stat to use if this initial idea doesn't work out!
		
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + (SHIELD_MALUS * 0.01f));
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getShieldUpkeepMult().modifyMult(id, 1f - SHIELD_UPKEEP_BONUS * 0.01f);
		}
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
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("Installs a shield bypass that trades efficiency for the ability to dissipate hard flux while shields are active.", opad);
		
		label = tooltip.addPara("Allows the ship to dissipate hard flux at %s of the normal rate while shields are on.", opad, h, "" + (int)HARD_FLUX_DISSIPATION_PERCENT + "%");
		label.setHighlight("" + (int)HARD_FLUX_DISSIPATION_PERCENT + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Shield damage taken increased by %s.", pad, bad, "" + (int)SHIELD_MALUS + "%");
		label.setHighlight("" + (int)SHIELD_MALUS + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("May only be installed on InReC vessels, and only one slot hullmod may be installed at a time.", opad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + SHIELD_UPKEEP_BONUS;
		return null;
	}
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") && !ship.getVariant().getHullMods().contains("InReC_slotVent") && !ship.getVariant().getHullMods().contains("InReC_slotFlares") && !ship.getVariant().getHullMods().contains("InReC_slotRange");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("InReC_slotVent") || ship.getVariant().getHullMods().contains("InReC_slotFlares") || ship.getVariant().getHullMods().contains("InReC_slotRange")) {
			return "May only install one InReC Slot hullmod at a time.";
		}
		if (!ship.getVariant().getHullMods().contains("InReC_slot")) {
			return "Only compatible with InReC vessels.";
		}
		return null;
	}
	
	
    @Override
    public Color getBorderColor() {
        return new Color(60,220,210,180);
    }

    @Override
    public Color getNameColor() {
        return new Color(60,220,210,240);
    }
}
