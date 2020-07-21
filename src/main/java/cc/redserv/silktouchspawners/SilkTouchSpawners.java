package cc.redserv.silktouchspawners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
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

        world.dropItemNaturally(loc, new ItemStack(Material.SPAWNER));
    }

}
