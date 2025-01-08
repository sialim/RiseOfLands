package me.sialim.riseoflands.culture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

public class CultureCommandExecutor implements TabExecutor {
    private final CultureManager cultureManager;
    private final Set<String> deleteConfirmations;

    public CultureCommandExecutor(CultureManager cultureManager) {
        this.cultureManager = cultureManager;
        this.deleteConfirmations = new HashSet<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only player can execute this command.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("Please specify a culture name.");
                    return false;
                }
                String createResponse = cultureManager.createCulture(player.getUniqueId().toString(), args[1]);
                player.sendMessage(createResponse);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage("Please specify a culture name.");
                    return false;
                }
                String joinResponse = cultureManager.joinCulture(player.getName(), args[1]);
                player.sendMessage(joinResponse);
                break;
            case "leave":
                String leaveResponse = cultureManager.leaveCulture(player.getName());
                player.sendMessage(leaveResponse);
                break;
            default:
                return false;
        }
        return true;
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (args.length == 1) {
            String playerCulture = cultureManager.getPlayerCulture(player.getUniqueId().toString());
            if (playerCulture == null) {
                player.sendMessage("You are not part of any culture to delete.");
                return;
            }
            Culture culture = cultureManager.getCulture(playerCulture);
            if (!culture.getOwner().equals(player.getUniqueId().toString())) {
                player.sendMessage("You are not the owner of this culture.");
                return;
            }
            deleteConfirmations.add(player.getName());
            player.sendMessage("Type /culture delete confirm to delete your culture.");
        } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            if (!deleteConfirmations.contains(player.getUniqueId().toString())) {
                player.sendMessage("You have not initiated a delete request.");
                return;
            }
            String playerCulture = cultureManager.getPlayerCulture(player.getUniqueId().toString());
            if (playerCulture == null) {
                player.sendMessage("You are not part of any culture to delete.");
                deleteConfirmations.remove(player.getName());
                return;
            }
            String deleteResponse = cultureManager.deleteCulture(player.getUniqueId().toString(), playerCulture);
            player.sendMessage(deleteResponse);
            deleteConfirmations.remove(player.getUniqueId().toString());
        } else {
            player.sendMessage("Invalid subcommand. Use /culture delete or /culture delete confirm.");
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return new ArrayList<>();

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            subcommands.add("create");
            subcommands.add("join");
            subcommands.add("leave");

            String playerCulture = cultureManager.getPlayerCulture(player.getUniqueId().toString());
            if (playerCulture != null) {
                Culture culture = cultureManager.getCulture(playerCulture);
                if (culture.getOwner().equalsIgnoreCase(player.getUniqueId().toString())) {
                    subcommands.add("delete");
                }
            }

            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return cultureManager.getCultureNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (deleteConfirmations.contains(player.getUniqueId().toString())) {
                return Collections.singletonList("confirm").stream()
                        .filter(sub -> sub.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
