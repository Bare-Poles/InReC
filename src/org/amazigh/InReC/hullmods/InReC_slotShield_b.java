package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotShield_b extends BaseHullMod {
	
	public static final float SOFT_FLUX_CONVERSION = 0.25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getShieldSoftFluxConversion().modifyFlat(id, SOFT_FLUX_CONVERSION);
		
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
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("A shield bypass that grants the ability to convert a portion of shield damage to soft flux.", opad);
		
		label = tooltip.addPara("Converts %s of the hard flux damage taken by shields to soft flux.", opad, h, "" + (int) Math.round(SOFT_FLUX_CONVERSION * 100f) + "%");
		label.setHighlight("" + (int) Math.round(SOFT_FLUX_CONVERSION * 100f) + "%");
		label.setHighlightColors(h);
		
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
