package org.sereinfish.catcat.framework.word.cloud.image.entity

data class ChatRankData(
    val title: String,
    val list: List<ItemData>
){
    data class ItemData(
        val senderId: Long,
        val senderName: String,
        val chatCount: String,
        val chatTime: String,
    )
}
