package com.example.photodo

import java.util.regex.Pattern

object SmartParser {

    /**
     * 核心算法：输入一长串乱七八糟的文字，输出一个整洁的 TaskInfo 对象
     */
    fun parseTextToTask(text: String): TaskInfo {
        val task = TaskInfo()
        task.rawContent = text

        // 1. 提取标题 (简单策略：取第一行有效文本)
        val lines = text.split("\n").filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            // 比如你的例子中，第一行是"亲爱的同学们..."，第二行是"第四十一届..."
            // 我们可以取字数较长且包含"大赛"、"会议"、"通知"的那一行，或者直接取前两行拼起来
            task.title = lines.find { it.contains("大赛") || it.contains("活动") } ?: lines[0]
        }

        // 2. 提取日期 (正则：匹配 2025年12月19日 或 12月19日)
        // \d{4} 表示4个数字，\d{1,2} 表示1到2个数字
        val datePattern = Pattern.compile("(\\d{4}年)?\\d{1,2}月\\d{1,2}日")
        val dateMatcher = datePattern.matcher(text)
        if (dateMatcher.find()) {
            task.date = dateMatcher.group() // 找到第一个匹配的日期，比如 "2025年12月19日"
        }

        // 3. 提取时间 (正则：匹配 15:00 或 18:00-22:00)
        // \d{1,2}:\d{2} 表示 12:30 这种格式
        val timePattern = Pattern.compile("\\d{1,2}:\\d{2}(-\\d{1,2}:\\d{2})?")
        val timeMatcher = timePattern.matcher(text)
        if (timeMatcher.find()) {
            task.time = timeMatcher.group() // 比如 "15:00"
        }

        // 4. 提取地点 (正则：匹配 "地点" 或 "地址" 或 "位置" 后面的文字)
        // (?<=...) 是“后顾断言”，意思是查找“地点”后面的内容
        // [：:]? 匹配中文或英文冒号，或者没有冒号
        // (.*) 捕获整行内容
        val locationPattern = Pattern.compile("(?<=[地Loc][点址ation])[:：]?\\s*(.*)")
        val locationMatcher = locationPattern.matcher(text)
        if (locationMatcher.find()) {
            // group(1) 是括号里匹配到的内容
            task.location = locationMatcher.group(1)?.trim() ?: ""
        } else {
            // 如果没找到明确的【地点】标签，尝试找包含"楼"、"室"、"厅"的行
            val implicitLoc = lines.find { it.contains("楼") || it.contains("室") || it.contains("厅") }
            if (implicitLoc != null) {
                // 简单的去噪，去掉前面的"地点"两个字（如果有的话）
                task.location = implicitLoc.replace("【地点】", "").replace("地点", "").trim()
            }
        }

        return task
    }
}