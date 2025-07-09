# Ideas!
* Storage Vessel
  * Look into Continuation Frames rather than a new Env 
  * Add Costs (for real)
  * Finish documentation
* Crafting ???
  * Define a pattern either in GUI, or with Hex (via Item Types)
  * When the Media Wave runs over the Crafting ??? it tries to craft the items in the Storage Vessels into that
  * Pushes a true boolean to the stack if successful, or false if not
* Ambit Extenders
  * Pops a vector from the Stack, and extends the Ambit by that much
* Ambit Portals
  * Takes a pair of blocks, and gives the Circle a small radius of Ambit around the "output"
* Circle Macros
  * Takes a Pattern, and a Hex, and makes that pattern into a Macro for the Spell Circle
* D.C. al Coda
  * Being able to "jump" to an old point of the Spell Circle (runs over the patterns again, doesn't fuck around with Stack/Evals)


## TODO:
* Finish the documentation for:
  * Storge Vessels
  * Ambit Extenders
* Fix the Ambit Extender's media costs
* work on the unfinished stuff (mostly the Crafting ???)

For the Crafting ??? extend `LootableContainerBlockEntity` and use `Generic3x3ContainerScreenHandler`