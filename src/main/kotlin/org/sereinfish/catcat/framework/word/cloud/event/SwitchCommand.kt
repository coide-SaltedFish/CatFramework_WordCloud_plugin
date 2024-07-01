package org.sereinfish.catcat.framework.word.cloud.event

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.at
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.parameter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.regex
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.spaces
import org.sereinfish.cat.frame.context.property.value
import org.sereinfish.catcat.framework.eventhandler.extend.build.buildEventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.build.reply
import org.sereinfish.catcat.framework.eventhandler.extend.build.router
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEvent
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageChainToMessageContent
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageContentToString
import org.sereinfish.catcat.framework.word.cloud.PluginMain

class SwitchCommand: CatEvent {

    @CatEvent.Handler
    fun timedNotifications() = buildEventHandler<GroupMessageEvent>(builder = {
        router {
            + at(it.bot) + spaces()
            + "水群时长通知" + spaces()
            + parameter("value"){
                regex("(?i)on".toRegex()) or regex("(?i)off".toRegex())
            }
        }

        typeHandler(MessageChainToMessageContent)
        typeHandler(MessageContentToString)
    }) {
        val value by it.value<String>()

        val switchStat = value == "on"

        PluginMain.config["group.timedNotifications.${group.id}"] = switchStat

        reply("已设置本群水群时长通知为：$switchStat")
    }
}