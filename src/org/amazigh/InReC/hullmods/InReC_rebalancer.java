package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_rebalancer extends BaseHullMod {
	
	public static final float SHIELD_BONUS = 20f;
	
	public static final float WEP_FLUX_BONUS = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
		MutableShipStatsAPI stats = ship.getMutableStats();

		boolean sMod;
		sMod = isSMod(stats);
		
		float shieldReal = 0f;
		float fluxReal = 0f;
		if (sMod) {
			shieldReal = 1f - (SHIELD_BONUS * 0.025f * Math.min(0.4f, (ship.getFluxLevel() - ship.getHardFluxLevel())));
			fluxReal = 1f - (WEP_FLUX_BONUS * 0.025f * Math.min(0.4f, ship.getHardFluxLevel()));
			// we divide these by 40 because smod! scaling to 40% flux!
		} else {
			shieldReal = 1f - (SHIELD_BONUS * 0.02f * Math.min(0.5f, (ship.getFluxLevel() - ship.getHardFluxLevel())));
			fluxReal = 1f - (WEP_FLUX_BONUS * 0.02f * Math.min(0.5f, ship.getHardFluxLevel()));
			// we only divide these by 50 rather than 100, because the scaling flux level thing only goes up to 0.5x
		}
		
		
		
		stats.getShieldDamageTakenMult().modifyMult(spec.getId(), shieldReal);
		stats.getEnergyWeaponFluxCostMod().modifyMult(spec.getId(), fluxReal);
		
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			
			float shieldStatus = 0f;
			float fluxStatus = 0f;
			
			if (sMod) {
				shieldStatus = SHIELD_BONUS * 2.5f * Math.min(0.4f, (ship.getFluxLevel() - ship.getHardFluxLevel()));
				fluxStatus = WEP_FLUX_BONUS * 2.5f * Math.min(0.4f, ship.getHardFluxLevel());
			} else {
				shieldStatus = SHIELD_BONUS * 2f * Math.min(0.5f, (ship.getFluxLevel() - ship.getHardFluxLevel()));
				fluxStatus = WEP_FLUX_BONUS * 2f * Math.min(0.5f, ship.getHardFluxLevel());
			}
			
			Global.getCombatEngine().maintainStatusForPlayerShip("INREC_REBALANCER", "graphics/icons/hullsys/fortress_shield.png",  "Flux Rebalancer", "Shield bonus: " + (int)shieldStatus + "% / Weapon flux bonus: " + (int)fluxStatus + "%", false);
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
		
		LabelAPI label = tooltip.addPara("Integrated into the vessels core flux systems, this rebalancer allows for the flux grid to compensate for some of the imbalances introduced through combat stress.", opad);
		
		label = tooltip.addPara("As %s and %s flux levels increase, this system will improve ship performance.", opad, h, "Soft", "Hard");
		label.setHighlight("Soft", "Hard");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("As %s flux raises, the amount of damage taken by shields is reduced by up to %s.", opad, h, "Soft", (int)SHIELD_BONUS +"%");
		label.setHighlight("Soft", (int)WEP_FLUX_BONUS +"%");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("As %s flux raises, energy weapon flux cost to fire reduced by up to %s.", pad, h, "Hard", (int)WEP_FLUX_BONUS +"%");
		label.setHighlight("Hard", (int)WEP_FLUX_BONUS +"%");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("Both bonuses reach maximum strength when their relevant flux type reaches %s of current flux capacity.", opad, h, "50%");
		label.setHighlight("50%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", opad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "40%";
		return null;
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
