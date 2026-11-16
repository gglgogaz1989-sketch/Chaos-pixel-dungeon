package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.PointF;

/**
 * Displays an item next to the hero and rotates it
 * around the hero toward the selected target.
 */
public class HeldItemSprite extends ItemSprite {

    private final HeroSprite hero;

    private float radius = 11f;

    private float targetX;
    private float targetY;

    private float direction = -(float) Math.PI / 2f;

    private boolean hasTarget = false;

    public HeldItemSprite(HeroSprite hero, Item item) {
        super(item);

        if (hero == null) {
            throw new IllegalArgumentException("HeroSprite cannot be null");
        }

        this.hero = hero;

        // Rotate the item around its own center.
        originToCenter();

        updatePosition();
    }

    /**
     * Sets the target position in screen coordinates.
     */
    public void aimAt(PointF target) {
        if (target == null) {
            return;
        }

        aimAt(target.x, target.y);
    }

    /**
     * Sets the target position in screen coordinates.
     */
    public void aimAt(float x, float y) {
        targetX = x;
        targetY = y;

        hasTarget = true;

        updatePosition();
    }

    /**
     * Changes the distance between the item and the hero.
     */
    public void setRadius(float radius) {
        if (Float.isNaN(radius) || Float.isInfinite(radius)) {
            return;
        }

        this.radius = Math.max(0f, radius);

        updatePosition();
    }

    /**
     * Updates the position and rotation of the item.
     */
    private void updatePosition() {

        // Center of the hero.
        float heroCenterX = hero.x + hero.width() * 0.5f;
        float heroCenterY = hero.y + hero.height() * 0.5f;

        if (hasTarget) {

            float dx = targetX - heroCenterX;
            float dy = targetY - heroCenterY;

            // Avoid unstable direction when target equals hero center.
            if (dx * dx + dy * dy > 0.0001f) {
                direction = (float) Math.atan2(dy, dx);
            }
        }

        // Position the item around the hero.
        float itemCenterX =
                heroCenterX + (float) Math.cos(direction) * radius;

        float itemCenterY =
                heroCenterY + (float) Math.sin(direction) * radius;

        point(
                itemCenterX - width() * 0.5f,
                itemCenterY - height() * 0.5f
        );

        // Rotate the item toward the target.
        angle = (float) Math.toDegrees(direction);
    }

    @Override
    public void update() {
        super.update();

        // Follow the hero even when the hero moves.
        updatePosition();
    }
}
