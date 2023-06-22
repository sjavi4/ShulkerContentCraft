package me.autobot.shulkerboxnbtcrafter;

import me.autobot.shulkerboxnbtcrafter.handler.Craft.InputHandler;
import me.autobot.shulkerboxnbtcrafter.handler.Craft.OutputHandler;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;


public final class ShulkerBoxNBTCrafter extends JavaPlugin {
    private static ShulkerBoxNBTCrafter plugin;
    //private static final ArrayList<Recipe> loadedRecipe = new ArrayList<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        /*
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe nextIndex = it.next();
            if (nextIndex.getResult().getType().isAir() || nextIndex.getResult().getMaxStackSize() == 1) {
                continue;
            }
            if (nextIndex instanceof ShapedRecipe || nextIndex instanceof ShapelessRecipe) {
                loadedRecipe.add(nextIndex);
            }

        }
        Collections.sort(loadedRecipe, (r1,r2) -> r1.getResult().toString().compareToIgnoreCase(r2.getResult().toString()));

         */

        //getCommand("sbc").setExecutor(new sbcrecipe());

        getServer().getPluginManager().registerEvents(new OutputHandler(), this);
        getServer().getPluginManager().registerEvents(new InputHandler(), this);
        //getServer().getPluginManager().registerEvents(new sbcHandler(), this);
    }

    public static ShulkerBoxNBTCrafter getPlugin() {
        return plugin;
    }
    /*
    public static Recipe getRecipe(int index) {
        return loadedRecipe.get(index);
    }

     */

    public static ItemStack shulkerBoxFill(ItemStack item, int itemCount) {
        ItemStack[] craftedItem = new ItemStack[27];
        for (int i = 0; i < craftedItem.length; i++) { craftedItem[i] = new ItemStack(item.getType(), itemCount);}
        ItemStack boxedItem = new ItemStack(Material.SHULKER_BOX, 1);
        BlockStateMeta bsm = (BlockStateMeta) boxedItem.getItemMeta();
        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
        Inventory boxInv = box.getInventory();
        boxInv.setContents(craftedItem);
        bsm.setBlockState(box);
        boxedItem.setItemMeta(bsm);
        box.update();

        return boxedItem;
    }
    public static ItemStack recipeGetResult(ItemStack[] itemStacks) {
        Recipe resultedRecipe = Bukkit.getCraftingRecipe(itemStacks, Bukkit.getWorlds().get(0));
        if (resultedRecipe == null) {return null;}
        return resultedRecipe.getResult();
    }
}