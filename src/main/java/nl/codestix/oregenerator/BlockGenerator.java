package nl.codestix.oregenerator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

public class BlockGenerator {

    public Material mat1;
    public Material mat2;
    public HashMap<Material, Integer> chances = new HashMap<>();
    public Particle particle = Particle.SMOKE_LARGE;
    public double particleSpeed = 0.01d;
    public int particleCount = 5;

    public static final HashMap<Material, Integer> DEFAULT_CHANCES = new HashMap<>();

    static {
        DEFAULT_CHANCES.put(Material.STONE, 100);
        DEFAULT_CHANCES.put(Material.DIRT, 40);
        DEFAULT_CHANCES.put(Material.EMERALD_ORE, 10);
        DEFAULT_CHANCES.put(Material.LAPIS_ORE, 25);
        DEFAULT_CHANCES.put(Material.DIAMOND_ORE, 20);
        DEFAULT_CHANCES.put(Material.GOLD_ORE, 30);
        DEFAULT_CHANCES.put(Material.REDSTONE_ORE, 40);
        DEFAULT_CHANCES.put(Material.IRON_ORE, 50);
        DEFAULT_CHANCES.put(Material.COAL_ORE, 50);
    }

    public BlockGenerator(Material mat1, Material mat2) {
        this.mat1 = mat1;
        this.mat2 = mat2;
    }

    public int getChancesSum() {
        int sum = 0;
        for(int c : chances.values()) {
            sum += c;
        }
        return sum;
    }

    public Material getRandomOre()
    {
        int sum = getChancesSum();
        int rnd = (int)Math.floor(Math.random() * sum);

        for(Map.Entry<Material,Integer> entry : chances.entrySet()) {
            rnd -= entry.getValue();
            if (rnd < 0)
                return entry.getKey();
        }

        Bukkit.getLogger().warning("getRandomOre(): Falling back to stone, rnd = " + rnd);
        return Material.STONE;
    }

    public String getConfigSectionName() {
        return mat1.name() + "&" + mat2.name();
    }

    @Override
    public String toString() {
        return String.format("%s <-> %s (%d blocks)", mat1.name().toLowerCase(), mat2.name().toLowerCase(), chances.size());
    }
}
