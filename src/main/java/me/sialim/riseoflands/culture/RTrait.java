package me.sialim.riseoflands.culture;

import me.sialim.riseoflands.culture.traits.*;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Objects;

public abstract class RTrait {
    protected String name;
    protected int points;

    public RTrait(String name, int points) {
        this.name = name;
        this.points = points;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RTrait other = (RTrait) obj;
        return Objects.equals(this.getName(), other.getName());  // Compare by trait name or a unique identifier
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);  // Use trait name for hash code generation
    }

    public abstract Map<String, Object> toMap();

    public static RTrait fromMap(Map<String, Object> map) {
        String traitName = (String) map.get("traitName");

        // Dynamically instantiate traits based on className
        switch (traitName) {
            case "Hostile Mob":
                EntityType mobType = EntityType.valueOf((String) map.get("mobType"));
                return new HostileMobCTrait(mobType);
            case "Neutral Mob":
                mobType = EntityType.valueOf((String) map.get("mobType"));
                return new NeutralMobCTrait(mobType);
            case "Passive Mob":
                mobType = EntityType.valueOf((String) map.get("mobType"));
                return new PassiveMobCTrait(mobType);
            case "Cannibal":
                return new CannibalCTrait();
            case "Carnivore":
                return new CarnivoreCTrait();
            case "Earth":
                return new EarthCTrait();
            case "Kosher":
                return new KosherCTrait();
            case "No Magic":
                return new MagicCTrait();
            case "No Marriage":
                return new MarriageCTrait();
            case "Nature":
                return new NatureCTrait();
            case "Pacifism to Mobs":
                return new PacifismMobCTrait();
            case "Pacifism to Players":
                return new PacifismPlayerCTrait();
            case "Pacifism to Players & Mobs":
                return new PacifismPlayerMobCTrait();
            case "Passive Animals":
                return new PassiveAnimalsCTrait();
            case "Pescatarian":
                return new PescatarianCTrait();
            case "No Procreation before Marriage":
                return new ProcreateBeforeMarriageCTrait();
            case "No Redstone":
                return new RedstoneCTrait();
            case "Silence":
                return new SilenceCTrait();
            case "Sweet Tooth":
                return new SweetToothCTrait();
            case "No Taming":
                return new TamingCTrait();
            case "True Carnivore":
                return new TrueCarnivoreCTrait();
            case "Vegan/Herbivore":
                return new VeganHerbivoreCTrait();
            default:
                throw new IllegalArgumentException("Unknown trait class: " + traitName);
        }
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
}
