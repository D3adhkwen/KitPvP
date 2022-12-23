package com.planetgallium.kitpvp.item;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class AttributeWriter {

    // look into using XItemStack by Crypto

    public static void potionEffectToResource(Resource resource, String path, PotionEffect effect) {
        if (effect == null) return;

        int amplifierNonZeroBased = effect.getAmplifier() + 1;
        int durationSeconds = effect.getDuration() / 20;

        resource.set(path + "." + effect.getType().getName() + ".Amplifier", amplifierNonZeroBased);
        resource.set(path + "." + effect.getType().getName() + ".Duration", durationSeconds);
        resource.save();
    }

    public static void itemStackToResource(Resource resource, String path, ItemStack item) {
        if (item == null || item.getType() == XMaterial.AIR.parseMaterial()) return;

        ItemMeta meta = item.getItemMeta();

        resource.set(path + ".Name", meta.hasDisplayName() ? Toolkit.toNormalColorCodes(meta.getDisplayName()) : null);
        resource.set(path + ".Lore", Toolkit.toNormalColorCodes(meta.getLore()));
        resource.set(path + ".Material", item.getType().toString());
        resource.set(path + ".Amount", item.getAmount() == 1 ? null : item.getAmount());
        resource.save();

        serializeDyedArmor(resource, item, path);
        serializeSkull(resource, item, path);
        serializeTippedArrows(resource, item, path);
        serializePotion(resource, item, path);
        serializeEnchantments(resource, item, path);
        serializeDurability(resource, item, path);
        serializeFireworkRocket(resource, item, path);
        serializeChargedProjectiles(resource, item, path);
    }

    private static void serializeDyedArmor(Resource resource, ItemStack item, String path) {
        if (item.getType() == XMaterial.LEATHER_HELMET.parseMaterial() ||
            item.getType() == XMaterial.LEATHER_CHESTPLATE.parseMaterial() ||
            item.getType() == XMaterial.LEATHER_LEGGINGS.parseMaterial() ||
            item.getType() == XMaterial.LEATHER_BOOTS.parseMaterial()) {

            LeatherArmorMeta dyedMeta = (LeatherArmorMeta) item.getItemMeta();

            resource.set(path + ".Dye.Red", dyedMeta.getColor().getRed());
            resource.set(path + ".Dye.Green", dyedMeta.getColor().getGreen());
            resource.set(path + ".Dye.Blue", dyedMeta.getColor().getBlue());
            resource.save();
        }
    }

    private static void serializeSkull(Resource resource, ItemStack item, String path) {
        if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

            resource.set(path + ".Skull", skullMeta.getOwner());
            resource.save();
        }
    }

    private static void serializeTippedArrows(Resource resource, ItemStack item, String path) {
        if (Toolkit.versionToNumber() >= 19 && item.getType() == XMaterial.TIPPED_ARROW.parseMaterial()) {
            serializeEffects(resource, item, path);
        }
    }

    private static void serializeEffects(Resource resource, ItemStack item, String path) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        String effectPath = path + ".Effects";

        if (meta.getCustomEffects().size() > 0) {
            for (PotionEffect effect : meta.getCustomEffects()) {
                resource.set(effectPath + "." + effect.getType().getName() + ".Amplifier", effect.getAmplifier() + 1);
                resource.set(effectPath + "." + effect.getType().getName() + ".Duration", effect.getDuration() / 20);
                resource.save();
            }

        } else {

            if (Toolkit.versionToNumber() == 18) {
                Potion potionStack = Potion.fromItemStack(item);

                resource.set(path + ".Type", potionStack.isSplash() ? "SPLASH_POTION" : "POTION");
                resource.save();

                resource.set(path + ".Effects." + potionStack.getType() + ".Upgraded", potionStack.getLevel() > 0);
                resource.set(path + ".Effects." + potionStack.getType() + ".Extended", potionStack.hasExtendedDuration());
                resource.save();

            } else if (Toolkit.versionToNumber() >= 19) {
                PotionData data = meta.getBasePotionData();

                String effectName = data.getType().getEffectType().getName();
                resource.set(effectPath + "." + effectName + ".Upgraded", data.isUpgraded());
                resource.set(effectPath + "." + effectName + ".Extended", data.isExtended());
                resource.save();
            }
        }
    }

    private static void serializePotion(Resource resource, ItemStack item, String path) {
        if (item.getType() == XMaterial.POTION.parseMaterial() ||
                (Toolkit.versionToNumber() >= 19 &&
                        (item.getType() == XMaterial.SPLASH_POTION.parseMaterial() ||
                        item.getType() == XMaterial.LINGERING_POTION.parseMaterial()))) {
            serializeEffects(resource, item, path);
        }
    }

    private static void serializeEnchantments(Resource resource, ItemStack item, String path) {
        if (item.getEnchantments().size() > 0) {
            for (Enchantment enchantment : item.getEnchantments().keySet()) {
                String enchantmentName = Toolkit.versionToNumber() < 113 ?
                        enchantment.getName() : enchantment.getKey().getKey();
                resource.set(path + ".Enchantments." + enchantmentName.toUpperCase(),
                        item.getEnchantments().get(enchantment));
                resource.save();
            }
        }
    }

    private static void serializeDurability(Resource resource, ItemStack item, String path) {
        if (Toolkit.versionToNumber() < 113) {
            if (item.getDurability() > 0 &&
                    item.getType() != XMaterial.PLAYER_HEAD.parseMaterial() &&
                    item.getType() != XMaterial.POTION.parseMaterial() &&
                    item.getType() != XMaterial.SPLASH_POTION.parseMaterial()) {

                resource.set(path + ".Durability", item.getDurability());
                resource.save();
            }

        } else if (Toolkit.versionToNumber() >= 113) {

            if (item.getItemMeta() instanceof Damageable &&
                    item.getType() != XMaterial.PLAYER_HEAD.parseMaterial() &&
                    item.getType() != XMaterial.POTION.parseMaterial() &&
                    item.getType() != XMaterial.SPLASH_POTION.parseMaterial()) {

                Damageable damagedMeta = (Damageable) item.getItemMeta();

                if (damagedMeta.hasDamage()) {
                    resource.set(path + ".Durability", damagedMeta.getDamage());
                    resource.save();
                }
            }
        }
    }

    private static void serializeFireworkRocket(Resource resource, ItemStack item, String path) {

        if (item.getType() == XMaterial.FIREWORK_ROCKET.parseMaterial()) {

            FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();

            if (!fireworkMeta.hasEffects()) return;

            List<FireworkEffect> effectList = fireworkMeta.getEffects();

            resource.set(path + ".Power", fireworkMeta.getPower());

            for (int i = 0; i < fireworkMeta.getEffectsSize(); i++) {

                FireworkEffect fireworkEffect = effectList.get(i);
                String effectPath = path + ".Effects." + i;

                resource.set(effectPath + ".Type", fireworkEffect.getType().toString());
                resource.set(effectPath + ".Flicker", fireworkEffect.hasFlicker());
                resource.set(effectPath + ".Trail", fireworkEffect.hasTrail());

                for (int j = 0; j < fireworkEffect.getColors().size(); j++) {
                    Color color = fireworkEffect.getColors().get(j);
                    String colorPath = effectPath + ".Colors." + j;
                    resource.set(colorPath + ".Red", color.getRed());
                    resource.set(colorPath + ".Green", color.getGreen());
                    resource.set(colorPath + ".Blue", color.getBlue());
                }

                for (int j = 0; j < fireworkEffect.getFadeColors().size(); j++) {
                    Color fadeColor = fireworkEffect.getFadeColors().get(j);
                    String fadeColorPath = effectPath + ".FadeColors." + j;
                    resource.set(fadeColorPath + ".Red", fadeColor.getRed());
                    resource.set(fadeColorPath + ".Green", fadeColor.getGreen());
                    resource.set(fadeColorPath + ".Blue", fadeColor.getBlue());
                }

                resource.save();

            }

        }

    }

    private static void serializeChargedProjectiles(Resource resource, ItemStack item, String path) {

        if (item.getType() == XMaterial.CROSSBOW.parseMaterial()) {

            CrossbowMeta crossbowMeta = (CrossbowMeta) item.getItemMeta();

            if (!crossbowMeta.hasChargedProjectiles()) return;

            List<ItemStack> chargedProjectiles = crossbowMeta.getChargedProjectiles();

            for (int i = 0; i < chargedProjectiles.size(); i++) {

                ItemStack projectile = chargedProjectiles.get(i);
                String projectilesPath = path + ".Projectiles." + i;

                itemStackToResource(resource, projectilesPath, projectile);

            }

        }

    }

}
