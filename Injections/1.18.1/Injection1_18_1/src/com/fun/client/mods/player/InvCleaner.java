package com.fun.client.mods.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.fun.client.mods.Category;
import com.fun.client.mods.Module;
import com.fun.client.mods.VModule;
import com.fun.client.settings.Setting;
import com.fun.eventapi.event.events.EventMotion;
import com.fun.eventapi.event.events.EventPacket;

import com.fun.eventapi.event.events.EventTick;
import com.fun.utils.BlockUtils;
import com.fun.utils.InventoryUtils;
import com.fun.utils.TimerUtils;
import com.fun.utils.math.Timer;
import net.minecraft.BlockUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SwordItem;


public class InvCleaner extends VModule {
   private final Setting swing = new Setting("Swing", this, true);
   private final Setting offHand = new Setting("Offhand Gapple", this, false);
   private final Setting delay = new Setting("Delay", this, 5, 0, 300, true);
   private final Setting armorDelay = new Setting("Armor Delay", this, 20, 0, 300, true);
   public final Setting slotWeapon = new Setting("Weapon Slot", this, 1, 1, 9, true);
   public final Setting slotPick = new Setting("Pickaxe Slot", this, 2, 1, 9, true);
   public final Setting slotAxe = new Setting("Axe Slot", this, 3, 1, 9, true);
   public final Setting slotGapple = new Setting("Gapple Slot", this, 4, 1, 9, true);
   public final Setting slotWater = new Setting("Water Slot", this, 5, 1, 9, true);
   public final Setting slotBow = new Setting("Bow Slot", this, 6, 1, 9, true);
   public final Setting slotBlock = new Setting("Block Slot", this, 7, 1, 9, true);
   public final Setting slotPearl = new Setting("Pearl Slot", this, 8, 1, 9, true);
   public final String[] serverItems = new String[]{
      "选择游戏",
      "加入游戏",
      "职业选择菜单",
      "离开对局",
      "再来一局",
      "selector",
      "tracking compass",
      "(right click)",
      "tienda ",
      "perfil",
      "salir",
      "shop",
      "collectibles",
      "game",
      "profil",
      "lobby",
      "show all",
      "hub",
      "friends only",
      "cofre",
      "(click",
      "teleport",
      "play",
      "exit",
      "hide all",
      "jeux",
      "gadget",
      " (activ",
      "emote",
      "amis",
      "bountique",
      "choisir",
      "choose "
   };
   private final int[] bestArmorPieces = new int[6];
   private final List<Integer> trash = new ArrayList<>();
   private final int[] bestToolSlots = new int[2];
   private final List<Integer> gappleStackSlots = new ArrayList<>();
   private int bestSwordSlot;
   private int bestPearlSlot;
   private int bestBowSlot;
   private int bestWaterSlot;
   private int ticksSinceLastClick;
   private boolean nextTickCloseInventory;
   private boolean serverOpen;
   private boolean clientOpen;
   private final TimerUtils timer = new TimerUtils();

   public InvCleaner() {
      super("InvCleaner", Category.Player);
   }

   @Override
   public void onPacket(EventPacket event) {
      Packet<?> packet = (Packet<?>) event.packet;
      if (packet instanceof ClientboundOpenScreenPacket) {
         this.clientOpen = false;
         this.serverOpen = false;
      }

      if (packet instanceof ServerboundPlayerCommandPacket wrapper) {
         if (wrapper.getData() == mc.player.getId() && wrapper.getAction() == Action.OPEN_INVENTORY) {
            this.clientOpen = true;
            this.serverOpen = true;
         }
      } else if (packet instanceof ServerboundContainerClosePacket wrapperx) {
         if (wrapperx.getContainerId() == mc.player.inventoryMenu.containerId) {
            this.clientOpen = false;
            this.serverOpen = false;
         }
      } else if (packet instanceof ServerboundContainerClickPacket && !mc.player.isUsingItem()) {
         this.ticksSinceLastClick = 0;
      }
   }

