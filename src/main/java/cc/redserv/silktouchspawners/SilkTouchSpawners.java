package cc.redserv.silktouchspawners;

import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SilkTouchSpawners extends JavaPlugin implements Listener {

    public static SilkTouchSpawners instance;
    public static boolean MobKeepOnDrop = true;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this,this);
        instance = this;
        saveDefaultConfig();
        MobKeepOnDrop = getConfig().getBoolean("keepTheMobOnBreak");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public static void onBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().equals(Material.SPAWNER))
            return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR))
            return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Map<Enchantment, Integer> enchents = item.getEnchantments();

        if (enchents.isEmpty())
            return;
        AtomicBoolean isHere = new AtomicBoolean(false);
        enchents.forEach((enchantment, integer) -> {
            if (enchantment.equals(Enchantment.SILK_TOUCH))
                isHere.set(true);
        });
        if (!isHere.get())
            return;
        event.setExpToDrop(0);
        event.setDropItems(false);
        Location loc = event.getBlock().getLocation();
        World world = event.getBlock().getWorld();
        ItemStack itemSpawner;
        if (MobKeepOnDrop)
            itemSpawner = setType(new ItemStack(Material.SPAWNER), ((CreatureSpawner)event.getBlock().getState()).getSpawnedType());
        else
            itemSpawner = new ItemStack(Material.SPAWNER);
        world.dropItemNaturally(loc, itemSpawner);
    }


    @EventHandler
    public static void onPlace(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.SPAWNER))
            return;
        EntityType type = null;
        type = getType(event.getItemInHand());
        if (type == null)
            return;

        CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
        spawner.setSpawnedType(type);

        spawner.setMaxNearbyEntities(SilkTouchSpawners.instance.getConfig().getInt("MaxNearbyEntities"));
        spawner.setMaxSpawnDelay(SilkTouchSpawners.instance.getConfig().getInt("MaxSpawnDelay"));
        spawner.setMinSpawnDelay(SilkTouchSpawners.instance.getConfig().getInt("MinSpawnDelay"));
        spawner.setRequiredPlayerRange(SilkTouchSpawners.instance.getConfig().getInt("RequiredPlayerRange"));
        spawner.setSpawnCount(SilkTouchSpawners.instance.getConfig().getInt("SpawnCount"));
        spawner.setSpawnRange(SilkTouchSpawners.instance.getConfig().getInt("SpawnRange"));

        spawner.update();
    }

    public static EntityType getType(org.bukkit.inventory.ItemStack itemStack) {

        ItemStack itemStack2 = itemStack.clone();
        NamespacedKey key = new NamespacedKey(instance, "MobType");
        ItemMeta meta = itemStack2.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING))
            return null;

        String mob = container.get(key, PersistentDataType.STRING);

        EntityType entity = null;
        try {
            entity = EntityType.valueOf(mob);
        } catch (Exception ignore) {}

        return entity;
    }

    public static org.bukkit.inventory.ItemStack setType(org.bukkit.inventory.ItemStack itemStack, org.bukkit.entity.EntityType type) {
        ItemStack itemStack2 = itemStack.clone();
        NamespacedKey key = new NamespacedKey(instance, "MobType");
        ItemMeta meta = itemStack2.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name());
        itemStack2.setItemMeta(meta);

        return itemStack2;
    }

}
