package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class InReC_recycler extends BaseHullMod {

	public static final float SHIELD_MALUS = 25f;
	
	public static final float SHIELD_BONUS = 25f;
	public static final float MANEUVER_BONUS = 30f;	
	public static final float ROF_BONUS = 15f;
	public static final float ROF_BONUS_PRIMED = 20f;
	
	public static final float FLAT_GAIN = 2f;
	public static final float SCALING_GAIN = 8f;
	public static final float BASE_DECAY = 4f;
	public static final float VENT_DECAY = 6f;
	public static final float PRIMED_DECAY = 8f;
	
	
	private IntervalUtil smokeInterval = new IntervalUtil(0.15f,0.2f);
	
	private static Map<HullSize, Integer> smokeCount = new HashMap<HullSize, Integer>();
	static {
		smokeCount.put(HullSize.FIGHTER, 2);
		smokeCount.put(HullSize.FRIGATE, 3);
		smokeCount.put(HullSize.DESTROYER, 5);
		smokeCount.put(HullSize.CRUISER, 7);
		smokeCount.put(HullSize.CAPITAL_SHIP, 9);
		smokeCount.put(HullSize.DEFAULT, 5);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getShieldUpkeepMult().modifyMult(id, 1f + (SHIELD_MALUS * 0.01f));
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
		ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_RECYCLER_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
		MutableShipStatsAPI stats = ship.getMutableStats();
		
			// heat generation/decay
		float heatMod = 1f;
		if (ship.getFluxLevel() < 0.5f) {
			heatMod = (ship.getFluxLevel() * (2f * BASE_DECAY)) - BASE_DECAY; // scaling from -4% at zero flux, up to 0% at half flux
		} else {
			heatMod = FLAT_GAIN + ((ship.getFluxLevel() - 0.5f) * (2f * SCALING_GAIN)); // scaling from +2% at half flux, up to +10% at full flux
		}
		if (ship.getFluxTracker().isOverloadedOrVenting()) {
			heatMod -= VENT_DECAY; // - heat if venting or overloaded (a "punishment" for not managing to "push the limit" and getting overloaded or having to vent) 
		}
		if (info.PRIMED) {
			heatMod -= PRIMED_DECAY; // - heat if primed (to make it hard to have 100% uptime)
		}
		info.HEAT += (amount * heatMod);
			// heat generation/decay
		
			// priming checks
		if (info.HEAT > 100f) {
			info.HEAT = 100f;
			info.PRIMED = true;
		} else if (info.HEAT < 0f) {
			info.HEAT = 0f;
			info.PRIMED = false;
		}
			// priming checks
		
		
			//base RoF bonus
		float rofMult = ROF_BONUS * (info.HEAT * 0.01f);
		if (info.PRIMED) {
			rofMult = ROF_BONUS_PRIMED; // full! bonus when primed!
		}
		stats.getBallisticRoFMult().modifyPercent(spec.getId(), rofMult);
		stats.getBallisticAmmoRegenMult().modifyPercent(spec.getId(), rofMult);
				// moved to be "base" because i really felt the hullmod was too tricky for AI to use, so this bonus (which also ramps flux generation a lil bit!) is (mostly) for them
			//base RoF bonus
		
		
			// core stat mod / vfx section
		if (info.PRIMED) {
			stats.getShieldDamageTakenMult().modifyMult(spec.getId(), 1f - (SHIELD_BONUS * 0.01f));

			stats.getAcceleration().modifyPercent(spec.getId(), MANEUVER_BONUS * 2f);
			stats.getDeceleration().modifyPercent(spec.getId(), MANEUVER_BONUS);
			stats.getTurnAcceleration().modifyPercent(spec.getId(), MANEUVER_BONUS * 2f);
			stats.getMaxTurnRate().modifyPercent(spec.getId(), MANEUVER_BONUS);
			

	    	// spawn smoke, more smoke with larger ships!
			float sizeScalar = 1f + (smokeCount.get(ship.getHullSize()) * 0.1f);
			smokeInterval.advance(amount * sizeScalar);
			if (smokeInterval.intervalElapsed()) {
				
            	float angleWedge = 360 / smokeCount.get(ship.getHullSize());
            	float initAngle = MathUtils.getRandomNumberInRange(0f, angleWedge);
				int smokeAlpha = 20 + (int) (info.HEAT * 0.6); // 20-80
            	
				float fullBright = 0.4f;
				if (info.HEAT < 30f) {
					fullBright = 0.1f + (info.HEAT * 0.01f); // 0.1-0.4
				}
				
            	for (int i=0; i < smokeCount.get(ship.getHullSize()); i++) {
            		
            		float angle = initAngle + (i * angleWedge) + MathUtils.getRandomNumberInRange(0f, angleWedge * 0.4f);
					float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
					
	            	engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
			        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
							ship.getCollisionRadius() * 0.3f,
							MathUtils.getRandomNumberInRange(2.1f, 2.7f),
							0.8f,
							fullBright,
							1f,
							new Color(110,95,90,smokeAlpha),
							false);
            	}
            }
            
			
		} else {
			stats.getShieldDamageTakenMult().unmodify(spec.getId());
			
			stats.getAcceleration().unmodify(spec.getId());
			stats.getDeceleration().unmodify(spec.getId());
			stats.getTurnAcceleration().unmodify(spec.getId());
			stats.getMaxTurnRate().unmodify(spec.getId());
			
			stats.getBallisticRoFMult().unmodify(spec.getId());
		}
			// core stat mod / vfx section
		
		
		
		// vfx!
		
		if (info.HEAT > 5f) {
			info.GLOW_FADE = Math.min(2f, info.GLOW_FADE + amount); // a 2 second fade in/out added, to prevent "pop-in/pop-out"
		} else {
			info.GLOW_FADE = Math.max(0f, info.GLOW_FADE - amount);
		}
		
		if (info.GLOW_FADE > 0f) {
			float glowLength = ship.getCollisionRadius() * 2f + (info.HEAT * (smokeCount.get(ship.getHullSize()) * 0.1f));
			
			int alpha = 15 + (int) (105 * info.GLOW_FADE);  // 15 min value, then scales up to 225 at max heat
	    	double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
			alpha = (int) Math.ceil(alpha / timeMult);
	    	alpha = Math.min(alpha, 255);
	    	int alpha2 = (int) (alpha * 0.5f); 
	    	
	    	
	    	int green = 125;
	    	int blue = 80;
	    	 if (info.HEAT > 75) {
	    		green = Math.max(0, 125 - (int)(2 * (info.HEAT - 75f))); // if heat is over 75, then the color shifts from orange towards red (a visual indicator that it's close to priming)
		    	blue = Math.max(0, 80 - (int)(info.HEAT - 75f));
	    	}
	    	
			Color glowColor1 = new Color(250,green,blue,alpha);
			Color glowColor2 = new Color(250,green,blue,alpha2);
			
			SpriteAPI Glow1 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
			SpriteAPI Glow2 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
	    	Vector2f glowSize1 = new Vector2f(glowLength, glowLength);
	    	Vector2f glowSize2 = new Vector2f(glowLength * 0.9f, glowLength * 0.9f);
	    	
	    	MagicRender.singleframe(Glow1, ship.getLocation(), glowSize1, ship.getFacing(), glowColor1, true, CombatEngineLayers.BELOW_SHIPS_LAYER);
	    	MagicRender.singleframe(Glow2, ship.getLocation(), glowSize2, ship.getFacing(), glowColor2, true, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
	    	
	    	
	    	
		}
		
		
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			
			int displayHeat = (int) heatMod;
			
			String info1 = "Idle -";
			String info2 = " Heat Decaying by " + displayHeat + "%";
			
			if (info.PRIMED) {
				info1 = "Active -";
			}
			
			if (heatMod > 0f) {
				if (info.HEAT < 0.1f) {
					info2 = " Heat Depleted";
				} else {
					info2 = " Heat Increasing by " + displayHeat + "%";
				}
			}
			
			Global.getCombatEngine().maintainStatusForPlayerShip("INREC_RECYCLER", "graphics/icons/tactical/venting_flux2.png", "Recycler Heat: " + (int)info.HEAT + "%", info1 + info2, false);
			
		}
		
		engine.getCustomData().put("InReC_RECYCLER_DATA_KEY" + ship.getId(), info);
		
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
		float pluspad = 18f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		
		LabelAPI label = tooltip.addPara("An advanced system that recycles the passive thermal energy generated from flux buildup and uses it to enhance the combat performance of the ship.", opad);
		
		
		label = tooltip.addPara("Integration of the recycler interferes with the ships shield emitter.", opad);
		label = tooltip.addPara("Shield upkeep is increased by %s.", pad, bad, "" + (int)SHIELD_MALUS +"%");
		label.setHighlight("" + (int)SHIELD_MALUS +"%");
		label.setHighlightColors(bad);
		
		
		label = tooltip.addPara("When the ship has %s flux, the recycler generates heat.", opad, h, "over 50%");
		label.setHighlight("over 50%");
		label.setHighlightColors(h);
		label = tooltip.addPara("The higher the ships flux level, the faster the recycler generates heat.", pad);
		label = tooltip.addPara("As heat rises, Ballistic weapon Rate of Fire and Ammo Regeneration is increased by up to %s.", pad, h, "" + (int)ROF_BONUS +"%");
		label.setHighlight("" + (int)ROF_BONUS +"%");
		label.setHighlightColors(h);
		
		
		label = tooltip.addPara("Once heat reaches %s the recycler switches to an active state, granting the following buffs:", opad, h, "100%");
		label.setHighlight("100%");
		label.setHighlightColors(h);
		label = tooltip.addPara("The amount of damage taken by shields is reduced by %s.", pad, h, "" + (int)SHIELD_BONUS +"%");
		label.setHighlight("" + (int)SHIELD_BONUS +"%");
		label.setHighlightColors(h);
		label = tooltip.addPara("The ship's maneuverability is improved by %s.", pad, h, "" + (int)MANEUVER_BONUS +"%");
		label.setHighlight("" + (int)MANEUVER_BONUS +"%");
		label.setHighlightColors(h);
		label = tooltip.addPara("The Ballistic weapon Rate of Fire and Ammo Regeneration bonus is increased to %s.", pad, h, "" + (int)ROF_BONUS_PRIMED +"%");
		label.setHighlight("" + (int)ROF_BONUS_PRIMED +"%");
		label.setHighlightColors(h);
		label = tooltip.addPara("The recycler will exit the active state and stop granting these buffs if heat drops to %s.", opad, h, "0%");
		label.setHighlight("0%");
		
		
		tooltip.addSectionHeading("Heat Generation", Alignment.MID, pluspad);
		tooltip.addPara("While heat generation/decay primarily scales with current flux level there are other modifiers, a full list is as follows:", pad);
		
		float col1 = 150;
		float col2 = 120;
		int totalGain = (int) (SCALING_GAIN + FLAT_GAIN);
		
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				20f, true, true, 
				new Object [] {"--", col1, "Heat Per Second", col2});
		tooltip.addRow(Alignment.MID, h, "Flux over 50%",
				Alignment.MID, h, "up to +" + totalGain + "%");
		tooltip.addRow(Alignment.MID, h, "Flux under 50%",
				Alignment.MID, bad, "up to -" + (int)BASE_DECAY + "%");
		tooltip.addRow(Alignment.MID, h, "Recycler Active",
				Alignment.MID, bad, "-" + (int)PRIMED_DECAY + "%");
		tooltip.addRow(Alignment.MID, h, "Overloaded/Venting",
				Alignment.MID, bad, "-" + (int)VENT_DECAY + "%");
		tooltip.addTable("", 0, opad);
		label = tooltip.addPara("The change in heat over time is a combination all currently active modifiers.", opad);
		
		
		label = tooltip.addPara("May only be installed on InReCo vessels.", pluspad);
		
	}

	public float getTooltipWidth() {
		return 466f;
	}
	
	private class ShipSpecificData {
		private float HEAT = 0f;
		private boolean PRIMED = false;
		private float GLOW_FADE = 0f;
	}
	
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b")  || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b")  || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		boolean valid = false;
		if (ship.getVariant().getHullMods().contains("InReC_slot") || ship.getVariant().getHullMods().contains("InReC_slotVent_b")  || ship.getVariant().getHullMods().contains("InReC_slotFlares_b") || ship.getVariant().getHullMods().contains("InReC_slotRange_b")) {
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
