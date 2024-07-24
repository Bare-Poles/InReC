package org.amazigh.InReC.scripts;

import org.amazigh.InReC.scripts.ai.InReC_gluonProjScript;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class InReC_gluonOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	engine.addPlugin(new InReC_gluonProjScript(projectile));
    	
    }
  }