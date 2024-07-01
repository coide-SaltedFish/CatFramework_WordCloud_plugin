package org.sereinfish.catcat.framework.word.cloud.event

import com.google.gson.JsonParser
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.Message
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageContent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.At
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.*
import org.sereinfish.cat.frame.context.property.value
import org.sereinfish.cat.frame.utils.logger
import org.sereinfish.catcat.framework.eventhandler.extend.build.*
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEvent
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageChainToMessageContent
import org.sereinfish.catcat.framework.qq.group.chat.statistic.db.entity.MessageRecord
import org.sereinfish.catcat.framework.qq.group.chat.statistic.record.RecordManager
import org.sereinfish.catcat.framework.qq.group.chat.statistic.utils.getTodayStartTime
import org.sereinfish.catcat.framework.word.cloud.image.ImageBuilder
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class WordCloudEvent: CatEvent {
    val logger = logger()

    @CatEvent.Catch
    fun catch() = buildCatchHandler<MessageEvent, Exception> {
        logger.error("异常", it.exception)
    }

    @CatEvent.Handler
    fun wordCloud() = buildEventHandler<GroupMessageEvent>(builder = {
        router {
            + optional {
                + at(it.bot) + spaces()
            }
            + "今日词云"
            + end()
        }
    }) {
        logger.info("尝试生成[$sender]今天在群[$group]的聊天词云")

        val records = RecordManager.find(MessageRecord.RecordType.GROUP, sender = sender.id, groupId = group.id, getTodayStartTime())
        val messageText = buildString {
            records.forEach {
                JsonParser.parseString(it.message).asJsonArray.forEach {
                    when(it.asJsonObject.get("type").asString){
                        "text" -> append(it.asJsonObject.get("data").asJsonObject.get("text").asString)
                        "at" -> {
                            val id = it.asJsonObject.get("data").asJsonObject.get("qq").asLong
                            group.members[id]?.let {
                                append("@${it.cardNameOrRemarkNameOrNickName}")
                            } ?: append("@${id}")
                        }
                    }
                }
            }
        }
        if (messageText.length < 150)
            reply("当前聊天记录过少，无法生成词云")
        else {
            val imageOutputStream = ByteArrayOutputStream()
            ImageIO.write(ImageBuilder.wordCloudImage(messageText), "png", imageOutputStream)

            reply(externalResource(imageOutputStream.toByteArray().inputStream()).uploadAsImage())
        }
    }

    @CatEvent.Handler
    fun wordCloudTest() = buildEventHandler<GroupMessageEvent>(builder = {
        filter { event.sender.id == 768856606L }
        router {
            + optional {
                + at(it.bot) + spaces()
            }
            + "今日词云" + spaces()
            + parameter("at", type<At>()) + spaces()
            + end()
        }

        typeHandler(MessageChainToMessageContent)
    }) {
        val at by it.value<At>()

        logger.info("尝试生成[${group.members[at.target]}]今天在群[$group]的聊天词云")

        val records = RecordManager.find(MessageRecord.RecordType.GROUP, sender = at.target, groupId = group.id, getTodayStartTime())
        val messageText = buildString {
            records.forEach {
                JsonParser.parseString(it.message).asJsonArray.forEach {
                    if (it.asJsonObject.get("type").asString == "text") {
                        append(it.asJsonObject.get("data").asJsonObject.get("text").asString)
                    }
                }
            }
        }
        if (messageText.length < 150)
            reply("[${group.members[at.target]}]当前聊天记录过少，无法生成词云")
        else {
            val imageOutputStream = ByteArrayOutputStream()
            ImageIO.write(ImageBuilder.wordCloudImage(messageText), "png", imageOutputStream)

            reply(externalResource(imageOutputStream.toByteArray().inputStream()).uploadAsImage())
        }
    }

    @CatEvent.Handler
    fun chatRank() = buildEventHandler<GroupMessageEvent>(builder = {
        router {
            + optional {
                + at(it.bot) + spaces()
            }
            + "今日水群排行榜"
            + end()
        }
    }){
        reply(externalResource(
            ImageBuilder.chatRankImage(
                "今日水群排行榜",
                group,
                RecordManager.getGroupTodayMessageTime(group.id).take(10)).inputStream()
        ).uploadAsImage())
    }

    @CatEvent.Handler
    fun chatYesterdayRank() = buildEventHandler<GroupMessageEvent>(builder = {
        router {
            + optional {
                + at(it.bot) + spaces()
            }
            + "昨日水群排行榜"
            + end()
        }
    }){
        val time = getTodayStartTime().let {
            (it.first - (24 * 60 * 60 * 1000)) .. (it.last - (24 * 60 * 60 * 1000))
        }
        reply(externalResource(
            ImageBuilder.chatRankImage(
                "昨日水群排行榜",
                group,
                RecordManager.getGroupMessageStatic(group.id, time).take(10)
            ).inputStream()
        ).uploadAsImage())
    }
}