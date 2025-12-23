package com.example.photodo

import java.io.Serializable

// 数据模型：用来存放解析后的结果
data class TaskInfo(
    var title: String = "",
    var date: String = "",
    var time: String = "",
    var location: String = "",
    var rawContent: String = "" // 保留原始识别文本，以防解析错误用户想自己看
) : Serializable