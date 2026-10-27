
/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Chaos Forge - Enhanced Blast Particles
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class BlastParticle extends PixelParticle.Shrinking {

	private static final int[] COLORS = {
			0xFF4020, // Hot red
			0xEE7722, // Orange
			0xFFB52E, // Golden sparks
			0xFFE6A0  // Bright core
	};

	private static final float MIN_SPEED = 32f;
	private static final float MAX_SPEED = 72f;
	private static final float MIN_SIZE = 3f;
	private static final float MAX_SIZE = 8f;

	public static final Factory FACTORY = new Factory() {
		@Override
		public void emit(Emitter emitter, int index, float x, float y) {
			((BlastParticle) emitter.recycle(BlastParticle.class))
					.reset(x, y);
		}

		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public BlastParticle() {
		super();

		color(COLORS[1]);
		acc.set(0, 50);
	}

	public void reset(float x, float y) {
		revive();

		this.x = x;
		this.y = y;

		// Randomized lifetime.
		left = lifespan = Random.Float(0.35f, 1.0f);

		// Randomized particle size.
		size = Random.Float(MIN_SIZE, MAX_SIZE);

		// Random fiery color.
		color(COLORS[Random.Int(COLORS.length)]);

		// Sparks spread in different directions.
		float angle = Random.Float(0f, (float) (Math.PI * 2));
		float velocity = Random.Float(MIN_SPEED, MAX_SPEED);
		speed.polar(angle, velocity);

		// Sparks rise and then fall under gravity.
		acc.set(
				Random.Float(-12f, 12f),
				Random.Float(35f, 65f)
		);
	}

	@Override
	public void update() {
		super.update();

		// Bright appearance, followed by a smooth fade.
		float progress = lifespan > 0f ? left / lifespan : 0f;
		progress = Math.max(0f, Math.min(1f, progress));

		am = progress > 0.75f
				? (1f - progress) * 4f
				: progress / 0.75f;
	}
}
