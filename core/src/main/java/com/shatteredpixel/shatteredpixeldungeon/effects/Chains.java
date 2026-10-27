
/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Chaos Forge - Enhanced Chains Effect
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class Chains extends Group {

	private static final double DEGREES_PER_RADIAN = 180.0 / Math.PI;

	private static final float BASE_DURATION = 0.2f;
	private static final float SPEED = 320f;
	private static final float LINK_SPACING = 6f;

	private float elapsed = 0f;
	private final float duration;

	private Callback callback;

	private final Image[] chains;
	private final PointF from;
	private final PointF to;

	private final float distance;
	private final float rotation;

	public Chains(int from, int to, Effects.Type type, Callback callback) {
		this(
				DungeonTilemap.tileCenterToWorld(from),
				DungeonTilemap.tileCenterToWorld(to),
				type,
				callback
		);
	}

	public Chains(PointF from, PointF to, Effects.Type type, Callback callback) {
		super();

		this.from = new PointF(from.x, from.y);
		this.to = new PointF(to.x, to.y);
		this.callback = callback;

		float dx = this.to.x - this.from.x;
		float dy = this.to.y - this.from.y;

		distance = (float) Math.hypot(dx, dy);
		duration = distance / SPEED + BASE_DURATION;

		rotation = (float) (Math.atan2(dy, dx) * DEGREES_PER_RADIAN) + 90f;

		int linkCount = Math.max(
				1,
				Math.round(distance / LINK_SPACING) + 1
		);

		chains = new Image[linkCount];

		for (int i = 0; i < chains.length; i++) {
			Image link = new Image(Effects.get(type));

			link.angle = rotation;
			link.origin.set(link.width() / 2f, link.height());

			chains[i] = link;
			add(link);
		}
	}

	@Override
	public void update() {
		super.update();

		if (elapsed > duration) {
			return;
		}

		elapsed += Game.elapsed;

		if (elapsed >= duration) {
			killAndErase();

			if (callback != null) {
				Callback finished = callback;
				callback = null;
				finished.call();
			}

			return;
		}

		float progress = Math.min(1f, elapsed / duration);

		float dx = to.x - from.x;
		float dy = to.y - from.y;

		for (int i = 0; i < chains.length; i++) {
			float linkProgress = i / (float) chains.length;
			float travel = linkProgress * progress;

			float x = from.x + dx * travel;
			float y = from.y + dy * travel;

			Image link = chains[i];
			link.center(new PointF(x, y));

			// Subtle ghostly flicker.
			float flicker = (float) Math.sin(elapsed * 16f - i * 0.7f);
			link.alpha(0.82f + 0.18f * flicker);
		}
	}
}
