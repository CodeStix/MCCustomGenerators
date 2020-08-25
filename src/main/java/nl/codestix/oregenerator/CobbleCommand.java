package nl.codestix.oregenerator;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class CobbleCommand implements CommandExecutor {

    public MCOreGeneratorPlugin plugin;
    private HashMap<String, BlockGenerator> selectedGenerators = new HashMap<>();

    public CobbleCommand(MCOreGeneratorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        BlockGenerator gen = selectedGenerators.get(commandSender.getName());

        if (strings.length == 3 && strings[0].equalsIgnoreCase("select")) {
            try {
                Material mat1 = Material.valueOf(strings[1].toUpperCase());
                Material mat2 = Material.valueOf(strings[2].toUpperCase());
                gen = plugin.getGenerator(mat1, mat2);
                if (gen == null) {
                    gen = new BlockGenerator(mat1, mat2);
                    plugin.gens.add(gen);

                    ConfigurationSection section = plugin.getConfig().getConfigurationSection(gen.getConfigSectionName());
                    if (section == null)
                        section = plugin.getConfig().createSection(gen.getConfigSectionName());
                    for(Map.Entry<Material,Integer> entry : BlockGenerator.DEFAULT_CHANCES.entrySet()) {
                        section.set(entry.getKey().name(), entry.getValue());
                        gen.chances.put(entry.getKey(), entry.getValue());
                    }
                    plugin.saveConfig();

                    commandSender.sendMessage("§dSelecting new generator " + gen.toString());
                }
                else {
                    commandSender.sendMessage("§dSelecting generator " + gen.toString());
                }
                selectedGenerators.put(commandSender.getName(), gen);
            }
            catch (Exception ex) {
                commandSender.sendMessage("§cCould not select generator: " + ex);
            }
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("unselect")) {
            selectedGenerators.remove(commandSender.getName());
            commandSender.sendMessage("§dDone, no generator selected.");
        }
        else if (strings.length == 1 && strings[0].equalsIgnoreCase("remove")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            commandSender.sendMessage("§dRemoving generator " + gen.toString());
            plugin.gens.remove(gen);
            plugin.getConfig().set(gen.getConfigSectionName(), null);
            plugin.saveConfig();

            selectedGenerators.remove(commandSender.getName());
        }
        else if (strings.length == 2 && strings[0].equalsIgnoreCase("unset")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            try {
                Material mat = Material.valueOf(strings[1].toUpperCase());
                commandSender.sendMessage("§dRemoving " + mat);
                gen.chances.remove(mat);
                plugin.getConfig().getConfigurationSection(gen.getConfigSectionName()).set(mat.name(), null);
                plugin.saveConfig();
            }
            catch(Exception ex) {
                commandSender.sendMessage("§cCould not remove chance: " + ex);
            }
        }
        else if (strings.length == 3 && strings[0].equalsIgnoreCase("set")) {
            if (gen == null) {
                commandSender.sendMessage("§cSelect a generator first.");
                return true;
            }
            try {
                Material mat = Material.valueOf(strings[1].toUpperCase());
                int i = Integer.parseInt(strings[2]);
                commandSender.sendMessage(String.format("§dSetting chance for %s to %d", mat.name(), i));
                gen.chances.put(mat, i);
                plugin.getConfig().getConfigurationSection(gen.getConfigSectionName()).set(mat.name(), i);
                plugin.saveConfig();
            }
            catch(Exception ex) {
                commandSender.sendMessage("§cCould not set chance: " + ex);
            }
        }
        else if (strings.length == 0) {
            if (gen == null) {
                if (plugin.gens.size() > 0) {
                    commandSender.sendMessage(String.format("§dList of %d generators:", plugin.gens.size()));
                    for(BlockGenerator g : plugin.gens) {
                        commandSender.sendMessage(g.toString());
                    }
                    commandSender.sendMessage("§8Select a generator with /cobble select <type1> <type2>");
                }
                else {
                    commandSender.sendMessage("§dYou don't have any custom generators, create one using '/cobble select <type1> <type2>'. Example:");
                    commandSender.sendMessage("/cobble select LAVA WATER");
                    commandSender.sendMessage("/cobble set STONE 100");
                    commandSender.sendMessage("/cobble set DIAMOND_ORE 20");
                    commandSender.sendMessage("(when lava and water collide, the chance STONE will generate is 100/120, and the chance that DIAMOND_ORE will generate is 20/120)");
                }
            }
            else {
                int sum = gen.getChancesSum();
                commandSender.sendMessage(String.format("§dGenerator %s: (%d chances sum)", gen.toString(), sum));
                for(Map.Entry<Material,Integer> entry : gen.chances.entrySet()) {
                    commandSender.sendMessage(String.format("%s = %d / %d = %.4f", entry.getKey().name(), entry.getValue(), sum, (float)entry.getValue() / sum));
                }
                commandSender.sendMessage("§8Set a block chance using /cobble set <type> <chance>");
                commandSender.sendMessage("§8Remove a generating block using /cobble unset <type>");
                commandSender.sendMessage("§8Deselect this generator using /cobble unselect");
                commandSender.sendMessage("§8Remove this generator using /cobble remove");
            }
        }
        else {
            commandSender.sendMessage("§cUnknown subcommand. Usage: " + command.getUsage());
        }

        return true;
    }
}
