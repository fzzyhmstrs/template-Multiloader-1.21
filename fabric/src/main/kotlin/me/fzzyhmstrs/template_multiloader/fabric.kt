package me.fzzyhmstrs.template_multiloader

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer


object ModFabric: ModInitializer {

    override fun onInitialize() {
        Mod.init()
    }


}

object ModFabricClient: ClientModInitializer {

    override fun onInitializeClient() {
        ModClient.init()
    }


}