   private boolean dropItem(List<Integer> listOfSlots) {
      if (!listOfSlots.isEmpty()) {
         int slot = listOfSlots.remove(0);
         mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 1, ClickType.THROW, mc.player);
         if (this.swing.getValBoolean()) {
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onTick(EventTick event) {
      this.ticksSinceLastClick++;
      if ((double)this.ticksSinceLastClick < Math.floor(this.delay.getValDouble()) / 50.0) {
         return;
      }

      if (mc.screen instanceof InventoryScreen) {
         this.clear();
         for (int slot = 5; slot < 45; slot++) {
            ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (!stack.isEmpty()) {
               if (stack.getItem() instanceof SwordItem && InventoryUtils.isBestSword(handler, stack)) {
                  this.bestSwordSlot = slot;
               } else if (stack.getItem() instanceof DiggerItem && InventoryUtils.isBestTool(handler, stack)) {
                  int toolType = InventoryUtils.getToolType(stack);
                  if (toolType != -1 && slot != this.bestToolSlots[toolType]) {
                     this.bestToolSlots[toolType] = slot;
                  }
               } else {
                  Item armorSlot = stack.getItem();
                  if (armorSlot instanceof ArmorItem) {
                     ArmorItem armor = (ArmorItem)armorSlot;
                     if (InventoryUtils.isBestArmor(handler, stack)) {
                        EquipmentSlot armorSlotx = armor.getSlot();
                        int index = armorSlotx.ordinal();
                        if (index >= 1 && index < this.bestArmorPieces.length + 2) {
                           int pieceSlot = this.bestArmorPieces[index];
                           if (pieceSlot == -1 || slot != pieceSlot) {
                              this.bestArmorPieces[index] = slot;
                           }
                        }
                        continue;
                     }
                  }

                  if (!(stack.getItem() instanceof BowItem) || !InventoryUtils.isBestBow(handler, stack)) {
                     if (stack.getItem() == Items.GOLDEN_APPLE) {
                        this.gappleStackSlots.add(slot);
                     } else if (stack.getItem() == Items.ENDER_PEARL) {
                        this.bestPearlSlot = slot;
                     } else if (stack.getItem() == Items.WATER_BUCKET) {
                        if (slot != this.bestWaterSlot) {
                           this.bestWaterSlot = slot;
                        }
                     } else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                        this.trash.add(slot);
                     }
                  } else if (slot != this.bestBowSlot) {
                     this.bestBowSlot = slot;
                  }
               }
            }
         }

         boolean busy = !this.trash.isEmpty() || this.equipArmor(false) || this.sortItems(false);
         if (!busy) {
            if (this.nextTickCloseInventory) {
               this.close();
               this.nextTickCloseInventory = false;
            } else {
               this.nextTickCloseInventory = true;
            }

            return;
         }

         boolean waitUntilNextTick = !this.serverOpen;
         this.open();
         if (this.nextTickCloseInventory) {
            this.nextTickCloseInventory = false;
         }

         if (waitUntilNextTick) {
            return;
         }

         if (this.timer.hasTimeElapsed((long) this.armorDelay.getValDouble()) && this.equipArmor(true)) {
            return;
         }

         if (this.dropItem(this.trash)) {
            return;
         }

         this.sortItems(true);
      }
   }

   private boolean sortItems(boolean moveItems) {
      int goodSwordSlot = (int) (this.slotWeapon.getValDouble() + 35);
      if (this.bestSwordSlot != -1 && this.bestSwordSlot != goodSwordSlot) {
         if (moveItems) {
            this.putItemInSlot(goodSwordSlot, this.bestSwordSlot);
            this.bestSwordSlot = goodSwordSlot;
         }

         return true;
      } else {
         int goodBowSlot = (int) (this.slotBow.getValDouble() + 35);
         if (this.bestBowSlot != -1 && this.bestBowSlot != goodBowSlot) {
            if (moveItems) {
               this.putItemInSlot(goodBowSlot, this.bestBowSlot);
               this.bestBowSlot = goodBowSlot;
            }

            return true;
         } else {
            int goodWaterSlot = (int) (this.slotWater.getValDouble() + 35);
            if (this.bestWaterSlot != -1 && this.bestWaterSlot != goodWaterSlot) {
               if (moveItems) {
                  this.putItemInSlot(goodWaterSlot, this.bestWaterSlot);
                  this.bestWaterSlot = goodWaterSlot;
               }

               return true;
            } else {
               int goodGappleSlot = (int) (this.slotGapple.getValDouble() + 35);
               if (this.offHand.getValBoolean()) {
                  if (!this.gappleStackSlots.isEmpty()) {
                     this.gappleStackSlots.sort(new Comparator<Integer>() {
                        public int compare(Integer slot1, Integer slot2) {
                           int count1 = mc.player.containerMenu.getSlot(slot1).getItem().getCount();
                           int count2 = mc.player.containerMenu.getSlot(slot2).getItem().getCount();
                           return Integer.compare(count1, count2);
                        }
                     });
                     int bestGappleSlot = this.gappleStackSlots.get(0);
                     if (bestGappleSlot != 45) {
                        if (moveItems) {
                           this.putItemInSlot(45, bestGappleSlot);
                           this.gappleStackSlots.set(0, 45);
                        }

                        return true;
                     }
                  }
               } else if (!this.gappleStackSlots.isEmpty()) {
                  this.gappleStackSlots.sort(new Comparator<Integer>() {
                     public int compare(Integer slot1, Integer slot2) {
                        int count1 = mc.player.containerMenu.getSlot(slot1).getItem().getCount();
                        int count2 = mc.player.containerMenu.getSlot(slot2).getItem().getCount();
                        return Integer.compare(count1, count2);
                     }
                  });
                  int bestGappleSlot = this.gappleStackSlots.get(0);
                  if (bestGappleSlot != goodGappleSlot) {
                     if (moveItems) {
                        this.putItemInSlot(goodGappleSlot, bestGappleSlot);
                        this.gappleStackSlots.set(0, goodGappleSlot);
                     }

                     return true;
                  }
               }

               int[] toolSlots = new int[]{(int) (this.slotPick.getValDouble() + 35), (int) (this.slotAxe.getValDouble() + 35)};

               for (int toolSlot : this.bestToolSlots) {
                  if (toolSlot != -1) {
                     int type = InventoryUtils.getToolType(mc.player.containerMenu.getSlot(toolSlot).getItem());
                     if (type != -1 && toolSlot != toolSlots[type]) {
                        if (moveItems) {
                           this.putToolsInSlot(type, toolSlots);
                        }

                        return true;
                     }
                  }
               }

               int goodBlockSlot = (int) (this.slotBlock.getValDouble() + 35);
               int mostBlocksSlot = this.getMostBlocks();
               if (mostBlocksSlot != -1 && mostBlocksSlot != goodBlockSlot) {
                  Slot dss = mc.player.containerMenu.getSlot(goodBlockSlot);
                  ItemStack dsis = dss.getItem();
                  if (dsis.isEmpty()
                     || !(dsis.getItem() instanceof BlockItem)
                     || dsis.getCount() < mc.player.containerMenu.getSlot(mostBlocksSlot).getItem().getCount()) {
                     this.putItemInSlot(goodBlockSlot, mostBlocksSlot);
                  }
               }

               int goodPearlSlot = (int) (this.slotPearl.getValDouble() + 35);
               if (this.bestPearlSlot != -1 && this.bestPearlSlot != goodPearlSlot) {
                  if (moveItems) {
                     this.putItemInSlot(goodPearlSlot, this.bestPearlSlot);
                     this.bestPearlSlot = goodPearlSlot;
                  }

                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   public int getMostBlocks() {
      int stack = 0;
      int biggestSlot = -1;

      for (int i = 9; i < 45; i++) {
         Slot slot = mc.player.containerMenu.getSlot(i);
         ItemStack is = slot.getItem();
         if (!is.isEmpty() && is.getItem() instanceof BlockItem && is.getCount() > stack) {
            boolean noneMatch = true;
            String itemName = is.getItem().getName(is).getString().toLowerCase();

            for (String serverItem : this.serverItems) {
               if (itemName.contains(serverItem.toLowerCase())) {
                  noneMatch = false;
                  break;
               }
            }

            if (noneMatch) {
               stack = is.getCount();
               biggestSlot = i;
            }
         }
      }

      return biggestSlot;
   }

   private boolean equipArmor(boolean moveItems) {
      for (int i = 0; i < this.bestArmorPieces.length; i++) {
         int piece = this.bestArmorPieces[i];
         if (piece != -1) {
            int armorPieceSlot = this.getArmorSlot(EquipmentSlot.values()[i]);
            if (armorPieceSlot >= 0 && armorPieceSlot < mc.player.containerMenu.slots.size()) {
               ItemStack stack = mc.player.containerMenu.getSlot(armorPieceSlot).getItem();
               if (stack.isEmpty()) {
                  if (moveItems) {
                     mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, piece, 0, ClickType.QUICK_MOVE, mc.player);
                  }

                  this.timer.reset();
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int getArmorSlot(EquipmentSlot slot) {
      switch (slot) {
         case HEAD:
            return 5;
         case CHEST:
            return 6;
         case LEGS:
            return 7;
         case FEET:
            return 8;
         default:
            return -1;
      }
   }

   private void putItemInSlot(int slot, int slotIn) {
      mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotIn, slot == 45 ? 40 : slot - 36, ClickType.SWAP, mc.player);
   }

   private void putToolsInSlot(int tool, int[] toolSlots) {
      int toolSlot = toolSlots[tool];
      mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, this.bestToolSlots[tool], toolSlot - 36, ClickType.SWAP, mc.player);
      this.bestToolSlots[tool] = toolSlot;
   }

   private static boolean isValidStack(ItemStack stack) {
      if (stack.getItem() instanceof BlockItem && BlockUtils.isValidBlock(((BlockItem)stack.getItem()).getBlock())) {
         return true;
      } else if (stack.getItem() instanceof PotionItem && InventoryUtils.isBuffPotion(stack)) {
         return true;
      } else if (stack.getItem().getFoodProperties() != null && InventoryUtils.isGoodFood(stack)) {
         return true;
      } else {
         return stack.getItem() == Items.TOTEM_OF_UNDYING ? true : InventoryUtils.isGoodItem(stack);
      }
   }

   @Override
   public void onEnable() {
      this.ticksSinceLastClick = 0;
      this.clientOpen = mc.screen instanceof InventoryScreen;
      this.serverOpen = this.clientOpen;
   }

   @Override
   public void onDisable() {
      this.clear();
   }

   private void open() {
      if (!this.clientOpen && !this.serverOpen) {
         mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, Action.OPEN_INVENTORY));
         this.serverOpen = true;
      }
   }

   private void close() {
      if (!this.clientOpen && this.serverOpen) {
         mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
         this.serverOpen = false;
      }
   }

   private void clear() {
      this.trash.clear();
      this.bestBowSlot = -1;
      this.bestSwordSlot = -1;
      this.bestWaterSlot = -1;
      this.gappleStackSlots.clear();
      Arrays.fill(this.bestArmorPieces, -1);
      Arrays.fill(this.bestToolSlots, -1);
   }
}
