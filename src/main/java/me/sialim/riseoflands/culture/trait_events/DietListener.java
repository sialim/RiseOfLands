package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.*;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DietListener implements Listener {

    public RiseOfLands plugin;

    public DietListener(RiseOfLands plugin) { this.plugin = plugin; }
    @EventHandler public void onPlayerEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Material consumedFood = e.getItem().getType();
        ReligionManager rm = plugin.religionManager;
        Religion playerReligion = rm.getPlayerCulture(uuid);
        if (playerReligion == null) return;
        List<RTrait> religionTraits = playerReligion.getTraits();
        for (RTrait trait : religionTraits) {
            if (isDietTrait(trait)) {
                DietRTrait dietTrait = (DietRTrait) trait;
                for (Material food : dietTrait.restricted) {
                    if (consumedFood == food) {
                        rm.handleTraitViolation(uuid, dietTrait, dietTrait.getPoints());
                        /*
                        ReligionCooldown cooldown = rm.cooldowns.getOrDefault(uuid, new ReligionCooldown());
                        long currentTime = System.currentTimeMillis();
                        cooldown.setForgivenessTimer(currentTime);

                        Set<RTrait> brokenTraits = cooldown.getBrokenTraits();
                        if (brokenTraits == null) {
                            brokenTraits = new HashSet<>();  // Initialize a new set if null
                            cooldown.setBrokenTraits(brokenTraits);  // Ensure this change is saved back to cooldown
                        }
                        if (!brokenTraits.contains(trait)) {
                            brokenTraits.add(trait);
                            // Deduct reputation points
                            int repLoss = trait.getPoints();
                            int currentRep = rpm.getPlayerReputation(uuid);
                            rpm.setPlayerReputation(uuid, currentRep - repLoss);
                            currentRep = rpm.getPlayerReputation(uuid);
                            player.sendMessage("You violated a " + dietTrait.getName() +
                                    " tradition! You lost " + repLoss + " reputation. You now have " +
                                    currentRep + " reputation points.");
                            rm.saveCooldownsToFile();
                        }
                        rm.cooldowns.put(uuid, cooldown);
                        */
                    }
                }
            }
        }
    }

    private boolean isDietTrait(RTrait trait) {
        return trait instanceof DietRTrait;
    }
}
