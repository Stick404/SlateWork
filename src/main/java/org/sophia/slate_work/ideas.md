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
* Circle Macros `ALMOST DONE`
  * Takes a Pattern, and a Hex, and makes that pattern into a Macro for the Spell Circle
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
* Use `FrameSearch` instead of Jank VM stuff

## TODO:
* Make ~~Hex Pose~~ Hexpose required
* Make the Set Macro spell
* Make the Macro Loci docs
* Document Allay Pigment
* Work on the Patterned Assembler more
  * Finish up the inv texture *maybe*