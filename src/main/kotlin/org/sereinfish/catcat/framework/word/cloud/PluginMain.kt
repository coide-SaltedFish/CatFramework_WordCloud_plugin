package org.sereinfish.catcat.framework.word.cloud

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.BotManager
import org.sereinfish.cat.frame.plugin.Plugin
import org.sereinfish.cat.frame.timer.CatTimer
import org.sereinfish.cat.frame.timer.SimpleCatTimerTask
import org.sereinfish.cat.frame.timer.TimerData
import org.sereinfish.catcat.framework.qq.group.chat.statistic.record.RecordManager
import org.sereinfish.catcat.framework.word.cloud.image.ImageBuilder

object PluginMain: Plugin {
    override fun start() {
        // 初始化定时器
        CatTimer.excute(SimpleCatTimerTask(
            timerData = TimerData(hour = 23, minute = 0, second = 0, millisecond = 0)
        ){
            // 遍历群聊
            BotManager.bots.values.forEach { bot ->
                bot.groups.values.forEach { group ->
                    // 判断是否已开启
                    if (PluginMain.config["group.timedNotifications.${group.id}"] == true){
                        // 发起通知
                        group.sendMessage(bot.externalResource(
                            ImageBuilder.chatRankImage(
                                "今日水群排行榜",
                                group,
                                RecordManager.getGroupTodayMessageTime(bot, group.id).take(10)).inputStream()
                        ).uploadAsImage())
                    }
                }
            }
        })

        logger.info("词云插件已引入")
    }
}