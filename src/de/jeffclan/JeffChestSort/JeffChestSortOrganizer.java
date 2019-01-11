package de.jeffclan.JeffChestSort;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class JeffChestSortOrganizer {

	/*
	 * DEPRECATED: THE FOLLOWING INFOS ARE OUTDATED
	 * I HAVE REPLACED THE UUID CONNECTION WITH AN ARRAY THAT REFERS TO THE ACTUAL ITEMSTACK
	 * Thoughts before implementing:
	 * We create a string from each item that can be sorted.
	 * We will omit certain parts of the name and put them behind the main name for sorting reasons.
	 * E.g. ACACIA_LOG -> LOG_ACACIA (so all LOGs are grouped)
	 * Diamond, Gold, Iron, Stone, Wood does NOT have to be sorted, because they are already alphabetically in the right order
	 * We identify the ItemStack by its hashcode, which is appended to the sorting string.
	 */

	JeffChestSortPlugin plugin;

	static final String[] colors = { "white", "orange", "magenta", "light_blue", "light_gray", "yellow", "lime", "pink", "gray",
			"cyan", "purple", "blue", "brown", "green", "red", "black" };
	static final String[] woodNames = { "acacia", "birch", "jungle", "oak", "spruce", "dark_oak" };  

	ArrayList<JeffChestSortCategory> categories = new ArrayList<JeffChestSortCategory>();

	JeffChestSortOrganizer(JeffChestSortPlugin plugin) {
		this.plugin = plugin;

	
		// Load Categories
		File categoriesFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "categories" + File.separator);
		File[] listOfCategoryFiles = categoriesFolder.listFiles();

		for (File file : listOfCategoryFiles) {
			if (file.isFile()) {
				String categoryName = file.getName().replaceFirst(".txt", "");
				
				try {
					categories.add(new JeffChestSortCategory(categoryName,getArrayFromCategoryFile(file)));
					if(plugin.verbose) {
						plugin.getLogger().info("Loaded category file "+file.getName());
					}
				} catch (FileNotFoundException e) {
					plugin.getLogger().warning("Could not load category file: "+file.getName());
					//e.printStackTrace();
				}
			}
		}

	}
	
	String[] getArrayFromCategoryFile(File file) throws FileNotFoundException {
		Scanner sc = new Scanner(file);
		List<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()) {
			//if(!sc.nextLine().startsWith("#")) {
		  lines.add(sc.nextLine());
			//}
		}

		String[] arr = lines.toArray(new String[0]);
		sc.close();
		return arr;
	}
	

	String[] getTypeAndColor(String typeName) {
		
		// [0] = TypeName
		// [1] = Color

		String myColor = "<none>";
		typeName = typeName.toLowerCase();

		for (String color : colors) {
			if (typeName.startsWith(color)) {
				typeName = typeName.replaceFirst(color + "_", "");
				myColor = color;
			}
		}
		
		for(String woodName : woodNames) {
			if(typeName.equals(woodName+"_wood")) {
				typeName = "log_wood";
				myColor = woodName;
			}
			else if(typeName.startsWith(woodName)) {
				typeName = typeName.replaceFirst(woodName+"_", "");
				myColor = woodName;
			}
			else if(typeName.equals("stripped_"+woodName+"_log")) {
				//typeName = typeName.replaceFirst("stripped_"+woodName+"_", "stripped_");
				typeName = "log_stripped";
				myColor = woodName;
			} else if(typeName.equals("stripped_"+woodName+"_wood")) {
				typeName = "log_wood_stripped";
				myColor = woodName;
			}
		}
		
		// Egg has to be put in front to group spawn eggs
		// E.g. cow_spawn_egg -> egg_cow_spawn
		if(typeName.endsWith("_egg")) {
			typeName = typeName.replaceFirst("_egg", "");
			typeName = "egg_" + typeName;
		}
		
		// polished_andesite -> andesite_polished
		if(typeName.startsWith("polished_")) {
			typeName = typeName.replaceFirst("polished_", "");
			typeName = typeName + "_polished";
		}
		
		if(typeName.equalsIgnoreCase("wet_sponge")) {
			typeName = "sponge_wet";
		}
		
		
		if(typeName.equalsIgnoreCase("carved_pumpkin")) {
			typeName = "pumpkin_carved";
		}
		
		// Sort armor: helmet, chestplate, leggings, boots
		if(typeName.endsWith("helmet")) {
			typeName = typeName.replaceFirst("helmet", "1_helmet");
		} else if(typeName.endsWith("chestplate")) {
			typeName = typeName.replaceFirst("chestplate", "2_chestplate");
		} else if(typeName.endsWith("leggings")) {
			typeName = typeName.replaceFirst("leggings", "3_leggings");
		} else if(typeName.endsWith("boots")) {
			typeName = typeName.replaceFirst("boots", "4_boots");
		}
		
		// Group horse armor
		if(typeName.endsWith("horse_armor")) {
			typeName = typeName.replaceFirst("_horse_armor", "");
			typeName = "horse_armor_" + typeName;
		}

		String[] typeAndColor = new String[2];
		typeAndColor[0] = typeName;
		typeAndColor[1] = myColor;

		return typeAndColor;
	}

	String getCategory(String typeName) {

		typeName = typeName.toLowerCase();

		for (JeffChestSortCategory cat : categories) {
			if (cat.matches(typeName)) {
				return cat.name;
			}
		}

		return "<none>";
	}

	String getSortableString(ItemStack item) {
		char blocksFirst;
		char itemsFirst;
		if (item.getType().isBlock()) {
			blocksFirst = '!';
			itemsFirst = '#';
		} else {
			blocksFirst = '#';
			itemsFirst = '!';
		}

		String[] typeAndColor = getTypeAndColor(item.getType().name());
		String typeName = typeAndColor[0];
		String color = typeAndColor[1];
		String category = getCategory(item.getType().name());

		String hashCode = String.valueOf(getBetterHash(item));

		String sortableString = plugin.sortingMethod.replaceAll("\\{itemsFirst\\}", String.valueOf(itemsFirst));
		sortableString = sortableString.replaceAll("\\{blocksFirst\\}", String.valueOf(blocksFirst));
		sortableString = sortableString.replaceAll("\\{name\\}", typeName);
		sortableString = sortableString.replaceAll("\\{color\\}", color);
		sortableString = sortableString.replaceAll("\\{category\\}", category);
		sortableString = sortableString + "," + hashCode;

		return sortableString;

	}

	void sortInventory(Inventory inv) {
		sortInventory(inv,0,inv.getSize()-1);
	}
	
	void sortInventory(Inventory inv,int startSlot, int endSlot) {
		
		// This has been optimized as of ChestSort 3.2.
		// The hashCode is just kept for legacy reasons, it is actually not needed.
		
		if(plugin.debug) {
			System.out.println(" ");
			System.out.println(" ");
		}
		
		// We copy the complete inventory into an array
		ItemStack[] items = inv.getContents();
		
		// Get rid of all stuff before startSlot and after endSlot
		for(int i = 0; i<startSlot;i++) {
			items[i] = null;
		}
		for(int i=endSlot+1;i<inv.getSize();i++) {
			items[i] = null;
		}
		
		// Remove the stuff that we took from the original inventory
		for(int i = startSlot; i<=endSlot;i++) {
			inv.clear(i);
		}
		
		// We don't want to have stacks of null
		ArrayList<ItemStack> nonNullItemsList = new ArrayList<ItemStack>();
		for(ItemStack item : items) {
			if(item!=null) {
				nonNullItemsList.add(item);
			}
		}
		
		// We don't need the copied inventory anymore
		items=null;
		
		// We need the list as array
		ItemStack[] nonNullItems = nonNullItemsList.toArray(new ItemStack[nonNullItemsList.size()]);
		
		// Sort the array with ItemStacks according to our sortable String
		Arrays.sort(nonNullItems,new Comparator<ItemStack>(){
            public int compare(ItemStack s1,ItemStack s2){
                  return(getSortableString(s1).compareTo(getSortableString(s2)));
            }});

		// put everything back in the inventory

		for(ItemStack item : nonNullItems) {
			if(plugin.debug) System.out.println(getSortableString(item));
			inv.addItem(item);
		}
	}

	void sortInventoryStrict(Inventory inv,int startSlot, int endSlot) {
        // We copy the complete inventory into an array
        ItemStack[] items = inv.getContents();

        // Get rid of all stuff before startSlot and after endSlot
        for (int i = 0; i < startSlot; i++) {
            items[i] = null;
        }
        for (int i = endSlot + 1; i < inv.getSize(); i++) {
            items[i] = null;
        }

        // Remove the stuff that we took from the original inventory
        for (int i = startSlot; i <= endSlot; i++) {
            inv.clear(i);
        }

        // We don't want to have stacks of null
        ArrayList<ItemStack> nonNullItemsList = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null) {
                nonNullItemsList.add(item);
            }
        }

        // I noticed what you meant about the items not stacking when using setItem because addItem attempts to stack with matching ItemStack in the inventory.
        // The only solution I could think of would to stack matching ItemStack before sorting happens.
        // An inventory size of 54 should fit most needs but can be changed or made into a parameter
        Inventory inventoryStack = Bukkit.createInventory(null, 54);
        for (ItemStack item : nonNullItemsList) {
            inventoryStack.addItem(item);
        }

        // We need the list as array
        // This is taken from the temp inventory that is used to stack matching items
        ItemStack[] nonNullItems = inventoryStack.getContents();

        // Sort the array with ItemStacks according to our sortable String
        Arrays.sort(nonNullItems, new Comparator<ItemStack>() {
            public int compare(ItemStack s1, ItemStack s2) {
                // getContents is returning null items so return if they are to avoid NullPointerException
                // Needs to return 0 or the actual items are placed at the end of the array
                if (s1 == null || s2 == null) return 0;
                return getSortableString(s1).compareTo(getSortableString(s2));
            }
        });

        // put everything back in the inventory starting from the startSlot index
        int startIndex = startSlot;
        for (ItemStack item : nonNullItems) {
            inv.setItem(startIndex, item);
            startIndex++;
        }
	}

	private static int getBetterHash(ItemStack item) {
		// I wanted to fix the skull problems here. Instead, I ended up not using the hashCode at all.
		// I still left this here because it is nice to see the hashcodes when debug is enabled
		return item.hashCode();
	}

}
