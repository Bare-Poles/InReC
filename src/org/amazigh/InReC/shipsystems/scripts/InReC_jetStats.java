package org.amazigh.InReC.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class InReC_jetStats extends BaseShipSystemScript {

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, 60f);
			stats.getAcceleration().modifyFlat(id, 15f * effectLevel);
			stats.getAcceleration().modifyPercent(id, 240f * effectLevel);
			stats.getDeceleration().modifyFlat(id, 10f * effectLevel);
			stats.getDeceleration().modifyPercent(id, 240f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 10f * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, 50f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 5f);
			stats.getMaxTurnRate().modifyPercent(id, 30f);
		}
		
		if (stats.getEntity() instanceof ShipAPI && false) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			String key = ship.getId() + "_" + id;
			Object test = Global.getCombatEngine().getCustomData().get(key);
			if (state == State.IN) {
				if (test == null && effectLevel > 0.2f) {
					Global.getCombatEngine().getCustomData().put(key, new Object());
					ship.getEngineController().getExtendLengthFraction().advance(1f);
					for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
						if (engine.isSystemActivated()) {
							ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
						}
					}
				}
			} else {
				Global.getCombatEngine().getCustomData().remove(key);
			}
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+60 top speed", false);
		}
		return null;
	}
}
