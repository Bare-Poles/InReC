package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;

import java.awt.Color;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotRange_b extends BaseHullMod {
	
	public static final int RANGE_BONUS = 100;
	public static final float MAX_RANGE = 750;
	
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		ship.addListener(new InReC_EnergyRangefinderBuiltInRangeModifier(MAX_RANGE));
	}
	
	public static class InReC_EnergyRangefinderBuiltInRangeModifier implements WeaponBaseRangeModifier {
		public float maxRange;
		public InReC_EnergyRangefinderBuiltInRangeModifier(float maxRange) {
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
				
				float bonus = RANGE_BONUS;
				
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
		
		LabelAPI label = tooltip.addPara("A series of regulators that allow for an increase in the range of projectile energy weaponry.", opad);
		
		label = tooltip.addPara("Increases the base range of all non-beam Energy and Hybrid weapons by %s.", opad, h, "" + (int)RANGE_BONUS);
		label.setHighlight("" + (int)RANGE_BONUS);
		label.setHighlightColors(h);
		label = tooltip.addPara("This range increase is limited to a maximum of %s range.", pad, h, "" + (int)MAX_RANGE);
		label.setHighlight("" + (int)MAX_RANGE);
		label.setHighlightColors(h);
		
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
