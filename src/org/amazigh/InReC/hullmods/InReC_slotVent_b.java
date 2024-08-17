package org.amazigh.InReC.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class InReC_slotVent_b extends BaseHullMod {
	
	public static final float FLAT_DISS = 40;
	
	public static final float VENT_BONUS = 15;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {	
		stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("InReC_VENT_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
		float fluxDiff = amount * Math.min(FLAT_DISS, (ship.getFluxTracker().getCurrFlux() - ship.getFluxTracker().getHardFlux()));
    	
		// if you have more soft flux than hard flux, dissipate some for free!
		if (fluxDiff > 0f) {
    		ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() - fluxDiff));
    		info.TIMER += amount;
    	}
		
		if (info.TIMER > 0.2f) {
			info.TIMER -= 0.2f;
			for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	    		if (weapon.isDecorative()) {
	    			
	    			int alpha = 30 + (int) (60f * ship.getFluxTracker().getFluxLevel());
	    			
	    			Vector2f ventPoint = weapon.computePosition(ship);
	    			Vector2f ventVel = ship.getVelocity();
	    			Float ventAngle = weapon.computeMidArcAngle(ship);
	    			
	    			Vector2f nebVel = MathUtils.getPointOnCircumference(ventVel, MathUtils.getRandomNumberInRange(9f, 27f), ventAngle + MathUtils.getRandomNumberInRange(-5f, 5f));
	    			
	    			engine.addNebulaParticle(ventPoint,
	    					nebVel,
							12f,
							MathUtils.getRandomNumberInRange(1.6f, 2.0f),
							0.8f,
							0.6f,
							0.85f,
							new Color(120,100,110,90),
							false);
	    			
	    	        for (int i = 0; i < 2; i++) {
	    	        	Vector2f puffRandomVel = MathUtils.getPointOnCircumference(ventVel, MathUtils.getRandomNumberInRange(9f, 27f), ventAngle + MathUtils.getRandomNumberInRange(-5f, 5f));
	        			
	    	        	engine.addSwirlyNebulaParticle(ventPoint,
	    	        			puffRandomVel,
	            				10f,
	            				2.4f,
	            				0.6f,
	            				0.6f,
	            				MathUtils.getRandomNumberInRange(0.69f, 0.9f),
	            				new Color(135,55,165,alpha),
	            				true);
	    	        }
	    	        
	    	        for (int j = 0; j < 3; j++) {
    	        		Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ventPoint, 4f);
    	        		Vector2f sparkVel = MathUtils.getPointOnCircumference(ventVel, MathUtils.getRandomNumberInRange(18f, 48f), ventAngle + MathUtils.getRandomNumberInRange(-4f, 4f));
	        			int sparkAlpha = Math.min(255, (int) (alpha * 3));
    	        		engine.addSmoothParticle(sparkPoint,
    	        				sparkVel,
    	            			MathUtils.getRandomNumberInRange(2f, 3f),
    	            			1f,
    	            			MathUtils.getRandomNumberInRange(0.25f, 0.5f),
    	            			new Color(215,235,225,sparkAlpha));
    	        		
    	        	}
	    			
	    		}
	    	}
		}
		
        engine.getCustomData().put("InReC_VENT_DATA_KEY" + ship.getId(), info);
	}
	
	private class ShipSpecificData {
		 private float TIMER = 0f;
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
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("A set of secondary flux vents that improve dissipation of soft flux.", opad);
		
		label = tooltip.addPara("The secondary vents will passively dissipate up to %s soft flux every second.", opad, h, "" + (int)FLAT_DISS);
		label.setHighlight("" + (int)FLAT_DISS);
		label.setHighlightColors(h);
		
		label = tooltip.addPara("The rate of this bonus dissipation is %s increased by active venting.", pad, bad, "Not");
		label.setHighlight("Not");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("Active flux vent rate increased by %s.", opad, h, "" + (int)VENT_BONUS + "%");
		label.setHighlight("" + (int)VENT_BONUS + "%");
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
