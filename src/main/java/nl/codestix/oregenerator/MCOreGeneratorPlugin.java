package nl.codestix.oregenerator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class MCOreGeneratorPlugin extends JavaPlugin implements Listener {

    public ArrayList<BlockGenerator> gens = new ArrayList<>();

    public FileConfiguration generatorsConfig = new YamlConfiguration();
    public File generatorsConfigFile = new File(getDataFolder(), "generators.yml");

    public void saveGeneratorsConfig() {
        try {
            generatorsConfig.save(generatorsConfigFile);
        }
        catch(IOException ex) {
            getLogger().severe("Could not save generators.yml! " + ex);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("cobble").setExecutor(new CobbleCommand(this));

        try {
            generatorsConfig.load(generatorsConfigFile);
        }
        catch (FileNotFoundException e) {
            getLogger().warning("generators.yml not found");
        }
        catch (InvalidConfigurationException | IOException ex) {
            getLogger().severe("Could not load generators.yml! " + ex);
        }

        for(Map.Entry<String, Object> rootEntry : generatorsConfig.getValues(false).entrySet()) {
            List<String> keySplit = Arrays.asList(rootEntry.getKey().split("&"));
            if (keySplit.size() != 2)
            {
                getLogger().warning("A generator should be configured using the LAVA&WATER format. Not: " + String.join("&", keySplit));
                continue;
            }

            try {
                Material mat1 = Material.valueOf(keySplit.get(0).toUpperCase());
                Material mat2 = Material.valueOf(keySplit.get(1).toUpperCase());
                BlockGenerator gen = new BlockGenerator(mat1, mat2);
                gens.add(gen);

                ConfigurationSection section = generatorsConfig.getConfigurationSection(rootEntry.getKey());
                for(Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("particle")) {
                        gen.particle = Particle.valueOf((String)entry.getValue());
                    }
                    else if (entry.getKey().equalsIgnoreCase("particle-count")) {
                        gen.particleCount = (Integer)entry.getValue();
                    }
                    else if (entry.getKey().equalsIgnoreCase("particle-speed")) {
                        gen.particleSpeed = (Double)entry.getValue();
                    }
                    else {
                        Material mat = Material.valueOf(entry.getKey().toUpperCase());
                        int chance = (int)entry.getValue();
                        gen.chances.put(mat, chance);
                    }
                }
            }
            catch(Exception ex) {
                getLogger().warning("Could not read config value: " + ex);
            }
        }
    }

    public BlockGenerator getGenerator(Material mat1, Material mat2) {
        for(BlockGenerator gen : gens) {
            if ((gen.mat1 == mat1 && gen.mat2 == mat2) || (gen.mat2 == mat1 && gen.mat1 == mat2)) {
                return gen;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        for(BlockGenerator gen : gens) {
            ConfigurationSection section = generatorsConfig.getConfigurationSection(gen.getConfigSectionName());
            if (section == null)
                section = generatorsConfig.createSection(gen.getConfigSectionName());
            section.set("particle", gen.particle.name());
            section.set("particle-count", gen.particleCount);
            section.set("particle-speed", gen.particleSpeed);
            for(Map.Entry<Material,Integer> chance : gen.chances.entrySet()) {
                section.set(chance.getKey().name(), chance.getValue());
            }
        }

        saveGeneratorsConfig();
    }

    private final BlockFace[] ALL_FACES = new BlockFace[]{
            BlockFace.SELF,
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    @EventHandler
    public void handleGenerator(BlockFromToEvent event) {

        Block b = event.getToBlock();
        if (b.getType() != Material.AIR)
            return;

        Material type = event.getBlock().getType();
        if (type != Material.WATER && type != Material.LAVA)
            return;

        BlockGenerator genToUse = null;
        for (BlockFace face : ALL_FACES){
            Block r = b.getRelative(face, 1);
            for(BlockGenerator gen :gens) {
                if ((gen.mat1 == type && gen.mat2 == r.getType())
                || (gen.mat2 == type && gen.mat1 == r.getType()))
                {
                    genToUse = gen;
                    break;
                }
            }
        }
        if (genToUse == null)
            return;

        event.setCancelled(true);
        event.getToBlock().setType(genToUse.getRandomOre());
        Location loc = event.getToBlock().getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, 1.0f, 0.9f);
        loc.getWorld().spawnParticle(genToUse.particle, loc.add(0.5d, 0.9d, 0.5d), genToUse.particleCount, 0.4d, 0.2d, 0.4d, genToUse.particleSpeed);
    }
}
