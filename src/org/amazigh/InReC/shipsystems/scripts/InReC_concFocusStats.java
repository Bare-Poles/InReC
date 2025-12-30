package org.amazigh.InReC.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class InReC_concFocusStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 30f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% energy weapon damage" , false);
		}
		return null;
	}
}