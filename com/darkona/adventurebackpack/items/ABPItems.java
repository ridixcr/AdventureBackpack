package com.darkona.adventurebackpack.items;

import com.darkona.adventurebackpack.config.ItemInfo;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class ABPItems {

	public static ItemAdvBackpack advBackpack;
	public static ItemHose hose;
	public static ItemMachete machete;
	public static ItemMiningHelmet miningHat;
	public static ItemPistonBoots pistonboots;
	public static ItemAdventureSuit advsuit;
	public static ItemBackpackComponent bpcomponent;
	public static ItemAdventureHat advhat;

	public static void init() {
		advBackpack = new ItemAdvBackpack(ItemInfo.AB_ID);
		hose = new ItemHose(ItemInfo.HOSE_ID);
		machete = new ItemMachete(ItemInfo.MACHETE_ID);
		miningHat = new ItemMiningHelmet(ItemInfo.HELMET_ID);
		pistonboots = new ItemPistonBoots(ItemInfo.BOOTS_ID);
		advsuit = new ItemAdventureSuit(ItemInfo.SUIT_ID);
		bpcomponent = new ItemBackpackComponent(ItemInfo.BACKPACKCOMPONENT_ID);
		advhat = new ItemAdventureHat(ItemInfo.HAT_ID);
	}

	public static void addNames() {
		LanguageRegistry.addName(advBackpack, ItemInfo.AB_NAME);
		LanguageRegistry.addName(hose, ItemInfo.HOSE_NAME);
		LanguageRegistry.addName(machete, ItemInfo.MACHETE_NAME);
		LanguageRegistry.addName(miningHat, ItemInfo.HELMET_NAME);
		LanguageRegistry.addName(pistonboots, ItemInfo.BOOTS_NAME);
		LanguageRegistry.addName(advsuit, ItemInfo.SUIT_NAME);
		LanguageRegistry.addName(bpcomponent, ItemInfo.BP_COMPONENT_NAME);
		LanguageRegistry.addName(advhat, ItemInfo.HAT_NAME);
	}
	
	
}
