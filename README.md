![alt tag](https://dl.dropboxusercontent.com/u/101919880/Banners/Thaumic%20Infusion.png)
===============

Welcome to the Thaumic Infusion Repository, first of this mod IS open-source. Feel free to take, re-produce, build and do anything with it as you wish, I only ask that you leave a note that the code originated from this repo.

About This Mod
--------------

Thaumic Infusion (TI) is a magical mod, all about adding in a whole new mechanic into Minecraft. It expands upon the idea of infusion, that was explained in Thaumcraft and brings reality to it. For it says in the thaumonomicon, that upon infusing a feather into a block, that block would become as light. TI aims to deliver just that, but instead of items it works with aspects and essentially allows you to take any aspect, then infuse it into a block. Each aspect has it's own personal effect, these effects can just change a single property about the block or add in a complete other mechanic into it.

So, an example of this would be: If you were to infuse the aspect Lux into a block, the said block would then emit light when placed in the world. If you infuse Aqua, said block would then become a tank. The list of all the effects go on.

Setting Up Enviorment
--------------

Fork this Repository & then clone/download it somewhere to your computer, then simply setup the enviorment by running the following command:

`gradlew setupDecompWorkSpace`

Then depending on your choice of IDE, run:

`gradlew idea or gradlew  eclipse`

Once your enviorment is setup, edit your run configurations and in the VM arugments paste this in:

`-Dfml.coreMods.load=drunkmafia.thaumicinfusion.common.asm.ThaumicInfusionPlugin`

This will let forge know that this is also a core mod, which will allow the Transformers that the Mod uses to run. Otherwise, non of the effects will work when you infuse them into a block.

If you make changes to the code, just make a pull request with a description of the changes and why they are there. If everything checks out, then I will accept the pull request.

Localizing
--------------

So for those who are interested in lending a helping hand to the development of my mod and want to make it more accessible to those whom do not speak english, then you can help localize it to other languages.

To do this, just fork this repo and then use the [en_US.lang](https://github.com/TheDrunkMafia/ThaumicInfusion/blob/master/src/main/resources/assets/thaumicinfusion/lang/en_US.lang)  file as a basis of the localizations, then simply make a pull request.
