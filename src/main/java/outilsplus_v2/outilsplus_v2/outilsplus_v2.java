package outilsplus_v2.outilsplus_v2;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

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
    private NamespacedKey customTag = new NamespacedKey(this, "veinMiner");
    private List<String[]> blocksVeinMiner = new ArrayList<>();

    @Override
    public void onEnable() {
        initCommande();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("outilsplus_v2 enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("outilsplus_v2 disabled!");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("giveitem")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // Create a new ItemStack with the Material of your choice
                ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);

                // Set the display name of the ItemStack
                ItemMeta meta = itemStack.getItemMeta();

                // Add your custom NBT tag to the ItemStack
                meta.setDisplayName("My Diamond Pickaxe");
                meta.getPersistentDataContainer().set(customTag, PersistentDataType.STRING, "veinMiner");
                itemStack.setItemMeta(meta);

                // Give the ItemStack to the player
                player.getInventory().addItem(itemStack);

                return true;
            }
        }
        return false;
    }

    private boolean hasCustomTag(Player player)
    {
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (meta == null)
        {
            return false;
        }
        return meta.getPersistentDataContainer().has(customTag, PersistentDataType.STRING);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        for(int i = 1; i < blocksVeinMiner.size(); i++)
        {
            if (blocksVeinMiner.get(i)[1].equals(block.getType().name().toLowerCase()))
            {
                if (hasCustomTag(player))
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

    private void initCommande()
    {
        try
        {
            List<String> tmp = Files.readAllLines(Paths.get(System.getProperty("user.dir"),"/plugins/config/blocksVeinMiner.txt").toAbsolutePath());
            String tmpCut;
            for(int i = 0;i < tmp.size();i++)
            {
                tmpCut = tmp.get(i);
                blocksVeinMiner.add(i, tmpCut.split(";"));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
