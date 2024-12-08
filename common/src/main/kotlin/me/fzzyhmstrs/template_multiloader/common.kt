package me.fzzyhmstrs.template_multiloader

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.slf4j.Logger


object Mod {
    const val ID = "template_multiloader"
    val LOGGER: Logger = LoggerFactory.getLogger(ID)
    val DEVLOG: Logger = ConfigApi.platform().devLogger(ID)

    //
    ///
    ///
    //

    fun identity(path: String): Identifier {
        return Identifier.of(ID, path)
    }

    fun init() {}
}

object ModClient {


    fun init() {}
}