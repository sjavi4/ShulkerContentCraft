package me.autobot.sbcrafter.listener;

import me.autobot.sbcrafter.SbCrafter;
import me.autobot.sbcrafter.util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class postcraft implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void processRemaining(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.WORKBENCH) {
            return;
        }
        CraftingInventory craftingInventory = (CraftingInventory) event.getInventory();

        if (craftingInventory.getResult() == null){
            return;
        }
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {return;}

        if (Arrays.stream(craftingInventory.getMatrix()).filter(Objects::nonNull).noneMatch(i -> i.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta)i.getItemMeta()).getBlockState() instanceof ShulkerBox)) {
            return;
        }

        Recipe recipe = util.recipe.getRecipe(craftingInventory);
        if (recipe == null) {
            return;
        }

        ItemStack result = recipe.getResult();
        List<ItemStack> _matrix = util.recipe.getMatrix();

        List<Material> matrix = new ArrayList<>();
        for (ItemStack i : _matrix) {
            if (i != null) {
                matrix.add(i.getType());
                continue;
            }
            matrix.add(null);
        }

        ItemStack[] _insideMatrix = util.recipe.getInsideMatrix();
        AtomicInteger inputBox = new AtomicInteger(util.recipe.getInputBox());


        ItemStack[] insideMatrix = new ItemStack[9];

        for (int i = 0 ; i < _insideMatrix.length ; i++) {
            if (_insideMatrix[i] != null) {
                insideMatrix[i] = _insideMatrix[i].clone();
                continue;
            }
            insideMatrix[i] = null;
        }

        Bukkit.getScheduler().runTaskLater(SbCrafter.getPlugin(), () -> {

            Predicate<Material> predicate = i -> !(i == null || i.isAir());


            boolean differ = Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(e -> e.getMaxStackSize() < 64) && Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(e -> e.getMaxStackSize() == 64);
            int min = differ? Arrays.stream(insideMatrix).filter(Objects::nonNull).min(Comparator.comparing(ItemStack::getMaxStackSize)).get().getMaxStackSize() : 64;
            int extraFill = Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(e -> e.getMaxStackSize() < 64)? 0 :result.getAmount() * 64/result.getMaxStackSize() -1;
            int[] pos = {0};
            if (differ) {
                Arrays.stream(insideMatrix).peek(e -> pos[0]++).filter(e -> e != null && e.getMaxStackSize() == min).findFirst();
                matrix.set(pos[0]-1,null);
            } else {
                matrix.stream().peek(e -> {
                    if (e == null || e.isAir()) {
                        pos[0]++;
                    }
                }).anyMatch(predicate);
                matrix.set(pos[0],null);
            }

            if (differ) {
                float remainder = 0.0F;
                int input = Integer.valueOf(inputBox.get()-1);
                int count = (int) Arrays.stream(insideMatrix).filter(e -> e != null && e.getMaxStackSize() > min).count();
                ItemStack[] remain = new ItemStack[27];
                for (int i = 0; i < count; i++) {
                    int[] index = {0};
                    if (remainder >= 1) {
                        remainder--;
                    }
                    remainder += (float) min/input - min/input;
                    float finalRemainder = remainder;
                    Arrays.stream(insideMatrix).filter(e -> e != null && !e.getType().isAir() && e.getMaxStackSize() > min).findFirst().ifPresent(e -> {
                        Arrays.fill(remain,new ItemStack(e.getType(), e.getMaxStackSize()-min/input-(int)finalRemainder));
                    });
                    matrix.stream().peek(e -> {
                        if (e == null || e.isAir()) {
                            index[0]++;
                        }
                    }).filter(predicate).findFirst().ifPresent(e -> {
                        craftingInventory.setItem(index[0]+1, util.box.newbox(e, remain));
                        matrix.set(index[0],null);
                    });
                    inputBox.getAndDecrement();
                }
            }

            //Arrays.fill(item, new ItemStack(result.getType(), result.getMaxStackSize()));

            ItemStack[] special = new ItemStack[27];
            switch (result.getType()) {
                case HONEY_BLOCK -> {
                    Arrays.fill(special, new ItemStack(Material.GLASS_BOTTLE,64));
                }
                case SUGAR -> {
                    if (Arrays.stream(insideMatrix).filter(Objects::nonNull).anyMatch(i -> i.getType() == Material.HONEY_BOTTLE)) {
                        Arrays.fill(special, new ItemStack(Material.GLASS_BOTTLE, 16));
                    }
                }
            }
            if (Arrays.stream(special).noneMatch(e -> e == null || e.getType().isAir())) {
                int[] index = {0};
                matrix.stream().peek(e -> {
                    if (e == null || e.isAir()) {
                        index[0]++;
                    }
                }).filter(predicate).findFirst().ifPresent(e -> {
                    craftingInventory.setItem(index[0]+1, util.box.newbox(e, special));
                    matrix.set(index[0],null);
                });
                inputBox.getAndDecrement();
            }

            ItemStack[] item = new ItemStack[27];
            Arrays.fill(item,new ItemStack(result.getType(),result.getMaxStackSize()));

            if (extraFill > 0) {
                for (int i = 0; i < extraFill ; i++) {
                    int[] index = {0};
                    matrix.stream().peek(e -> {
                        if (e == null || e.isAir()) {
                            index[0]++;
                        }
                    }).filter(predicate).findFirst().ifPresent(e -> {
                        craftingInventory.setItem(index[0]+1, util.box.newbox(e, item));
                        matrix.set(index[0],null);
                    });
                    inputBox.getAndDecrement();
                }
            }
            if (inputBox.get() > 0) {
                for (int i = 0; i < inputBox.get(); i++) {
                    int[] index = {0};
                    matrix.stream().peek(e -> {
                        if (e == null || e.isAir()) {
                            index[0]++;
                        }
                    }).filter(predicate).findFirst().ifPresent(e -> {
                        craftingInventory.setItem(index[0]+1, new ItemStack(e));
                        matrix.set(index[0],null);
                    });

                }
            }
        },0);
    }
}
