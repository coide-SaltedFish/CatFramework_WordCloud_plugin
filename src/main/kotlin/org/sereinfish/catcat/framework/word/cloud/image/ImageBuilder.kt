package org.sereinfish.catcat.framework.word.cloud.image

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.CircleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.SqrtFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import com.microsoft.playwright.Route
import com.microsoft.playwright.options.LoadState
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.contact.Group
import org.sereinfish.cat.frame.utils.toJson
import org.sereinfish.catcat.framework.playwright.extend.buildPlaywrightElementImage
import org.sereinfish.catcat.framework.qq.group.chat.statistic.record.RecordManager
import org.sereinfish.catcat.framework.qq.group.chat.statistic.utils.toTimeText
import org.sereinfish.catcat.framework.word.cloud.image.entity.ChatRankData
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.image.BufferedImage

object ImageBuilder {

    init {
        (LoggerFactory.getLogger(WordCloud::class.java) as? Logger)?.level = Level.OFF
    }

    /**
     * 构建词云图片
     */
    fun wordCloudImage(text: String): BufferedImage {
        val wordFrequencyList = FrequencyAnalyzer().apply {
            setWordFrequenciesToReturn(600)
            setMinWordLength(2)
            setWordTokenizer(ChineseWordTokenizer())
        }.load(text.byteInputStream())

        val width = maxOf(100, minOf(wordFrequencyList.size * 5, 800))


        val wordCloud = WordCloud(Dimension(width, width), CollisionMode.PIXEL_PERFECT).apply {
            // 字体设置
            setKumoFont(KumoFont(Font("微软雅黑", Font.PLAIN, 20)))
            setPadding(0)
            setBackground(CircleBackground(width / 2))
            setColorPalette(
                ColorPalette(
                    Color(0x4055F1),
                    Color(0x408DF1),
                    Color(0x40AAF1),
                    Color(0x40C5F1),
                    Color(0x40D3F1),
                    Color(0xFFFFFF)
                )
            )
            setFontScalar(SqrtFontScalar(10, maxOf(40, minOf(wordFrequencyList.size, 80))))
            setBackgroundColor(Color.WHITE)
        }

        wordCloud.build(wordFrequencyList)
        return wordCloud.bufferedImage
    }

    /**
     * 构建水群排行榜
     */
    suspend fun chatRankImage(title: String, group: Group, data: List<RecordManager.MemberChatInfo>): ByteArray = buildPlaywrightElementImage(select = {
        waitForLoadState(LoadState.NETWORKIDLE)
        querySelectorAll(".draw_block").first()
    }, pageBuilder = {
        setViewportSize(700, 3000)

        route("**/api/chat/rank.json") { route ->
            route.fulfill(Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(ChatRankData(title, data.map {
                    val name = group.members[it.sender]?.cardNameOrRemarkNameOrNickName ?: it.sender.toString()
                    ChatRankData.ItemData(it.sender.encodeToString(), name, "消息数：${it.messages.size} 条", it.time.toTimeText())
                }).toJson())
            )
        }
        route("**/api/sender/face/**.jpg") { route ->
            try {
                val id = route.request().url().substringAfter("api/sender/face/").substringBefore(".")
                group.members[group.bot.decodeContactId(id)]?.queryFaceImage()
            }finally {
                ImageBuilder::class.java.classLoader.getResourceAsStream("html/image/headimg_dl.png")?.readBytes()
            }?.let { imageByteArray ->
                route.fulfill(Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("image/png")
                    .setBodyBytes(imageByteArray)
                )
            }
        }
        route("http://127.0.0.1:8080/html/**") { route ->
            val path = route.request().url().substring("http://127.0.0.1:8080/".length)

            ImageBuilder::class.java.classLoader.getResourceAsStream(path)?.use {
                route.fulfill(
                    Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType(when(path.substringAfter(".")){
                        "js" -> "application/javascript"
                        "css" -> "text/css"
                        "png" -> "image/png"
                        else -> "text/html"
                    })
                    .setBodyBytes(it.readBytes())
                )
            }
        }
    }){
        htmlBuilder {
            string {
                + ImageBuilder::class.java.classLoader.getResource("html/ChatRank.html")!!.readText()
            }
        }
    }
}