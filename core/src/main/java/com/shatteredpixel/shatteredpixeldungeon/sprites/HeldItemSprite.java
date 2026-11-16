
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.PointF;

/**
 * Displays an item beside the hero.
 * The item follows the hero and rotates toward the aiming target.
 */
public class HeldItemSprite extends ItemSprite {

    private final HeroSprite hero;

    private float radius = 12f;
    private float direction = -(float) Math.PI / 2f;

    private float targetX;
    private float targetY;

    private boolean hasTarget = false;

    public HeldItemSprite(HeroSprite hero, Item item) {
        super(item);

        if (hero == null) {
            throw new IllegalArgumentException(
                    "HeroSprite cannot be null"
            );
        }

        this.hero = hero;

        // Set the rotation origin to the center of the item.
        originToCenter();

        updatePosition();
    }

    /**
     * Aim the item toward a screen position.
     */
    public void aimAt(PointF target) {
        if (target == null) {
            return;
        }

        aimAt(target.x, target.y);
    }

    /**
     * Aim the item toward screen coordinates.
     */
    public void aimAt(float x, float y) {
        targetX = x;
        targetY = y;
        hasTarget = true;

        updatePosition();
    }

    /**
     * Set the distance between the item and the hero.
     */
    public void setRadius(float radius) {
        if (Float.isNaN(radius) || Float.isInfinite(radius)) {
            return;
        }

        this.radius = Math.max(0f, radius);
        updatePosition();
    }

    /**
     * Update item position and rotation.
     */
    private void updatePosition() {

        if (hero == null) {
            return;
        }

        // Hero center.
        float heroCenterX = hero.x + hero.width() * 0.5f;
        float heroCenterY = hero.y + hero.height() * 0.5f;

        // Calculate aiming direction.
        if (hasTarget) {

            float dx = targetX - heroCenterX;
            float dy = targetY - heroCenterY;

            if (dx * dx + dy * dy > 0.0001f) {
                direction = (float) Math.atan2(dy, dx);
            }
        }

        // Position the item around the hero.
        float itemCenterX =
                heroCenterX + (float) Math.cos(direction) * radius;

        float itemCenterY =
                heroCenterY + (float) Math.sin(direction) * radius;

        // Apply position.
        point(new PointF(
                itemCenterX - width() * 0.5f,
                itemCenterY - height() * 0.5f
        ));

        // Rotate around the item's center.
        angle = (float) Math.toDegrees(direction);
    }

    @Override
    public void update() {
        super.update();

        // Keep following the hero.
        updatePosition();
    }
}
