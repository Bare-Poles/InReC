package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_siegeMods extends BaseHullMod {
	
	public static final float SPEED_MALUS = 0.2f;
	
	public static final float RoF_BONUS = 50f;
	
	public static final float RANGE_BONUS = 200f;
	public static final float FLUX_MALUS = 5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getMaxSpeed().modifyMult(id, 1f - SPEED_MALUS);
		
		stats.getMissileRoFMult().modifyMult(id, 1f + (RoF_BONUS * 0.01f));
		
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
		
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f + (FLUX_MALUS * 0.01f));
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f + (FLUX_MALUS * 0.01f));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f + (FLUX_MALUS * 0.01f)); // yes, we boost missile wep flux cost! it doesn't specify that it's only ball/en flux boost after all!
		
	}
	
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		ship.getEngineController().fadeToOtherColor(this, new Color(145,255,75,255), new Color(100,110,90,30), 1f, 0.6f);
		ship.getEngineController().extendFlame(this, -0.2f, -0.2f, -0.2f);
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
		
		int speedDesc = (int) (100f * SPEED_MALUS);
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("A series of modifications that optimise the vessel for engaging in long range bombardment.", opad);
		
		label = tooltip.addPara("Missile weapon Rate of Fire increased by %s.", pad, h, (int)RoF_BONUS + "%");
		label.setHighlight((int)RoF_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Increases the range of ballistic and energy weapons by %s.", pad, h, "" + (int)RANGE_BONUS);
		label.setHighlight("" + (int)RANGE_BONUS);
		label.setHighlightColors(h);
		
		
		label = tooltip.addPara("The flux cost to fire all weapons is increased by %s.", opad, bad, (int)FLUX_MALUS + "%");
		label.setHighlight((int)FLUX_MALUS + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("The ship's top speed in combat is reduced by %s.", pad, bad, speedDesc + "%");
		label.setHighlight(speedDesc + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", opad);
		
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotShield_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotShield_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		boolean valid = false;
		if (ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotShield_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b")) {
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
