package me.fzzyhmstrs.template_multiloader

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus


@net.neoforged.fml.common.Mod(Mod.ID)
class ModNeoforge(bus: IEventBus) {
    init {
        Mod.init()




    }

}

@net.neoforged.fml.common.Mod(Mod.ID, dist = [Dist.CLIENT])
class ModNeoForgeClient(bus: IEventBus) {

    init {
        ModClient.init()




    }


}