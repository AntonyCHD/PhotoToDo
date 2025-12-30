# 🚀 PhotoDo v2.0 更新日志 (分支: 0x01)

> **版本**: v2.0 Release
> **代号**: AI & Experience Upgrade
> **开发小组：**软件挖坑王
> **时间**: 2025-12-30

---

## 🌟 核心亮点 (Highlights)

本次迭代（0x01 分支）主要聚焦于 **AI 智能化集成**、**相机交互升级** 以及 **UI/UX 深度重构**。我们将 PhotoDo 从一个简单的 OCR 工具升级为具备语义理解能力的智能助手。

### 1. 🤖 接入 Qwen 大模型 (AI Intelligence)
* **集成硅基流动 (SiliconFlow) API**：引入 Retrofit + OkHttp 网络层，对接 Qwen/Qwen2.5-7B-Instruct 模型。
* **智能语义解析**：替代了旧版的正则表达式解析。现在 AI 能精准识别“标题”、“时间”、“地点”。
* **上下文感知**：通过 Prompt Engineering (提示词工程)，将“当前日期”注入 AI 上下文。
    * *效果*：AI 现在能理解“明天”、“下周三”具体是哪一天，并自动转换为 `YYYY-MM-DD` 标准格式。
* **JSON 结构化输出**：强制 AI 输出纯净的 JSON 格式，实现了数据的自动回填。

### 2. 📷 专业级相机体验 (Camera Pro)
* **双指缩放 (Pinch-to-Zoom)**：集成了 `ScaleGestureDetector`，支持无级变焦，轻松捕捉远处文字。
* **点击对焦 (Tap-to-Focus)**：集成了 `FocusMeteringAction`，支持点击屏幕任意位置进行对焦、曝光和白平衡锁定 (AF/AE/AWB)。
* **UI 升级**：保留了沉浸式取景框和参考线，提升拍摄沉浸感。

### 3. 🎨 UI/UX 交互重构
* **悬浮式导航 (Floating Action Button)**：
    * 底部导航栏重构，中间升级为**凸起的紫色大圆形按钮**。
    * **交互创新**：
        * **短按**：快速进入相机拍照模式。
        * **长按**：触发“手动创建模式”，无需拍照即可输入日程。
* **日历页优化**：
    * 优化了顶部布局，采用 Material Design 风格的圆形翻页按钮。
    * 调整了“今天”按钮的位置，布局更加符合人体工学。
* **防误触优化**：
    * 通过反射修改 `ViewPager2` 的 `TouchSlop` 灵敏度，解决了在滑动列表时容易误触导致左右切页的问题。

---

## 🛠️ 技术细节 (Technical Details)

### 🏗️ 架构升级
* **数据流 (Reactive Data Flow)**：
    * 将 `Room` 数据库查询全面升级为 Kotlin `Flow`。
    * 实现了数据的**实时响应式刷新**。现在新增、删除任务后，所有页面（首页、日历、记录）无需手动刷新，UI 会自动同步。
* **依赖库更新**：
    * 新增 `com.squareup.retrofit2:retrofit:2.9.0` (网络请求)
    * 新增 `com.squareup.retrofit2:converter-gson:2.9.0` (JSON 解析)
    * 新增 `com.squareup.okhttp3:logging-interceptor` (网络调试)

### 🐛 问题修复 (Bug Fixes)
* ✅ **修复**：在“日历页”和“今日待办页”点击删除按钮无反应的问题。
* ✅ **修复**：不同页面间数据状态不同步的问题。
* ✅ **修复**：CameraX 预览在某些设备上无法对焦模糊的问题。
* ✅ **修复**：`fragment_calendar.xml` 头部 XML 声明格式错误导致的编译失败。

---

## 📦 发布信息 (Release Info)

* **Version Code**: 2
* **Version Name**: 2.0
* **Package Type**: Signed APK (Release)
* **Signature**: V1 + V2 Signature Scheme

---

## 📸 效果演示

*(建议在此处附上 1-2 张新版 UI 的截图，例如 AI 分析弹窗、悬浮大按钮效果)*

---

### 📝 下一步计划 (Next Steps)
* [ ] **用户系统**：引入 User 表，实现多用户数据隔离与云端同步 (Plan Item 1)。
* [ ] **百度 OCR (可选)**：作为备用 OCR 引擎引入，提升特殊字体的识别率。