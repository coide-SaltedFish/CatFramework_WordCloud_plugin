
document.addEventListener("DOMContentLoaded", (event) => {
    // 请求数据
    fetch('http://127.0.0.1:8080/api/chat/rank.json')
        .then(response => response.json())
        .then(data => {
            // 设置标题
            document.querySelector(".rank_title").innerHTML = data.title;
            // 设置列表
            data.list.forEach((item, index) => {
                document.getElementById("rank_list").innerHTML += `
                    <div class="rank_item" id="rank_item_${index}">
                        <img class="sender_face" src="http://127.0.0.1:8080/api/sender/face/${item.senderId}.jpg">
                        <div class="sender_info">
                            <div class="sender_name">${item.senderName}</div>
                            <div class="chat_info">
                                <div class="desc_text chat_count">${item.chatCount}</div>
                            </div>
                        </div>
                        <div class="chat_time">${item.chatTime}</div>
                        <div class="rank_index" >${index + 1}</div>
                    </div>
                `
                // 设置列表背景
                var style = document.createElement('style');
                style.innerHTML = `
                    #rank_item_${index} {
                        background-image: linear-gradient(to left, rgb(255, 255, 255), rgba(180, 180, 180, 0.5)), url('http://127.0.0.1:8080/api/sender/face/${item.senderId}.jpg');
                    }
                `;
                document.head.appendChild(style);
            })

            document.querySelectorAll(".rank_index").forEach((item) => {
                item.style.backgroundColor = getRandomColor(100);
            })
        })
        .catch(error => {
            console.error('Error:', error);
        });
})


function getRandomColor(alphe) {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    // 加入颜色的透明度，传入的透明度为0-255
    if (alphe) {
        color += alphe.toString(16);
    }
    return color;
}