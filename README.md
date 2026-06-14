# HTML Quick View

一个Android离线HTML文件管理与预览工具，帮助用户高效管理本地HTML文件、创建离线知识库。

## 功能特点

### 文件管理
- **文件夹管理** - 创建多层文件夹结构组织HTML文件
- **标签系统** - 为文件添加彩色标签便于分类
- **全文搜索** - 支持文件名和文件内容的全文搜索

### 离线保存
- **网页保存** - 一键将网页保存为离线版本
- **资源打包** - 自动下载并打包HTML、CSS、图片、JS等所有资源
- **WebArchive** - 保存完整的网页快照

### 预览与批注
- **HTML预览** - 内置WebView高效预览HTML文件
- **批注功能** - 为文件添加文字批注和标注

### 接收分享
- **分享接收** - 接收其他应用分享的HTML文件
- **快捷方式** - 支持创建桌面快捷方式

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Repository
- **数据库**: Room
- **依赖注入**: KSP
- **最低版本**: Android 8.0 (API 26)
- **目标版本**: Android 14 (API 34)

## 项目结构

```
app/src/main/java/com/example/htmlquickview/
├── database/          # Room数据库和DAO
├── model/              # 数据模型
├── repository/         # 数据仓库
├── service/            # 业务服务
├── ui/
│   ├── component/      # Compose UI组件
│   ├── screen/         # 页面屏幕
│   └── theme/          # 主题配置
├── util/               # 工具类
└── viewmodel/          # ViewModel
```

## 构建

```bash
# Debug构建
./gradlew assembleDebug

# Release构建
./gradlew assembleRelease
```

Release构建需要配置签名密钥，请在 `app/build.gradle.kts` 中配置您的签名信息。

## 许可证

本项目基于 MIT 许可证开源。
