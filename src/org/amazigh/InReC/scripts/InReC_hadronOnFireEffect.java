package org.amazigh.InReC.scripts;

import java.awt.Color;

import org.amazigh.InReC.scripts.ai.InReC_hadronProjScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_hadronOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
            // attach the weave script
        	engine.addPlugin(new InReC_hadronProjScript(projectile));
            
    		float angle1 = angle + MathUtils.getRandomNumberInRange(-7f, 7f);
    		Vector2f nebVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 31f), angle1);
    		engine.addSwirlyNebulaParticle(projectile.getLocation(),
    				nebVel,
    				15f,
    				1.6f,
    				0.6f,
    				0.55f,
    				MathUtils.getRandomNumberInRange(0.35f, 0.7f),
    				new Color(150,178,42,89),
    				true);
    		
    		for (int j=0; j < 4; j++) {

    			float angle2 = angle + MathUtils.getRandomNumberInRange(-27f, 27f);
                Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(11f, 93f), angle2);
                
                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 3f),
                		sparkVel,
        				MathUtils.getRandomNumberInRange(3f, 6f), //size
        				1f, //brightness
        				MathUtils.getRandomNumberInRange(0.35f, 0.5f), //duration
        				new Color(210,235,55,225));
            	}
    		
    }
  }