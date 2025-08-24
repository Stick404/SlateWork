# Ideas!
### Styles:
* Slate is the default
* Allay colors, "meta" circle stuff (media wave speed, ambit, etc)
* Edified Wood, storage/"memory" maybe?
* Copper colors, more "high impact" loci. Such as speeding up the Media wave *greatly*

## Loci Ideas!
* Storage Vessel `DONE`
  * Look into Continuation Frames rather than a new Env 
  * Add Costs (for real)
  * Finish documentation
* Patterned Assemblers `DONE`
  * Define a pattern either in GUI, or with Hex (via Item Types)
  * When the Media Wave runs over the Patterned Assembler it tries to craft the items in the Storage Vessels into that
  * Pushes a true boolean to the stack if successful, or false if not
* Ambit Extenders `DONE`
  * Pops a vector from the Stack, and extends the Ambit by that much
* Circle Macros `DONE`
  * Takes a Pattern, and a Hex, and makes that pattern into a Macro for the Spell Circle
* Broadcaster `DONE`
  * Prop Iota like block, but in slate form. Pops an iota from the stack, and takes no ambit to read
* Inventory Locus `DONE`
  * Gives the Spell Circle 6 slots of inventory to work with, can select the held slot via a Spell (1-6)
  * Likely have a reflection spell to return the item stacks in the Locus
  * Make it hopper compatible!
* Redstone Locus `DONE`
  * Outputs redstone for a few seconds after being ran over (can be toggled to use top of the stack number, or 15)
* Accelerator `DONE`
  * (From Gloop) takes a bit of Media, and accelerates the Media Wave for a few blocks
* Straight Slate
  * Slate that only works in a straight direction
* Trading set of Loci
  * Can Trade Dirx
    * Goes down either to the left or right based on if it can or cant trade
  * Buy Locus
    * Tries to buy a trade based on the items set in it (no GUI?)
    * Mishaps if it cant maybe
* Warp Tunnel(?)
  * Instantly moves the Media Wave down a line of connected (CAN NOT TURN)
  * Could be copper lined
* In World Crafting
  * https://discord.com/channels/@me/1386726525633560620/1409191847288508516
* ~~Gemini Dirx~~
  * ~~Yep.~~ Can **not** be done without removing parity between Circle PR and Base Hex

## Impti Ideas!
* Chat Impti `Done`
  * Takes a chat message, and starts it on the stack

## Dirx Ideas!
* Try-Catch Dirx
    * Goes forward, running the Hex as normal, but when it encounters a Mishap, it "teleports" back to the Dirx, and goes down the path to the left
    * Mixin to:
        * `CircleEnv#postExecution`, to not post the Mishap
        * `CircleExecutionState#tick`, to ignore the "Stop," and to change the "currentPos"

Maybe make a full Abstract Locus class that contains helpers/common code for Locus blocks.

Maybe could do some "fucky" custom scanning for `CircleExecutionState`, to allow for Syntrexs and stuff akin to it. 
This would likely be checking over the list again and checking inherited blocks. Would be the best to just change the scanning, but that removes the parity between Circle PR and Base Hex...
## TODO:
* maybe reword the Gloopy Accelerator page, so its a bit clearer on the speed changes

## Problems: