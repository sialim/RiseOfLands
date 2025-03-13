package me.sialim.riseoflands.buildershop;

import me.sialim.riseoflands.RiseOfLandsMain;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BuilderShop implements CommandExecutor, Listener {
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, Integer> stackSelection = new HashMap<>();
    private final Map<Material, Double> itemPrices = new LinkedHashMap<>();
    private Economy economy;
    private RiseOfLandsMain plugin;

    public BuilderShop(RiseOfLandsMain plugin) {
        this.plugin = plugin;
    }

    private Economy getEconomy() {
        if (economy == null) {
            economy = plugin.econ;
        }
        return economy;
    }


    public void loadShopConfig() {
        itemPrices.clear();

        for (String key : plugin.getConfig().getConfigurationSection("buildershop").getKeys(false)) {
            Material material = Material.matchMaterial(key.toUpperCase());
            if (material != null) {
                itemPrices.put(material, plugin.getConfig().getDouble("buildershop." + key));
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (sender instanceof Player p && p.isOp()) {
            int page = playerPages.getOrDefault(p.getUniqueId(), 0);
            p.openInventory(createShopMenu(p, page));
            return true;
        }

        return false;
    }

    public Inventory createShopMenu(Player p, int page) {
        Inventory shopGui = Bukkit.createInventory(null, 36, ChatColor.LIGHT_PURPLE + "Imperia II Builder Shop - Page " + (page + 1));

        List<Material> materials = new ArrayList<>(itemPrices.keySet());
        int maxItemsPerPage = 21;
        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, materials.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = materials.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + material.name());
                meta.setLore(Collections.singletonList(ChatColor.GREEN + "$" + itemPrices.get(material) + ChatColor.GRAY + "/stack"));
                item.setItemMeta(meta);
            }
            shopGui.setItem(slot++, item);
        }

        if (page > 0) {
            shopGui.setItem(27, createButton(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }
        if (endIndex < materials.size()) {
            shopGui.setItem(35, createButton(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        return shopGui;
    }

    public Inventory createConfirmMenu(Player p, Material material) {
        UUID uuid = p.getUniqueId();
        int selectedStacks = stackSelection.getOrDefault(uuid, 1);
        double basePrice = itemPrices.get(material);
        double baseLabelPrice = basePrice * selectedStacks;
        double multiplier = (-1.0 / 5200) * (selectedStacks * selectedStacks) + 1;
        double adjustedPrice = basePrice * multiplier * selectedStacks;
        double savings = baseLabelPrice - adjustedPrice;
        String newSavings = String.format("%.2f", savings);
        String priceFormula = String.format("$%.2f x %d stacks x %.4f", basePrice, selectedStacks, multiplier);
        String savingsOne = ChatColor.GRAY + "You're saving " + ChatColor.GREEN + "$" + newSavings + ChatColor.GRAY + " by";
        String savingsTwo = ChatColor.GRAY + "purchasing " + ChatColor.YELLOW + selectedStacks + ChatColor.GRAY + " stacks";
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Confirm Purchase");

        ItemStack itemPreview = new ItemStack(material, Math.min(selectedStacks, material.getMaxStackSize()));
        ItemMeta previewMeta = itemPreview.getItemMeta();
        if (previewMeta != null) {
            previewMeta.setDisplayName(ChatColor.YELLOW + material.name());
            itemPreview.setItemMeta(previewMeta);
        }
        ItemStack minusOne = createButton(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-1 Stack");
        ItemStack plusOne = createButton(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+1 Stack");

        ItemStack confirm = createButton(Material.LIME_STAINED_GLASS, ChatColor.GREEN + "Confirm Purchase",
                ChatColor.YELLOW + "Total Price: $" + String.format("%.2f", adjustedPrice),
                ChatColor.GRAY + priceFormula, savingsOne, savingsTwo);

        ItemStack cancel = createButton(Material.RED_STAINED_GLASS, ChatColor.RED + "Cancel");

        gui.setItem(10, minusOne);
        gui.setItem(11, itemPreview);
        gui.setItem(12, plusOne);
        gui.setItem(15, confirm);
        gui.setItem(16, cancel);

        return gui;
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void adjustStackCount(Player player, Material material, boolean increase) {
        UUID uuid = player.getUniqueId();
        int currentStacks = stackSelection.getOrDefault(uuid, 1);
        int maxStacks = getMaxFullStacks(player, material);

        if (increase) {
            if (currentStacks < maxStacks) stackSelection.put(uuid, currentStacks + 1);
        } else {
            if (currentStacks > 1) stackSelection.put(uuid, currentStacks - 1);
        }

        player.openInventory(createConfirmMenu(player, material));
    }

    public void handlePurchase(Player player, Material material) {
        UUID uuid = player.getUniqueId();
        int stacks = stackSelection.getOrDefault(uuid, 1);
        double basePrice = itemPrices.get(material);
        int availableStacks = getMaxFullStacks(player, material);

        if (stacks > availableStacks) {
            player.sendMessage(ChatColor.RED + "Not enough space in your inventory!");
            return;
        }

        double multiplier = (-1.0 / 5200) * (stacks * stacks) + 1;
        double adjustedPrice = basePrice * multiplier * stacks;

        Economy economy = getEconomy();

        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system is not available. Please contact an administrator.");
            return;
        }

        if (!economy.has(player, adjustedPrice)) {
            player.sendMessage(ChatColor.RED + "Not enough money!");
            return;
        }

        economy.withdrawPlayer(player, adjustedPrice);
        for (int i = 0; i < stacks; i++) {
            player.getInventory().addItem(new ItemStack(material, 64));
        }

        player.sendMessage(ChatColor.GREEN + "You bought " + stacks + " stack(s) of " + material.name() + " for $" + String.format("%.2f", adjustedPrice));
        player.closeInventory();
    }

    private int getMaxFullStacks(Player player, Material material) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) emptySlots++;
        }
        return emptySlots;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        UUID uuid = player.getUniqueId();

        if (!ChatColor.stripColor(title).contains("Builder Shop") && !ChatColor.stripColor(title).equals("Confirm Purchase")) return;

        if (ChatColor.stripColor(title).contains("Builder Shop")) {
            event.setCancelled(true);

            if (clickedItem.getType() == Material.ARROW) {
                int page = playerPages.getOrDefault(uuid, 0);
                String buttonName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                if ("Previous Page".equals(buttonName)) {
                    page = Math.max(0, page - 1);
                } else if ("Next Page".equals(buttonName)) {
                    page++;
                }
                playerPages.put(uuid, page);
                player.openInventory(createShopMenu(player, page));
            } else {
                Material material = clickedItem.getType();
                stackSelection.put(uuid, 1);
                player.openInventory(createConfirmMenu(player, material));
            }
        } else if (ChatColor.stripColor(title).equals("Confirm Purchase")) {
            event.setCancelled(true);
            if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE || clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                ItemStack itemInSlot11 = event.getInventory().getItem(11);
                if (itemInSlot11 != null && itemInSlot11.getType() != Material.AIR) {
                    Material material = itemInSlot11.getType();
                    adjustStackCount(player, material, clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE);
                } else {
                    player.sendMessage(ChatColor.RED + "Error: No item selected.");
                    return;
                }
            } else if (clickedItem.getType() == Material.LIME_STAINED_GLASS) {
                ItemStack itemInSlot11 = event.getInventory().getItem(11);
                if (itemInSlot11 != null && itemInSlot11.getType() != Material.AIR) {
                    Material material = itemInSlot11.getType();
                    handlePurchase(player, material);
                }
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS && ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).equals("Cancel")) {
                player.closeInventory();
            }
        }
    }
}
