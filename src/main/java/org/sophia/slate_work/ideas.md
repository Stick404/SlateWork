# Ideas!
* Storage Vessel
  * Look into Continuation Frames rather than a new Env 
  * Add Costs (for real)
  * Finish documentation
* Patterned Assemblers
  * Define a pattern either in GUI, or with Hex (via Item Types)
  * When the Media Wave runs over the Crafting ??? it tries to craft the items in the Storage Vessels into that
  * Pushes a true boolean to the stack if successful, or false if not
* Ambit Extenders
  * Pops a vector from the Stack, and extends the Ambit by that much
* Vector Dirx
  * Pops a vector from the Stack, and tries to move the Media Wave that way
* Ambit Portals
  * Takes a pair of blocks that "point" to each other, and gives a small radius of ambit around the "output" portal (must be pointing back to the first one)
* Circle Macros
  * Takes a Pattern, and a Hex, and makes that pattern into a Macro for the Spell Circle
* D.C. al Coda
  * Being able to "jump" to an old point of the Spell Circle (runs over the patterns again, doesn't fuck around with Stack/Evals)


## TODO:
* Finish the documentation for:
  * Storge Vessels
  * Ambit Extenders
  * Patterned Assemblers
  * Wave Regulator (About done)
  * Patterns
    * Wave Position
    * Wave Normal
    * (Circle) Media Reflection
* Work on the Patterned Assembler more
  * Make the Set Recipe spell
  * Finish up the inv texture
* MAKE THE BLOCKS DROP ITEMS
* Maybe finish `FrameSearch`