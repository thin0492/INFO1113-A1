package WizardTD;

import processing.core.PImage;

public class MonsterType {
    private String type;
    private float hp;
    private float speed;
    private float armour;
    private float mana_gained_on_kill;
    private PImage sprite;

    public MonsterType(String type, float hp, float speed, float armour, float mana_gained_on_kill, PImage sprite) {
        this.type = type;
        this.hp = hp;
        this.speed = speed;
        this.armour = armour;
        this.mana_gained_on_kill = mana_gained_on_kill;
        this.sprite = sprite;
    }

    public String getType() {
        return type;
    }

    public float getHp() {
        return hp;
    }

    public float getSpeed() {
        return speed;
    }

    public float getArmour() {
        return armour;
    }

    public float getManaGainedOnKill() {
        return mana_gained_on_kill;
    }
    public PImage getSprite() {
        return sprite;
    }
}
