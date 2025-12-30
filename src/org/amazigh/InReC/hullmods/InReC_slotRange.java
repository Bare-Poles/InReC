package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotRange extends BaseHullMod {
	
	public static final int BASE_RANGE_BONUS = 50;
	public static final int SMOD_RANGE_BONUS = 100;
	
	public static final float FRIG_MAX_RANGE = 700;
	public static final float DEST_MAX_RANGE = 750;
	public static final float CRU_MAX_RANGE = 800;
	public static final float CAP_MAX_RANGE = 850;
	
	private static Map<HullSize, Float> MAX_RANGE = new HashMap<HullSize, Float>();
	static {
		MAX_RANGE.put(HullSize.FRIGATE, FRIG_MAX_RANGE);
		MAX_RANGE.put(HullSize.DESTROYER, DEST_MAX_RANGE);
		MAX_RANGE.put(HullSize.CRUISER, CRU_MAX_RANGE);
		MAX_RANGE.put(HullSize.CAPITAL_SHIP, CAP_MAX_RANGE);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		boolean sMod = isSMod(stats);
		float maxRange = MAX_RANGE.get(ship.getHullSize());
		if (sMod) {
			maxRange += 50f;
		}
		
		ship.addListener(new InReC_EnergyRangefinderRangeModifier(sMod, maxRange));
	}
	
	public static class InReC_EnergyRangefinderRangeModifier implements WeaponBaseRangeModifier {
		public boolean sMod;
		public float maxRange;
		public InReC_EnergyRangefinderRangeModifier(boolean sMod, float maxRange) {
			this.sMod = sMod;
			this.maxRange = maxRange;
		}
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			
			if (weapon.isBeam()) return 0f;
			if (weapon.getType() == WeaponType.ENERGY || weapon.getType() == WeaponType.HYBRID) {
				
				float bonus = 0;
				if (sMod) {
					bonus = SMOD_RANGE_BONUS;
				} else {
					bonus = BASE_RANGE_BONUS;
				}
				if (bonus == 0f) return 0f;
				
				float base = weapon.getSpec().getMaxRange();
				if (base + bonus > maxRange) {
					bonus = maxRange - base;
				}
				if (bonus < 0) bonus = 0;
				return bonus;
				
			}
			return 0f;
			
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
		
		LabelAPI label = tooltip.addPara("Installs a series of regulators that allow for an increase in the range of projectile energy weaponry.", opad);
		
		label = tooltip.addPara("Increases the base range of all non-beam Energy and Hybrid weapons by %s.", opad, h, "" + (int)BASE_RANGE_BONUS);
		label.setHighlight("" + (int)BASE_RANGE_BONUS);
		label.setHighlightColors(h);
		label = tooltip.addPara("This range increase is limited to %s/%s/%s/%s, depending on this ship's hull size.", pad, h, "" + (int)FRIG_MAX_RANGE, "" + (int)DEST_MAX_RANGE, "" + (int)CRU_MAX_RANGE, "" + (int)CAP_MAX_RANGE);
		label.setHighlight("" + (int)FRIG_MAX_RANGE, "" + (int)DEST_MAX_RANGE, "" + (int)CRU_MAX_RANGE, "" + (int)CAP_MAX_RANGE);
		label.setHighlightColors(h, h, h, h);
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this range modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", opad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)SMOD_RANGE_BONUS;
		if (index == 1) return "" + (int)(FRIG_MAX_RANGE + 50);
		if (index == 2) return "" + (int)(DEST_MAX_RANGE + 50);
		if (index == 3) return "" + (int)(CRU_MAX_RANGE + 50);
		if (index == 4) return "" + (int)(CAP_MAX_RANGE + 50);
		return null;
	}
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		boolean valid = false;
		if (ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b") || ship.getVariant().getHullMods().contains("InReC_slotFlares_b")) {
			valid = true;
		}
		if (ship.getVariant().getHullMods().contains("InReC_slotRange_b") ) {
			return "Already installed.";
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
