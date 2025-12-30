package org.amazigh.InReC.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.util.Misc;

import particleengine.BaseIEmitter;
import particleengine.ParticleData;

import org.amazigh.InReC.scripts.ai.InReC_defoliantMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_fungicideMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_beekeeperMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_apiaristMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_herbicideMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_pesticideMissileAI;
import org.amazigh.InReC.scripts.ai.InReC_sundownMissileAI;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class InReC_ModPlugin extends BaseModPlugin {
	
	public static final String DEFOLIANT_MISSILE_ID = "InReC_defoliant";
	public static final String BEEKEEPER_MISSILE_ID = "InReC_beekeeper";
	public static final String APIARIST_MISSILE_ID = "InReC_apiarist";
	public static final String HERBICIDE_MISSILE_ID = "InReC_herbicide";
	public static final String FUNGICIDE_MISSILE_ID = "InReC_fungicide_mssl";
	public static final String PESTICIDE_MISSILE_ID = "InReC_pesticide";
	public static final String SUNDOWN_MISSILE_ID = "InReC_sundown";
	
	public boolean HAS_GRAPHICSLIB = false;
    
    //New game stuff
    @Override
    public void onNewGameAfterProcGen() {
    }
    
    public void onApplicationLoad() throws Exception {

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            LightData.readLightDataCSV((String)"data/config/InReC_lights_data.csv");
        }
    	
	}

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case DEFOLIANT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_defoliantMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case BEEKEEPER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_beekeeperMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case APIARIST_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_apiaristMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case HERBICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_herbicideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FUNGICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_fungicideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case PESTICIDE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_pesticideMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case SUNDOWN_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new InReC_sundownMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
                return null;
        }
    }
    
    @Override
	public void onCodexDataGenerated() {
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getHullmodEntryId("InReC_greg_a"));
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getHullmodEntryId("InReC_greg_s"));
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getHullmodEntryId("InReC_greg_p"));
    	
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getFighterEntryId("InReC_greg_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("InReC_greg_a"), CodexDataV2.getFighterEntryId("InReC_greg_a_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getFighterEntryId("InReC_greg_a_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("InReC_greg_s"), CodexDataV2.getFighterEntryId("InReC_greg_s_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getFighterEntryId("InReC_greg_s_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("InReC_greg_p"), CodexDataV2.getFighterEntryId("InReC_pulse_drone_g_wing"));
    	CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId("InReC_greg"), CodexDataV2.getFighterEntryId("InReC_pulse_drone_g_wing"));
    	
    	CodexDataV2.makeRelated(CodexDataV2.getFighterEntryId("InReC_pulse_drone_g_wing"), CodexDataV2.getWeaponEntryId("InReC_pulse"));
    	CodexDataV2.makeRelated(CodexDataV2.getFighterEntryId("InReC_pulse_drone_w_wing"), CodexDataV2.getWeaponEntryId("InReC_pulse"));
    	CodexDataV2.makeRelated(CodexDataV2.getFighterEntryId("InReC_greg_a_wing"), CodexDataV2.getWeaponEntryId("InReC_fusion"));
    	CodexDataV2.makeRelated(CodexDataV2.getFighterEntryId("InReC_greg_s_wing"), CodexDataV2.getWeaponEntryId("InReC_gluon"));
    	
    	CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("InReC_parasite"), CodexDataV2.getFighterEntryId("InReC_pulse_drone_g_wing"));
    	
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_heart-throb"), CodexDataV2.getFighterEntryId("InReC_pulse_drone_w_wing"));
    	
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_quark"), CodexDataV2.getWeaponEntryId("InReC_hangnail"));
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_quark"), CodexDataV2.getWeaponEntryId("InReC_wishbone"));
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_quark"), CodexDataV2.getWeaponEntryId("InReC_headstone"));
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_hangnail"), CodexDataV2.getWeaponEntryId("InReC_wishbone"));
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_hangnail"), CodexDataV2.getWeaponEntryId("InReC_headstone"));
    	CodexDataV2.makeRelated(CodexDataV2.getWeaponEntryId("InReC_wishbone"), CodexDataV2.getWeaponEntryId("InReC_headstone"));
    }
    
    // Custom Particle Engine emitter, radial emission within an arc, with random velocity+distance matching facing within a provided min/max value
    // note that dist+vel scale together by default, but it can be set to independent/random
    public static class INREC_RadialEmitter extends BaseIEmitter {
    	
        private final Vector2f location;
		private float angle, arc, minLife, maxLife, minSize, maxSize, minVelocity, addVelocity, minDistance, addDistance, emissionOffsetBase, emissionOffsetAdd, coreDispersion;
        private final float[] color = new float[] {1f, 1f, 1f, 1f};
        private boolean linkage, lifeLink, angleSplit;
        private CombatEntityAPI anchor;

        public INREC_RadialEmitter(CombatEntityAPI host) {
        	anchor = host;
            location = new Vector2f();
            angle = 0f;
            arc = 360f; //these default to giving omnidirectional emission, as that's my main use-case for this Emitter
            minLife = maxLife = 0.5f;
            minSize = 20f;
            maxSize = 30f;
            minVelocity = addVelocity = 1f;
            minDistance = addDistance = 0f;
            emissionOffsetBase = emissionOffsetAdd = 0f;
            linkage = true;
            lifeLink = angleSplit = false;
            coreDispersion = 0;
        }

		@Override
        public SpriteAPI getSprite() { //graphics/portraits/characters/sebestyen.png
            return particleengine.Utils.getLoadedSprite("graphics/fx/particlealpha64sq.png");
        }

        public INREC_RadialEmitter anchor(CombatEntityAPI anchor) {
            this.anchor = anchor;
            return this;
        }

        public INREC_RadialEmitter location(Vector2f location) {
            this.location.set(location);
            return this;
        }

        /**
         * @param angle
         * @return Starting angle an The arc in which to emit particles (defaults have it emit in all directions)
         */
        public INREC_RadialEmitter angle(float angle, float arc) {
            this.angle = angle;
            this.arc = arc;
            return this;
        }

        public INREC_RadialEmitter life(float minLife, float maxLife) {
            this.minLife = minLife;
            this.maxLife = maxLife;
            return this;
        }

        public INREC_RadialEmitter size(float minSize, float maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            return this;
        }

        public INREC_RadialEmitter color(float r, float g, float b, float a) {
            color[0] = r;
            color[1] = g;
            color[2] = b;
            color[3] = a;
            return this;
        }
        
        public INREC_RadialEmitter distance(float minDistance, float addDistance) {
            this.minDistance = minDistance;
            this.addDistance = addDistance;
            return this;
        }
        
        public INREC_RadialEmitter velocity(float minVelocity, float addVelocity) {
            this.minVelocity = minVelocity;
            this.addVelocity = addVelocity;
            return this;
        }
        
        public INREC_RadialEmitter emissionOffset(float emissionOffsetBase, float emissionOffsetAdd) {
            this.emissionOffsetBase = emissionOffsetBase;
            this.emissionOffsetAdd = emissionOffsetAdd;
            return this;
        }
        
        /**
         * @param velDistLinkage
         * @return If set to false then velocity and distance random variance will scale independently of each other
         */
        public INREC_RadialEmitter velDistLinkage(boolean linkage) {
            this.linkage = linkage;
            return this;
        }
        
        /**
         * @param coreDispersion
         * @return random radial offset around point (for 2-dimensional offsetting) (Ignored if below 1)
         */
        public INREC_RadialEmitter coreDispersion(float coreDispersion) {
            this.coreDispersion = coreDispersion;
            return this;
        }

        /**
         * @param angleSplit
         * @return If angle of dist/vel should be generated seperate from each other (defaults to false)
         */
        public INREC_RadialEmitter angleSplit(boolean angleSplit) {
            this.angleSplit = angleSplit;
            return this;
        }

        /**
         * @param lifeLink
         * @return If lifetime should be linked to vel/dist scaling (defaults to false)
         */
        public INREC_RadialEmitter lifeLinkage(boolean lifeLinkage) {
            this.lifeLink = lifeLinkage;
            return this;
        }
        
        
        @Override
        public Vector2f getLocation() {
            return location;
        }

        @Override
        protected ParticleData initParticle(int i) {
            ParticleData data = new ParticleData();

            float rand = MathUtils.getRandomNumberInRange(0f, 1f);
            
            // Life uniformly random between minLife and maxLife
            float life = MathUtils.getRandomNumberInRange(minLife, maxLife);
            
            if (lifeLink) {
            	life = minLife + ((maxLife - minLife) * rand);
            }
            
            data.life(life).fadeTime(0f, life);
            
            // velocity is random within the defined range
            float theta = angle + MathUtils.getRandomNumberInRange(0, arc);
            Vector2f vel = Misc.getUnitVectorAtDegreeAngle(theta + (emissionOffsetBase + MathUtils.getRandomNumberInRange(0, emissionOffsetAdd)));
            vel.scale(minVelocity + (rand * addVelocity));

            if (angleSplit) {
            	theta = angle + MathUtils.getRandomNumberInRange(0, arc);
            }
            
            Vector2f pt = new Vector2f(0,0);
            
            if (linkage) {
                pt = MathUtils.getPointOnCircumference(null, minDistance + (rand * addDistance), theta);
            } else {
                pt = MathUtils.getPointOnCircumference(null, minDistance + (MathUtils.getRandomNumberInRange(0f, 1f) * addDistance), theta);
            }
            
            if (coreDispersion >= 1f) {
            	Vector2f.add(MathUtils.getRandomPointInCircle(null, coreDispersion), pt, pt);
            }
            
            // Add the anchor's velocity, if it exists
            if (anchor != null) {
                Vector2f.add(anchor.getVelocity(), vel, vel);
            }
            data.offset(pt).velocity(vel);
            
            // Size uniformly random between minSize and maxSize
            float size = MathUtils.getRandomNumberInRange(minSize, maxSize);
            data.size(size, size);
            
            // Color
            data.color(color);
            
            return data;
        }
        
    }
}