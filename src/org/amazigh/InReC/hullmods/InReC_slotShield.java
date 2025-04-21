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
	
	public static final float SHIELD_MALUS = 10f;
	public static final float SOFT_FLUX_CONVERSION = 0.3f;
	
	public static final float SHIELD_UPKEEP_BONUS = 25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getShieldSoftFluxConversion().modifyFlat(id, SOFT_FLUX_CONVERSION);
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
		
		LabelAPI label = tooltip.addPara("Installs a shield bypass that trades efficiency for the ability to convert a portion of shield damage to soft flux.", opad);
		
		label = tooltip.addPara("Converts %s of the hard flux damage taken by shields to soft flux.", opad, h, "" + (int) Math.round(SOFT_FLUX_CONVERSION * 100f) + "%");
		label.setHighlight("" + (int) Math.round(SOFT_FLUX_CONVERSION * 100f) + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Shield damage taken increased by %s.", pad, bad, "" + (int)SHIELD_MALUS + "%");
		label.setHighlight("" + (int)SHIELD_MALUS + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", opad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)SHIELD_UPKEEP_BONUS + "%";
		return null;
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
