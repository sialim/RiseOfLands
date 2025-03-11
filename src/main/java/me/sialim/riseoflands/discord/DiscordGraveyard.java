package me.sialim.riseoflands.discord;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.LocalDate;

public class DiscordGraveyard implements Listener {
    private final DiscordSRV discordSRV;
    private RiseOfLandsMain plugin;

    public DiscordGraveyard(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        discordSRV = DiscordSRV.getPlugin();
    }

    @EventHandler public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        String playerName = (plugin.identityManager.getRoleplayName(p.getUniqueId()) != null) ? plugin.identityManager.getRoleplayName(p.getUniqueId()) : p.getName();

        if (discordSRV.getJda() == null) {
            System.out.println("DiscordSRV JDA is null! The bot might not be connected.");
            return;
        }

        if (discordSRV.getJda().getTextChannelById("1264435880400126075") == null) {
            System.out.println("Error: Bot cannot access the Discord channel!");
            return;
        }

        if (!plugin.identityManager.hasIdentity(p.getUniqueId())) return;

        LocalDate birthDate = plugin.identityManager.getBirthDate(p.getUniqueId());
        String birthdate = plugin.calendar.getFormattedDate(birthDate);

        String worldName = plugin.getConfig().getString("main-world");
        LocalDate deathDate = plugin.calendar.worldDates.get(worldName);
        String deathdate = plugin.calendar.getFormattedDate(deathDate);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Here lies, " + playerName + " (" + p.getName() + ")").setColor(0xFF0000)
                        .setDescription(getDeathCause(p) + " (" + birthdate + " - " + deathdate + ") (Season 0)");

        discordSRV.getJda().getTextChannelById("1264435880400126075").sendMessageEmbeds(embed.build()).queue();
    }

    private String getDeathCause(Player player) {
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null) return "Mysteriously vanished.";

        EntityDamageEvent.DamageCause cause = lastDamage.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            if (player.getKiller() != null) {
                return "Murdered by " + player.getKiller().getName() + ".";
            }

            if (lastDamage instanceof org.bukkit.event.entity.EntityDamageByEntityEvent entityEvent) {
                Entity damager = entityEvent.getDamager();
                if (damager instanceof LivingEntity) {
                    return "Slain by a " + formatEntityName(damager.getType()) + ".";
                }
            }
            return "Struck down by an unknown foe.";
        }

        return switch (cause) {
            case PROJECTILE -> "Pierced by an arrow.";
            case FALL -> "Fell to their death.";
            case FIRE, FIRE_TICK -> "Consumed by flames.";
            case LAVA -> "Perished in molten rock.";
            case DROWNING -> "Drawn into the depths.";
            case STARVATION -> "Starved to death.";
            case VOID -> "Fell into the abyss.";
            case LIGHTNING -> "Smote by the heavens.";
            case SUFFOCATION -> "Crushed beneath the weight of the world.";
            case SUICIDE -> "Chose their own fate.";
            case POISON, MAGIC -> "Fell victim to sorcery most foul.";
            case WITHER -> "Drained of life.";
            case DRAGON_BREATH -> "Burned by a wyvern's breath.";
            default -> "Met their end.";
        };
    }

    private String formatEntityName(EntityType type) {
        return type.name().toLowerCase().replace("_", " ");
    }
}
