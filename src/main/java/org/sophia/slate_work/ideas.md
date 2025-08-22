# Ideas!
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
* Inventory Locus
  * Gives the Spell Circle 6 slots of inventory to work with, can select the held slot via a Spell (1-6)
  * Likely have a reflection spell to return the item stacks in the Locus
  * Make it hopper compatible!
* Redstone Locus
  * Outputs redstone for a few seconds after being ran over (can be toggled to use top of the stack number, or 15)
* Random Tick Locus
  * Gives the block its facing 1 Random tick, cost scales as it is used. And resets when the Circle ends
* Accelerator
  * (From Gloop) takes a bit of Media, and accelerates the Media Wave for a few blocks
* Straight Slate
  * Slate that only works in a straight direction
* Vector Dirx
  * Pops a vector from the Stack, and tries to move the Media Wave that way
* Binding any entity to a Spell Circle
* Ambit Portals
  * Takes a pair of blocks that "point" to each other, and gives a small radius of ambit around the "output" portal (must be pointing back to the first one)
* D.C. al Coda
  * Being able to "jump" to an old point of the Spell Circle (runs over the patterns again, doesn't fuck around with Stack/Evals)


Maybe make a full Abstract Locus class that contains helpers/common code for Locus blocks.

## TODO:
* Hatbar Loci
  * Make the Book Pages
* Redstone Pulser (Redstone Loci)
  * Make the Book Pages
* Accelerator
  * Make the Book Pages (reference Gloop in there. Likely "a slightly gloop aura emanates from this." )
* Akashic Record
  * Book Pages


Changed in 1.0:
* Added some missing translation keys
* Made the Storage Vessel compatible with hoppers
* TODO: ADD IN THE OTHER BLOCKS WHEN THEY ARE DONE
* A few small bug fixes and print statements

## Problems:

