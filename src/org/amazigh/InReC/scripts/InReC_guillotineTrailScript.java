package org.amazigh.InReC.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import particleengine.BaseIEmitter;
import particleengine.ParticleData;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;
import java.util.List;

public class InReC_guillotineTrailScript extends BaseEveryFrameCombatPlugin {
	
	private DamagingProjectileAPI projectile;
	final IntervalUtil FXTimer = new IntervalUtil(0.05f, 0.05f);
	int fadeVal = 0;
	private Vector2f parentVel;
	
	public InReC_guillotineTrailScript(@NotNull DamagingProjectileAPI projectile) {
		this.projectile = projectile;
		parentVel = new Vector2f(projectile.getSource().getVelocity());
	}
	
	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}
		
		//Checks if our script should be removed from the combat engine
		if (projectile == null || projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
			engine.removePlugin(this);
			return;
		}
		
		// spawn a fancy trail
		FXTimer.advance(amount);
        
        if (FXTimer.intervalElapsed()) {
        	
        	if (projectile.isFading()) {
        		fadeVal += 10; // was 15 when it was "normal" particles
        	}
        	
        	
        	Vector2f proj_location = projectile.getLocation();
        	float angle = projectile.getFacing();
        	
        	Vector2f smokeVel = MathUtils.getPointOnCircumference(parentVel, MathUtils.getRandomNumberInRange(11f, 26f), angle + MathUtils.getRandomNumberInRange(-2f, 2f));
        	
        	int nebAlpha = Math.max(1, 120 - fadeVal);
        	
        	engine.addNebulaParticle(proj_location,
        			smokeVel,
            		MathUtils.getRandomNumberInRange(29f, 33f),
            		1.9f, //endsizemult
            		0.5f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.5f, 1.7f), //totalDuration
            		new Color(40,50,35,nebAlpha),
            		true);
        	
    		int sparkAlpha = (int)Math.max(1, 137 - (fadeVal * 3f)); // was (197 - 1.6x) alpha when it was "normal" particles
    		
    		InReC_TrailEmitter emitterTrail1 = new InReC_TrailEmitter(proj_location, angle, parentVel, 4f);
        	emitterTrail1.life(1.05f, 1.35f);
        	emitterTrail1.size(2f, 3f);
        	emitterTrail1.velocity(9, 20);
			emitterTrail1.distance(1f, -62f);
			emitterTrail1.color(60,220,210,sparkAlpha);
			emitterTrail1.emissionOffset(45f, 14f);
			emitterTrail1.burst(9);
			
			InReC_TrailEmitter emitterTrail2 = new InReC_TrailEmitter(proj_location, angle, parentVel, 4f);
        	emitterTrail2.life(1.05f, 1.35f);
        	emitterTrail2.size(2f, 3f);
        	emitterTrail2.velocity(9, 20);
			emitterTrail2.distance(1f, -62f);
			emitterTrail2.color(60,220,210,sparkAlpha);
			emitterTrail2.emissionOffset(-59f, 14f);
			emitterTrail2.burst(9);
			
        }
        
	}
	
	// Custom Particle Engine emitter, rather specific for this projs trail spawning purposes
	// spawns particles that are
		// emitted in direction 1
		// offset in direction 2 mainly, but with a "nudge" in direction 1 as well 
    public static class InReC_TrailEmitter extends BaseIEmitter {
    	
        private Vector2f location, parentVel;
		private float angle, minLife, maxLife, minSize, maxSize, minVelocity, addVelocity, minDistance, addDistance, emissionOffsetBase, emissionOffsetAdd, trailNudge;
        private final float[] color = new float[] {1f, 1f, 1f, 1f};
        
        public InReC_TrailEmitter(Vector2f point, float angle1, Vector2f hostVel, float nudge) {
            location = point;
            angle = angle1;
            parentVel = hostVel;
            trailNudge = nudge;
            minLife = maxLife = 0.5f;
            minSize = 20f;
            maxSize = 30f;
            minVelocity = addVelocity = 1f;
            minDistance = addDistance = 1f;
            emissionOffsetBase = emissionOffsetAdd = 0f;
        }

		@Override
        public SpriteAPI getSprite() { //graphics/portraits/characters/sebestyen.png
            return particleengine.Utils.getLoadedSprite("graphics/fx/particlealpha64sq.png");
        }
		
        public InReC_TrailEmitter life(float minLife, float maxLife) {
            this.minLife = minLife;
            this.maxLife = maxLife;
            return this;
        }
        
        public InReC_TrailEmitter size(float minSize, float maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            return this;
        }
        
        public InReC_TrailEmitter color(float r, float g, float b, float a) {
            color[0] = r;
            color[1] = g;
            color[2] = b;
            color[3] = a;
            return this;
        }
        
        public InReC_TrailEmitter distance(float minDistance, float addDistance) {
            this.minDistance = minDistance;
            this.addDistance = addDistance;
            return this;
        }
        
        public InReC_TrailEmitter velocity(float minVelocity, float addVelocity) {
            this.minVelocity = minVelocity;
            this.addVelocity = addVelocity;
            return this;
        }
        
        public InReC_TrailEmitter emissionOffset(float emissionOffsetBase, float emissionOffsetAdd) {
            this.emissionOffsetBase = emissionOffsetBase;
            this.emissionOffsetAdd = emissionOffsetAdd;
            return this;
        }
        
        @Override
        public Vector2f getLocation() {
            return location;
        }
        
        @Override
        protected ParticleData initParticle(int i) {
            ParticleData data = new ParticleData();

            // Life uniformly random between minLife and maxLife
            float life = MathUtils.getRandomNumberInRange(minLife, maxLife);
            data.life(life).fadeTime(0f, life);
            
            // velocity is random within the defined range
            float theta = angle + (emissionOffsetBase + MathUtils.getRandomNumberInRange(0, emissionOffsetAdd));
            Vector2f vel = MathUtils.getPointOnCircumference(parentVel, minVelocity + MathUtils.getRandomNumberInRange(0f, addVelocity), theta);
            
            Vector2f pt = MathUtils.getPointOnCircumference(MathUtils.getPointOnCircumference(null, minDistance + (MathUtils.getRandomNumberInRange(0f, addDistance)), angle), trailNudge, theta);
            
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