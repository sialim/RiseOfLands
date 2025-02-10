package me.sialim.riseoflands.culture;

import com.google.gson.*;
import me.sialim.riseoflands.culture.traits.*;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Type;
import java.util.Map;

public class RTraitAdapter implements JsonDeserializer<RTrait>, JsonSerializer<RTrait> {
    @Override
    public RTrait deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Check if the traitName is present and not null
        if (jsonObject.has("traitName") && !jsonObject.get("traitName").isJsonNull()) {
            String traitName = jsonObject.get("traitName").getAsString();

            // Switch case for different trait types
            switch (traitName) {
                case "Carnivore" -> {
                    return new CarnivoreCTrait(); // No additional fields required
                }
                case "Passive Animals" -> {
                    return new PassiveAnimalsCTrait(); // No additional fields required
                }
                // Handling HolyMobRTrait subclasses (like Hostile Mob)
                case "Hostile Mob" -> {
                    if (jsonObject.has("mobType")) {
                        EntityType mobType = EntityType.valueOf(jsonObject.get("mobType").getAsString());
                        return new HostileMobCTrait(mobType);
                    } else {
                        throw new JsonParseException("Missing mobType for Hostile Mob trait.");
                    }
                }
                case "Neutral Mob" -> {
                    if (jsonObject.has("mobType")) {
                        EntityType mobType = EntityType.valueOf(jsonObject.get("mobType").getAsString());
                        return new NeutralMobCTrait(mobType);
                    } else {
                        throw new JsonParseException("Missing mobType for Neutral Mob trait.");
                    }
                }
                case "Passive Mob" -> {
                    if (jsonObject.has("mobType")) {
                        EntityType mobType = EntityType.valueOf(jsonObject.get("mobType").getAsString());
                        return new PassiveMobCTrait(mobType);
                    } else {
                        throw new JsonParseException("Missing mobType for Passive Mob trait.");
                    }
                }
                case "Cannibal" -> {
                    return new CannibalCTrait();
                }
                case "Earth" -> {
                    return new EarthCTrait();
                }
                case "Kosher" -> {
                    return new KosherCTrait();
                }
                case "No Magic" -> {
                    return new MagicCTrait();
                }
                case "No Marriage" -> {
                    return new MarriageCTrait();
                }
                case "Nature" -> {
                    return new NatureCTrait();
                }
                case "Pacifism to Mobs" -> {
                    return new PacifismMobCTrait();
                }
                case "Pacifism to Players" -> {
                    return new PacifismPlayerCTrait();
                }
                case "Pacifism to Players & Mobs" -> {
                    return new PacifismPlayerMobCTrait();
                }
                case "Pescatarian" -> {
                    return new PescatarianCTrait();
                }
                case "Chaste" -> {
                    return new ProcreateBeforeMarriageCTrait();
                }
                case "No Redstone" -> {
                    return new RedstoneCTrait();
                }
                case "Silence" -> {
                    return new SilenceCTrait();
                }
                case "Sweet Tooth" -> {
                    return new SweetToothCTrait();
                }
                case "No Taming" -> {
                    return new TamingCTrait();
                }
                case "True Carnivore" -> {
                    return new TrueCarnivoreCTrait();
                }
                case "Vegan/Herbivore" -> {
                    return new VeganHerbivoreCTrait();
                }
                default -> throw new JsonParseException("Unknown trait class: " + traitName);
            }
        } else {
            throw new JsonParseException("Trait name is missing or null in the JSON.");
        }
    }

    @Override
    public JsonElement serialize(RTrait src, Type typeOfSrc, JsonSerializationContext context) {
        // Use toMap() method to serialize
        Map<String, Object> traitMap = src.toMap();
        JsonObject jsonObject = new JsonObject();

        // Convert the map to JSON fields
        for (Map.Entry<String, Object> entry : traitMap.entrySet()) {
            jsonObject.add(entry.getKey(), context.serialize(entry.getValue()));
        }

        return jsonObject;
    }
}
