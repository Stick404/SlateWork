# Ideas!
* Storage Vessel `DONE`
  * Look into Continuation Frames rather than a new Env 
  * Add Costs (for real)
  * Finish documentation
* Patterned Assemblers `DONE`
  * Define a pattern either in GUI, or with Hex (via Item Types)
  * When the Media Wave runs over the Patterned Assembler it tries to craft the items in the Storage Vessels into that
  * Pushes a true boolean to the stack if successful, or false if not
  * "Enlightened Mode," takes a hex like the rest of the Vessel stuff, and must return a number, this would be the slot to set with that item. -1 would mean to not do anything with that item `NOT DONE`
* Ambit Extenders `DONE`
  * Pops a vector from the Stack, and extends the Ambit by that much
* Circle Macros `DONE`
  * Takes a Pattern, and a Hex, and makes that pattern into a Macro for the Spell Circle
* Broadcaster
  * Prop Iota like block, but in slate form. Pops an iota from the stack, and takes no ambit to read
* Straight Slate
  * Slate that only works in a straight direction
* Accelerator
  * (From Gloop) takes a bit of Media, and accelerates the Media Wave for a few blocks
* Handed Slate
  * Lets Spell Circles hold invs/hands
  * (if you put 2 together, maybe get a secret advancement)
* Vector Dirx
  * Pops a vector from the Stack, and tries to move the Media Wave that way
* Binding any entity to a Spell Circle
* Ambit Portals
  * Takes a pair of blocks that "point" to each other, and gives a small radius of ambit around the "output" portal (must be pointing back to the first one)
* D.C. al Coda
  * Being able to "jump" to an old point of the Spell Circle (runs over the patterns again, doesn't fuck around with Stack/Evals)

### New Media Gen
Takes a processing chain of Hex related stuff to make. Possible ideas are: crafting -> fire -> break block -> recharge -> Energized by a circle.
This is to limit the speed of media gen, while having something as good as allays without the lag.

## TODO:
* Work on the Patterned Assembler more maybe
  * Finish up the inv texture *maybe*

## Problems:

