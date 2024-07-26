package org.amazigh.InReC.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_variableDecoWeaponScript implements EveryFrameWeaponEffectPlugin {

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		// setting weapon frame based on whether or not we have any of the "swap hullmods" installed 
        
        ShipVariantAPI variant = weapon.getShip().getVariant();
		if (variant.getHullMods().contains("InReC_slotVent")) {
			weapon.getAnimation().setFrame(1);
		} else if (variant.getHullMods().contains("InReC_slotShield")) {
			weapon.getAnimation().setFrame(2);
		} else if (variant.getHullMods().contains("InReC_slotFlares")) {
			weapon.getAnimation().setFrame(3);
		} else if (variant.getHullMods().contains("InReC_slotRange")) {
			weapon.getAnimation().setFrame(4);
		} else {
			weapon.getAnimation().setFrame(0);
		}
		
	}
  }