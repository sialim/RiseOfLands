package me.sialim.riseoflands.culture;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReligionCooldownAdapter implements JsonSerializer<ReligionCooldown>, JsonDeserializer<ReligionCooldown> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RTrait.class, new RTraitAdapter())  // Register the RTrait adapter
            .create();

    @Override
    public JsonElement serialize(ReligionCooldown src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Serialize each field in ReligionCooldown
        jsonObject.addProperty("cultureCreateTime", src.getCultureCreateTime());
        jsonObject.addProperty("cultureJoinTime", src.getCultureJoinTime());
        jsonObject.addProperty("cultureLeaveTime", src.getCultureLeaveTime());
        jsonObject.addProperty("cultureDeleteTime", src.getCultureDeleteTime());
        jsonObject.addProperty("forgivenessTimer", src.getForgivenessTimer());
        jsonObject.addProperty("reputationReset", src.getReputationReset());

        // Serialize brokenTraits as a JSON array using toMap() method
        JsonArray brokenTraitsArray = new JsonArray();
        for (RTrait trait : src.getBrokenTraits()) {
            Map<String, Object> traitMap = trait.toMap();  // Convert trait to a map
            JsonElement traitJson = gson.toJsonTree(traitMap);  // Serialize the map into JSON
            brokenTraitsArray.add(traitJson);
        }
        jsonObject.add("brokenTraits", brokenTraitsArray);

        // Debugging: Log the current state of brokenTraits
        System.out.println("Serialized brokenTraits: " + src.getBrokenTraits());

        return jsonObject;
    }

    @Override
    public ReligionCooldown deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ReligionCooldown cooldown = new ReligionCooldown();

        // Deserialize each field in ReligionCooldown
        cooldown.setCultureCreateTime(jsonObject.get("cultureCreateTime").getAsLong());
        cooldown.setCultureJoinTime(jsonObject.get("cultureJoinTime").getAsLong());
        cooldown.setCultureLeaveTime(jsonObject.get("cultureLeaveTime").getAsLong());
        cooldown.setCultureDeleteTime(jsonObject.get("cultureDeleteTime").getAsLong());
        cooldown.setForgivenessTimer(jsonObject.get("forgivenessTimer").getAsLong());
        cooldown.setReputationReset(jsonObject.get("reputationReset").getAsBoolean());

        // Deserialize brokenTraits from a JSON array using TypeToken
        JsonArray brokenTraitsArray = jsonObject.getAsJsonArray("brokenTraits");
        Set<RTrait> brokenTraits = new HashSet<>();

        for (JsonElement traitElement : brokenTraitsArray) {
            Map<String, Object> traitMap = gson.fromJson(traitElement, Map.class);  // Deserialize map
            RTrait trait = RTrait.fromMap(traitMap);  // Convert map back to RTrait

            // Only add the trait if it hasn't been added already (i.e., no duplicates)
            if (!brokenTraits.contains(trait)) {
                brokenTraits.add(trait);
                System.out.println("Added trait: " + trait.getName() + ". Current brokenTraits: " + brokenTraits);
            } else {
                System.out.println("Skipped duplicate trait: " + trait.getName());
            }
        }

        cooldown.setBrokenTraits(brokenTraits);

        // Debugging: Log the current state of brokenTraits
        System.out.println("Deserialized brokenTraits: " + brokenTraits);

        return cooldown;
    }
}
