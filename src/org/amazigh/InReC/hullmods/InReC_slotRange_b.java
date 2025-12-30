package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotRange_b extends BaseHullMod {
	
	public static final int TARGET_RANGE = 900;
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		ship.addListener(new InReC_EnergyCalibratorRangeModifier(TARGET_RANGE));
	}

	
	public static class InReC_EnergyCalibratorRangeModifier implements WeaponBaseRangeModifier {
		public float targetRange;
		public InReC_EnergyCalibratorRangeModifier(float targetRange) {
			this.targetRange = targetRange;
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
				
				float base = weapon.getSpec().getMaxRange();
				
				bonus = (targetRange - base) * 0.5f; // "Anti-SO" : if base range is below 900, increase range to halfway between base and 900
				
				if (weapon.hasAIHint(AIHints.PD)) {
					bonus *= 0.5f; // But! only half the bonus for PD weapons!
				}

				if (base + bonus > targetRange) {
					bonus = targetRange - base; // a lil sanity check, just in case!
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
		
		LabelAPI label = tooltip.addPara("A specialised calibration system that increases the range of projectile energy weaponry.", opad);
		
		  // Convervent Bolt Calibrator
		
		label = tooltip.addPara("Increases the base range of all non-beam Energy and Hybrid weapons that have a base range below %s.", opad, h, "" + (int)TARGET_RANGE);
		label.setHighlight("" + (int)TARGET_RANGE);
		label.setHighlightColors(h);
		label = tooltip.addPara("Shorter ranged weapons recieve a greater range bonus, point defense weapons recieve a halved range bonus.", pad);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addSectionHeading("Current Range Bonuses", Alignment.MID, opad);
			
			List<WeaponAPI> weapons = new ArrayList<WeaponAPI>();
			Set<String> seen = new LinkedHashSet<String>();
			if (ship != null) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (!isAffected(w)) continue;
					String id = w.getId();
					if (seen.contains(id)) continue;
					seen.add(id);
					weapons.add(w);
				}
			}
			
			float costW = 120f;
			float nameW = width - (costW * 2f) - 5f;
			tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
							   20f, true, true, 
							   new Object [] {"Affected weapon", nameW, "Base Range", costW, "Boosted Range", costW});
			int max = 12;
			int count = 0;
			for (WeaponAPI w : weapons) {
				count++;
				int base = (int) w.getOriginalSpec().getMaxRange();
				
				float bonus =  (TARGET_RANGE - base) * 0.5f;
				if (w.hasAIHint(AIHints.PD)) {
					bonus *= 0.5f;
				}
				float boosted = base + bonus;
				
				String name = tooltip.shortenString(w.getDisplayName(), nameW - 20f);
				tooltip.addRow(Alignment.LMID, Misc.getTextColor(), name,
						Alignment.MID, h, Misc.getRoundedValueOneAfterDecimalIfNotWhole(base),
						Alignment.MID, h, Misc.getRoundedValueOneAfterDecimalIfNotWhole(boosted));
				if (count >= max) break;
			}
			tooltip.addTable("No affected weapons mounted", weapons.size() - max, opad);
		} else {
			tooltip.addSectionHeading("Example Range Bonuses", Alignment.MID, opad);
			
			float col1 = 85;
			float col2 = 107;
			float col3 = 139;
			
			tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
					20f, true, true, 
					new Object [] {"Base Range", col1, "Boosted Range", col2, "Boosted Range (PD)", col3});
			
			tooltip.addRow(Alignment.MID, h, "300",
					Alignment.MID, h, "600",
					Alignment.MID, h, "450");
			tooltip.addRow(Alignment.MID, h, "400",
					Alignment.MID, h, "650",
					Alignment.MID, h, "525");
			tooltip.addRow(Alignment.MID, h, "500",
					Alignment.MID, h, "700",
					Alignment.MID, h, "600");
			tooltip.addRow(Alignment.MID, h, "600",
					Alignment.MID, h, "750",
					Alignment.MID, h, "675");
			tooltip.addRow(Alignment.MID, h, "700",
					Alignment.MID, h, "800",
					Alignment.MID, h, "750");
			tooltip.addRow(Alignment.MID, h, "800",
					Alignment.MID, h, "850",
					Alignment.MID, h, "825");
			tooltip.addTable("", 0, opad);
		}
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this range modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
		
	}
	
	public static boolean isAffected(WeaponAPI w) {
		if (w == null) return false;
		if (w.getType() == WeaponType.ENERGY || w.getType() == WeaponType.HYBRID) {
			// do nothing!
		} else {
			return false;
		}
		if (w.getOriginalSpec().getMaxRange() >= 900) return false;
		if (w.isDecorative()) return false;
		if (w.getSlot() != null && w.getSlot().isSystemSlot()) return false;
		return true;
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
