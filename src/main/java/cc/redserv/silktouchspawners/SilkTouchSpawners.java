package cc.redserv.silktouchspawners;

import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagString;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SilkTouchSpawners extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this,this);
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

        ItemStack itemSpawner = setType(new ItemStack(Material.SPAWNER), ((CreatureSpawner)event.getBlock().getState()).getSpawnedType());

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
        spawner.update();
    }










    public static EntityType getType(org.bukkit.inventory.ItemStack itemStack) {
        net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nmsItemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        if (nmsItemCompound == null) {
            return null;
        }
        if (nmsItemCompound.isEmpty())
            return null;

        String mob = nmsItemCompound.getString("spawner_type");

        EntityType entity = null;
        try {
            entity = EntityType.valueOf(mob);
        } catch (Exception ignore) {}

        return entity;
    }

    public static org.bukkit.inventory.ItemStack setType(org.bukkit.inventory.ItemStack itemStack, org.bukkit.entity.EntityType type) {
        net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nmsItemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        if (nmsItemCompound == null) {
            return null;
        }
        nmsItemCompound.set("spawner_type", NBTTagString.a(type.name()));
        nmsItem.setTag(nmsItemCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

}
