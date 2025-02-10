package me.sialim.riseoflands.culture;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ReligionCommandExecutor implements TabExecutor {
    private final ReligionManager religionManager;
    private final Set<UUID> deleteConfirmations;

    public ReligionCommandExecutor(ReligionManager cultureManager) {
        this.religionManager = cultureManager;
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
                String createResponse = religionManager.createCulture(player.getUniqueId(), args[1]);
                player.sendMessage(createResponse);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage("Please specify a culture name.");
                    return false;
                }
                String joinResponse = religionManager.joinCulture(player.getUniqueId(), args[1]);
                player.sendMessage(joinResponse);
                break;
            case "leave":
                String leaveResponse = religionManager.leaveCulture(player.getUniqueId());
                player.sendMessage(leaveResponse);
                break;
            case "delete":
                handleDeleteCommand(player, args);
                break;
            case "forgive":
                String response = "";
                if(!player.isOp()) {
                    player.sendMessage("Insufficient permissions.");
                    return false;
                }
                if (args.length < 2) {
                    response = religionManager.forceForgive(player.getUniqueId());
                } else {
                    Player p = Bukkit.getPlayer(args[1]);
                    if (p != null) {
                        UUID uuid = p.getUniqueId();
                        response = religionManager.forceForgive(uuid);
                    }
                }
                player.sendMessage(response);
                break;
            default:
                return false;
        }
        return true;
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (args.length == 1) {
            Religion playerCulture = religionManager.getPlayerCulture(player.getUniqueId());
            if (playerCulture == null) {
                player.sendMessage("You are not part of any culture to delete.");
                return;
            }
            if (!playerCulture.getOwner().equals(player.getUniqueId())) {
                player.sendMessage("You are not the owner of this culture.");
                return;
            }
            deleteConfirmations.add(player.getUniqueId());
            player.sendMessage("Type '/religion delete confirm' to delete your religion.");
            player.sendMessage("Disclaimer: you and your members will not be able to " +
                    "join/create another religion for 48 hours");
        } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            if (!deleteConfirmations.contains(player.getUniqueId())) {
                player.sendMessage("You have not initiated a delete request.");
                return;
            }
            Religion playerCulture = religionManager.getPlayerCulture(player.getUniqueId());
            if (playerCulture == null) {
                player.sendMessage("You are not part of any culture to delete.");
                deleteConfirmations.remove(player.getUniqueId());
                return;
            }
            String deleteResponse = religionManager.deleteCulture(player.getUniqueId(), playerCulture);
            player.sendMessage(deleteResponse);
            deleteConfirmations.remove(player.getUniqueId());
        } else {
            player.sendMessage("Invalid subcommand. Use /religion delete or /religion delete confirm.");
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

            Religion playerCulture = religionManager.getPlayerCulture(player.getUniqueId());
            if (playerCulture != null) {
                if (playerCulture.getOwner().equals(player.getUniqueId())) {
                    subcommands.add("delete");
                }
                if(player.isOp())
                    subcommands.add("forgive");
            }

            return subcommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return religionManager.getCultureNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (deleteConfirmations.contains(player.getUniqueId())) {
                return Collections.singletonList("confirm").stream()
                        .filter(sub -> sub.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
