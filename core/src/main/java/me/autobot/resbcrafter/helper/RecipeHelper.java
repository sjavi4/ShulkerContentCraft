package me.autobot.resbcrafter.helper;

import me.autobot.resbcrafter.constants.Items;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecipeHelper {
    public final CraftingInventory craftingInventory;
    public final ItemStack[] originalMatrix;
    public int[] originalMatrixWithBoxColor = new int[] {-3,-3,-3,-3,-3,-3,-3,-3,-3};
    //public final Map<Integer, ItemStack[]> originalBoxContent = new HashMap<>();
    public final Map<Integer, ItemStack> condensedBoxContent = new HashMap<>();
    public ItemStack[] condensedMatrix = new ItemStack[9];
    //public int[] condensedContentMaxCount = new int[9];
    public int firstBoxIndex = -1;
    public int leastMaxStackSize = 64;
    public int mostMaxStackSize = 0;
    public Player player;
    public RecipeHelper(CraftingInventory inventory) {
        this.craftingInventory = inventory;
        this.originalMatrix = inventory.getMatrix();
        getMatrixContent();
        //System.out.println(Arrays.toString(originalMatrixWithBoxColor));
        getShulkerBoxContent();
        //originalBoxContent.values().forEach(itemStacks -> System.out.println(Arrays.toString(itemStacks)));
        this.player = (Player) craftingInventory.getViewers().getFirst();
    }

    public void getMatrixContent() {
        // -3 for placeholder, to determine player 4x4 crafting
        // -2 for empty item stack
        // -1 for non-shulker box
        for (int matrixIndex = 0; matrixIndex < this.originalMatrix.length; matrixIndex++) {
            ItemStack itemStack = this.originalMatrix[matrixIndex];
            if (isEmpty(itemStack)) {
                this.originalMatrixWithBoxColor[matrixIndex] = -2;
                continue;
            }
            this.originalMatrixWithBoxColor[matrixIndex] = Items.SHULKER_BOXES.indexOf(itemStack.getType());
        }
    }
    public void getShulkerBoxContent() {
        int matrixIndex = 0;
        for (int colorIndex : originalMatrixWithBoxColor) {
            // 0 - 15
            if (colorIndex < 0) {
                matrixIndex++;
                continue;
            }
            ItemStack[] boxContents = ShulkerBoxHelper.convertShulkerBox(this.originalMatrix[matrixIndex]).getInventory().getContents();
            //originalBoxContent.put(matrixIndex, boxContents);
            getCondensedBoxContent(matrixIndex, boxContents);
            matrixIndex++;
        }

        // Not using toArray, not retaining shape
        this.condensedBoxContent.forEach((index, itemStack) -> {
            if (itemStack != null) {
                ItemStack clone = new ItemStack(itemStack);
                //clone.setAmount(1);
                condensedMatrix[index] = clone;
                //condensedContentMaxCount[index] = clone.getMaxStackSize();
            }
        });
        //System.out.println(Arrays.toString(condensedMatrix));
    }
    public void getCondensedBoxContent(int matrixIndex, ItemStack[] itemStacks) {
        // Beware of empty box
        long itemTypes = Arrays.stream(itemStacks).distinct().count();
        // Mixed content
        if (itemTypes > 1) {
            this.condensedBoxContent.put(matrixIndex, null);
            return;
        }
        // Fill air if null content
        if (isEmpty(itemStacks[0])) {
            this.condensedBoxContent.put(matrixIndex, Items.AIR);
        } else {
            this.condensedBoxContent.put(matrixIndex, itemStacks[0]);
            int stackSize = itemStacks[0].getMaxStackSize();
            if (firstBoxIndex == -1) {
                firstBoxIndex = matrixIndex;
            }
            if (stackSize < leastMaxStackSize) {
                leastMaxStackSize = stackSize;
                firstBoxIndex = matrixIndex;
                //System.out.println("LeastSize " + leastMaxStackSize);
            }
            if (stackSize > mostMaxStackSize) {
                mostMaxStackSize = stackSize;
            }
        }
        // if having the least max stack, firstBoxIndex is indicated to the least one
        //System.out.println("First " + firstBoxIndex);

    }
    public Recipe getRecipe() {
        // Not accept non-Shulker box and non-AIR items
        if (invalidMatrix()) {
            return null;
        }
        // This Recipe does not contain NBT
        return Bukkit.getServer().getCraftingRecipe(condensedMatrix, player.getWorld());
    }
    public ItemStack getCondensedResult() {
        // Not accept non-Shulker box and non-AIR items
        if (invalidMatrix()) {
            return null;
        }
        // Simulate Player Crafting, Return Item with NBT / custom items
        // Does not consume items
        return Bukkit.craftItem(condensedMatrix, player.getWorld(), player);
    }
    public boolean invalidMatrix() {
        boolean impure = Arrays.stream(this.originalMatrixWithBoxColor).anyMatch(color -> color == -1);
        boolean allFull = Arrays.stream(this.originalMatrix)
                .filter(i -> i != null && !i.equals(Items.AIR))
                .map(i -> ShulkerBoxHelper.convertShulkerBox(i).getInventory().getContents())
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .allMatch(c -> c.getAmount() == c.getMaxStackSize());
        if (impure || !allFull) {
            return true;
        }
        // Mixed Content is presence
        return this.condensedBoxContent.containsValue(null);
    }
    public boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.isSimilar(Items.AIR);
    }
    public long getInputBoxCount() {
        return Arrays.stream(originalMatrixWithBoxColor).filter(i -> i >= 0).count();
    }
    /*
    public long getEmptyBoxCount() {
        return condensedBoxContent.values().stream().filter(i -> i.equals(Items.AIR)).count();
    }

     */
    public boolean mixStackCrafting() {
        return leastMaxStackSize != 64 && mostMaxStackSize != 0 && leastMaxStackSize != mostMaxStackSize;
    }
}
