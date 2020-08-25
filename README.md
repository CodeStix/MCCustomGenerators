# MCCustomGenerators
A bukkit plugin to create any variation of the default cobblestone generator!

## Installation
1. Download the plugin [here](https://github.com/CodeStix/MCCustomGenerators/releases). (pick the right one for your server version)
2. Place the downloaded jar file in your server's `plugins` folder.
3. Reload/restart your server

## Usage
`/cobble [list | select <block1> <block2> | deselect | info | remove | set <block> <chance> | unset <block> | particle <name> [count] [speed]]`
- `list`: Show all the created generators.
- `select <block1> <block2>`: Select (or create) the generator that is activated by `block1` and `block2`. For example: `/cobble select lava water` to select the default cobblestone generator. Either `block1` or `block2` must be a liquid.
- `deselect`: Deselect the selected generator.
- `remove`: Remove the selected generator.
- `info`: Show information about the selected generator.
- `set <block> <chance>`: Set the chance for the selected generator to generate `block`. The chance is calculated by (block / sum of all block chances).
- `unset <block>`: Do not generate `block` in the selected generator.
- `particle <name> [count] [speed]`: Set the particle that will spawn when a block is generated in the selected generator.