package me.sialim.riseoflands.culture.trait_events;

import com.comphenix.protocol.PacketType;
import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.*;
import me.sialim.riseoflands.culture.traits.PassiveAnimalsCTrait;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HolyMobListener implements Listener {
    public RiseOfLands plugin;

    public HolyMobListener(RiseOfLands plugin) { this.plugin = plugin; }

    @EventHandler public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        Player player = e.getEntity().getKiller();
        UUID uuid = player.getUniqueId();
        EntityType entityType = e.getEntityType();
        ReligionManager rm = plugin.religionManager;
        ReputationManager rpm = plugin.reputationManager;
        Religion playerReligion = rm.getPlayerCulture(uuid);
        if (playerReligion == null) return;
        List<RTrait> religionTraits = playerReligion.getTraits();
        for (RTrait trait : religionTraits) {
            if (trait instanceof HolyMobRTrait holyMobTrait) {
                if (holyMobTrait.restricted == entityType) {
                    rm.handleTraitViolation(uuid, holyMobTrait, holyMobTrait.getPoints());
                }
            } else if (trait instanceof PassiveAnimalsCTrait passiveAnimalsTrait) {
                for (EntityType restrictedEntity : passiveAnimalsTrait.restricted) {
                    if (restrictedEntity == entityType) {
                        rm.handleTraitViolation(uuid, passiveAnimalsTrait, passiveAnimalsTrait.getPoints());
                    }
                }
            }
        }
    }
}
