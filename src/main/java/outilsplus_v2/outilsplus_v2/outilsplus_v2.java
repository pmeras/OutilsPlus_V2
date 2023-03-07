package outilsplus_v2.outilsplus_v2;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class outilsplus_v2 extends JavaPlugin implements Listener, CommandExecutor
{
    private final int MAX_BLOCKS_TO_MINE = 64;
    private NamespacedKey tagTimberAxe = new NamespacedKey(this, "timberAxe");
    private NamespacedKey tagVeinMiner = new NamespacedKey(this, "veinMiner");
    private NamespacedKey tagHoueFarmer = new NamespacedKey(this, "houeFarmer");
    private List<String[]> blocksVeinMiner = new ArrayList<>();
    private List<String[]> blocksHoue = new ArrayList<>();
    private List<String[]> blocksHache = new ArrayList<>();

    @Override
    public void onEnable() {
        initOutils();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("outilsplus_v2 enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("outilsplus_v2 disabled!");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            if (command.getName().equalsIgnoreCase("veinminer"))
            {
                Player player = (Player) sender;

                // Create a new ItemStack with the Material of your choice
                ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);

                // Set the display name of the ItemStack
                ItemMeta meta = itemStack.getItemMeta();

                // Add your custom NBT tag to the ItemStack
                meta.setDisplayName("Pioche du Mineur");
                meta.getPersistentDataContainer().set(tagVeinMiner, PersistentDataType.STRING, "veinMiner");
                itemStack.setItemMeta(meta);

                // Give the ItemStack to the player
                player.getInventory().addItem(itemStack);

                return true;
            }
            else if (command.getName().equalsIgnoreCase("timberaxe"))
            {
                Player player = (Player) sender;

                // Create a new ItemStack with the Material of your choice
                ItemStack itemStack = new ItemStack(Material.DIAMOND_AXE);

                // Set the display name of the ItemStack
                ItemMeta meta = itemStack.getItemMeta();

                // Add your custom NBT tag to the ItemStack
                meta.setDisplayName("Hache du Bûcheron");
                meta.getPersistentDataContainer().set(tagTimberAxe, PersistentDataType.STRING, "timberaxe");
                itemStack.setItemMeta(meta);

                // Give the ItemStack to the player
                player.getInventory().addItem(itemStack);

                return true;
            }
            else if (command.getName().equalsIgnoreCase("houefarmer"))
            {
                Player player = (Player) sender;

                // Create a new ItemStack with the Material of your choice
                ItemStack itemStack = new ItemStack(Material.DIAMOND_HOE);

                // Set the display name of the ItemStack
                ItemMeta meta = itemStack.getItemMeta();

                // Add your custom NBT tag to the ItemStack
                meta.setDisplayName("Houe du Fermier");
                meta.getPersistentDataContainer().set(tagHoueFarmer, PersistentDataType.STRING, "houefarmer");
                itemStack.setItemMeta(meta);

                // Give the ItemStack to the player
                player.getInventory().addItem(itemStack);

                return true;
            }
        }
        return false;
    }

    private boolean hasCustomTag(Player player, NamespacedKey tag)
    {
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (meta == null)
        {
            return false;
        }
        return meta.getPersistentDataContainer().has(tag, PersistentDataType.STRING);
    }

    @EventHandler
    public void onBlockInteract(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (hasCustomTag(player, tagVeinMiner))
        {
            for(int i = 1; i < blocksVeinMiner.size(); i++)
            {
                if (blocksVeinMiner.get(i)[1].equals(block.getType().name().toLowerCase()))
                {
                    ArrayList<Block> blocksToCheck = new ArrayList<>();
                    getVein(block, block.getType(), blocksToCheck);
                    int blocksMined = 0;
                    for (Block b : blocksToCheck)
                    {
                        if (blocksMined >= MAX_BLOCKS_TO_MINE)
                        {
                            break;
                        }
                        b.breakNaturally(player.getInventory().getItemInMainHand());
                        blocksMined++;
                    }
                }
            }
        }
        else if(hasCustomTag(player, tagTimberAxe))
        {
            for(int i = 1; i < blocksHache.size(); i++)
            {
                if (blocksHache.get(i)[1].equals(block.getType().name().toLowerCase()))
                {
                    ArrayList<Block> blocksToCheck = new ArrayList<>();
                    getVein(block, block.getType(), blocksToCheck);
                    int blocksMined = 0;
                    for (Block b : blocksToCheck)
                    {
                        if (blocksMined >= MAX_BLOCKS_TO_MINE)
                        {
                            break;
                        }
                        b.breakNaturally(player.getInventory().getItemInMainHand());
                        blocksMined++;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Block baseCenterBlock = event.getClickedBlock();

        for(int i = 1; i < blocksHoue.size(); i++)
        {
            if(hasCustomTag(player, tagHoueFarmer))
            {
                if (blocksHoue.get(i)[1].equals(baseCenterBlock.getType().name().toLowerCase()))
                {
                    new BukkitRunnable() {
                        @Override
                        public void run()
                        {
                            for (int xOffset = -1; xOffset <= 1; xOffset++)
                            {
                                for (int zOffset = -1; zOffset <= 1; zOffset++)
                                {
                                    for(int i = 1; i < blocksHoue.size(); i++)
                                    {
                                        Block adjacentBlock = baseCenterBlock.getRelative(xOffset, 0, zOffset);
                                        if(adjacentBlock.getType().name().toLowerCase().equals(blocksHoue.get(i)[1]))
                                            adjacentBlock.setType(baseCenterBlock.getType());
                                    }
                                }
                            }
                        }
                    }.runTaskLater(this, 1L);
                }
            }
        }
    }

    private void getVein(Block block, Material material, ArrayList<Block> blocks) {
        int MAX_BLOCKS_TO_CHECK = 100;
        Queue<Block> blocksToCheck = new LinkedList<Block>();
        blocksToCheck.add(block);

        while (!blocksToCheck.isEmpty() && blocks.size() < MAX_BLOCKS_TO_CHECK) {
            Block currentBlock = blocksToCheck.poll();
            if (currentBlock.getType() == material && !blocks.contains(currentBlock)) {
                blocks.add(currentBlock);
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block relative = currentBlock.getRelative(x, y, z);
                            if (relative.getType() == material && !blocks.contains(relative)) {
                                blocksToCheck.add(relative);
                            }
                        }
                    }
                }
            }
        }
    }

    public void initOutils()
    {
        try
        {
            List<String> tmp_blocksVeinMiner = Files.readAllLines(Paths.get(System.getProperty("user.dir"),"/plugins/config/blocksVeinMiner.txt").toAbsolutePath());
            List<String> tmp_blocksHoue = Files.readAllLines(Paths.get(System.getProperty("user.dir"),"/plugins/config/blocksHoue.txt").toAbsolutePath());
            List<String> tmp_blocksHache = Files.readAllLines(Paths.get(System.getProperty("user.dir"),"/plugins/config/blocksHache.txt").toAbsolutePath());

            for(int j = 0; j < tmp_blocksVeinMiner.size(); j++)
                blocksVeinMiner.add(j, tmp_blocksVeinMiner.get(j).split(";"));

            for(int j = 0; j < tmp_blocksHoue.size(); j++)
                blocksHoue.add(j, tmp_blocksHoue.get(j).split(";"));

            for(int j = 0; j < tmp_blocksHache.size(); j++)
                blocksHache.add(j, tmp_blocksHache.get(j).split(";"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
