package com.rogue.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rogue.game.screens.GameScreen;

import java.io.Serializable;

public abstract class BaseUnit implements Serializable {
    public enum Type {
        /*Knight("knight"),*/ Player("p1Walk", "p1Jump"), Bear("bear");
        //

        private String texture;
        private String jumpTexture;

        public String getTexture() {
            return texture;
        }

        public String getJumpTexture() {
            return jumpTexture;
        }

        Type(String texture) {
            this.texture = texture;
            this.jumpTexture = null;
        }

        Type(String texture, String jumpTexture) {
            this.texture = texture;
            this.jumpTexture = jumpTexture;
        }
    }

    protected transient GameScreen gameScreen;
    protected Map map;
    protected transient TextureRegion[] regions;
    protected transient TextureRegion jumpRegion;
    protected Vector2 velocity;
    protected float animationTime;
    protected boolean right;
    protected int maxHp;
    protected int hp;
    protected Rectangle hitArea;
    protected int width;
    protected int height;
    protected float firePressTimer;
    protected float timeBetweenFire;
    protected float speed;
    protected float reddish;
    protected Type type;
    protected transient TextureRegion moveRegion;

    public float getCenterX() {
        return hitArea.x + hitArea.width / 2;
    }

    public float getCenterY() {
        return hitArea.y + hitArea.height / 2;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Rectangle getHitArea() {
        return hitArea;
    }

    public BaseUnit(GameScreen gameScreen, Map map, int maxHp, float speed, float timeBetweenFire, float x, float y, int width, int height) {
        this.gameScreen = gameScreen;
        this.map = map;
        this.velocity = new Vector2(0, 0);
        this.width = width;
        this.height = height;
        this.right = true;
        this.maxHp = maxHp;
        this.speed = speed;
        this.hp = this.maxHp;
        this.hitArea = new Rectangle(x, y, width / 2, (int) (height / 1.2));
        this.timeBetweenFire = timeBetweenFire;
        this.reddish = 0.0f;
    }

    public void afterLoad(GameScreen gameScreen) {
        TextureRegion tr = Assets.getInstance().getAtlas().findRegion(type.getTexture());
        this.regions = tr.split(width, height)[0];

        if(type.getJumpTexture() != null) {
            this.jumpRegion = Assets.getInstance().getAtlas().findRegion(type.getJumpTexture());
        } else {
            this.jumpRegion = this.regions[0];
        }

        this.gameScreen = gameScreen;
    }

    public void update(float dt) {
        velocity.add(0, -600.0f * dt);
        velocity.x *= 0.6f;
        float len = velocity.len() * dt;
        float dx = velocity.x * dt / len;
        float dy = velocity.y * dt / len;
        for (int i = 0; i < len; i++) {
            hitArea.y += dy;
            if (checkCollision()) {
                hitArea.y -= dy;
                velocity.y = 0.0f;
            }
            hitArea.x += dx;
            if (checkCollision()) {
                hitArea.x -= dx;
                velocity.x = 0.0f;
            }
        }
        if (Math.abs(velocity.x) > 1.0f) {
            if (Math.abs(velocity.y) < 1.0f) {
                animationTime += (Math.abs(velocity.x) / 800.0f);
            }
        } else {
            animationTime = 0;
        }
        if (reddish > 0.0f) {
            reddish -= dt / 2;
            if (reddish < 0.0f) {
                reddish = 0.0f;
            }
        }
    }

    public void setBlock() {
        map.blockCell(this);
    }

    public void moveLeft() {
        velocity.x = -speed;
        right = false;
    }

    public void moveRight() {
        velocity.x = speed;
        right = true;
    }

    public void fire(float dt, boolean isPlayer) {
        firePressTimer += dt;
        if (firePressTimer > timeBetweenFire) {
            firePressTimer -= timeBetweenFire;
            float bulletVelX = 600.0f;
            if (!right) bulletVelX *= -1;
            gameScreen.getBulletEmitter().setup(isPlayer, getCenterX(), getCenterY(), bulletVelX, 0);
        }
    }

    public void jump() {
        hitArea.y--;
        if (Math.abs(velocity.y) < 0.1f && checkCollision()) {
            velocity.y = 400.0f;
        }
        hitArea.y++;
    }

    public boolean takeDamage(int dmg) {
        hp -= dmg;
        reddish += 0.5f;
        if (reddish > 1.0f) {
            reddish = 1.0f;
        }
        if (hp <= 0) {
            destroy();
            return true;
        }
        return false;
    }

    public abstract void destroy();

    public boolean checkCollision() {
        final int parts = 4;
        float dx = hitArea.width / parts;
        float dy = hitArea.height / parts;
        for (int i = 0; i <= parts; i++) {
            if (!map.checkSpaceIsEmpty(hitArea.x + i * dx, hitArea.y) || !map.checkSpaceIsEmpty(hitArea.x + i * dx, hitArea.y + hitArea.height) || !map.checkSpaceIsEmpty(hitArea.x, hitArea.y + i * dy) || !map.checkSpaceIsEmpty(hitArea.x + hitArea.width, hitArea.y + i * dy)) {
                return true;
            }
        }
        return false;
    }

    public void render(SpriteBatch batch) {
        int frameIndex = getCurrentFrame();

        moveRegion = velocity.y != 0 ? jumpRegion : regions[frameIndex];


        if (!right && !moveRegion.isFlipX()) {
            moveRegion.flip(true, false);
        }
        if (right &&  moveRegion.isFlipX()) {
            moveRegion.flip(true, false);
        }
        batch.setColor(1.0f, 1.0f - reddish, 1.0f - reddish, 1.0f);

        batch.draw(moveRegion, hitArea.x - (width - hitArea.width) / 2, hitArea.y - (height - hitArea.height) / 2);
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public int getCurrentFrame() {
        return (int) animationTime % regions.length;
    }
}
