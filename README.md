# MT-KimiTranslationEngine

> **最后更新：2026-09-20** ｜ Kimi 的模型生命周期变动很快，本文档的模型信息以 [Kimi 官方模型列表](https://platform.kimi.com/docs/models) 为准。

MT 管理器（MT Manager）翻译插件，基于 MT 官方 DeepSeek 翻译插件改造，把翻译引擎换成 **Kimi（Moonshot AI）**。

## 目录结构

| 目录 | 说明 |
|---|---|
| `V3/` | **当前版本**。基于 AGP 构建（dex 模式），插件 ID 为 `by.plugin.translator.kimi` |
| `V2/` | 早期版本存档（MT 插件 v2 SDK，插件 ID `BY.plugin.kimi.translate`，v1.2），已停止维护。使用的是 v2 时代的模型名，**不保证可用** |

## 一、功能

- **API Key 自行填写**，一键发起翻译测试
- **模型名自定义输入**：插件不写死模型，想用哪个填哪个
- **温度可选**：留空 = 请求中不带 `temperature` 字段，跟随模型默认值（推荐保持留空）
- **申请地址 / GitHub / 可用模型说明** 三个入口可直接跳转
- 支持 MT 的批量翻译（分隔符模式）；单次翻译文本上限 10000 字符

## 二、安装

1. 下载发布包 `by.plugin.translator.kimi.mtp`（或自行编译，见第七节）
2. 用 MT 管理器打开该 `.mtp` 文件，按提示安装
3. 本插件 ID 与官方 DeepSeek 插件不同，**可以共存，互不覆盖**

## 三、使用步骤

1. 申请 API Key：<https://platform.kimi.com/console/api-keys>
2. 打开 **MT 管理器 → 侧边栏 → 工具 → 插件管理 → Kimi 翻译 → 设置**
3. 填写 **API Key**
4. 填写 **模型**（务必看第四节，别填已下线的模型）
5. 点 **翻译测试**，出现 `apple → 苹果` 之类结果即表示配置成功
6. 翻译时在翻译引擎列表里选择 **Kimi 翻译**

设置页项目顺序：`API Key` → `模型` → `温度` → `翻译测试` → `申请地址` → `GitHub 详情` → `可用模型`

## 四、模型选择（2026-09 已大幅变动，重要）

### ✅ 当前可用

| 模型 | 上下文 | 特点 | 翻译场景建议 |
|---|---|---|---|
| `kimi-k3` | 1M | 旗舰，始终推理，支持 `reasoning_effort` | 质量优先；慢、贵 |
| `kimi-k2.6` | 256K | 支持思考/非思考切换，支持视觉输入 | **性价比首选**（翻时关掉思考） |
| `kimi-k2.7-code` | 256K | 编程向，始终思考且不可关闭 | 不建议用于翻译 |
| `kimi-k2.7-code-highspeed` | 256K | 同上，输出速度更快 | 不建议用于翻译 |

### ❌ 已下线（填了会直接报“模型不存在”，请检查并清理你配置里的这些名字）

| 模型 | 下线时间 |
|---|---|
| `moonshot-v1-8k`、`moonshot-v1-32k`、`moonshot-v1-128k`、`moonshot-v1-auto`、`moonshot-v1-*-vision-preview` | 2026-08-31 |
| `kimi-k2.5` | 2026-08-31 |
| `kimi-k2-0711-preview`、`kimi-k2-0905-preview`、`kimi-k2-turbo-preview`、`kimi-k2-thinking`、`kimi-k2-thinking-turbo` | 2026-05-25 |
| `kimi-latest` | 2026-01-28 |
| `kimi-thinking-preview` | 2025-11-11 |

> ⚠️ 旧版 README 里推荐的 **K2 Turbo / K2 0905 / K2.5 / Moonshot V1** 全部已下线。如果你之前照旧文档填过这些模型名，请改成上表中的可用模型。

## 五、温度设置（旧文档的说法已作废）

Kimi 官方参数表明确标注**当前模型的 `temperature` 不可修改**：

| 模型 | temperature |
|---|---|
| `kimi-k3` | 固定 `1.0` |
| `kimi-k2.7-code` / `kimi-k2.7-code-highspeed` | 固定 `1.0` |
| `kimi-k2.6` | 思考模式 `1.0` / 非思考模式 `0.6` |

传入其他值会直接返回 `invalid_request_error`，官方建议**不要显式传这个参数**。

因此本插件的温度设置行为是：

- **留空（默认）**：请求里不发送 `temperature`，完全跟随模型默认 —— 推荐，也是最安全的选择
- **填了值**：请求会带上该值 —— 只在你使用中转/代理 API、或将来官方放开该参数时才有意义

> 旧版 README 与旧版插件把温度固定为 `0.3`，那个值在现在的官方模型上会**直接报错**，不要再沿用。

## 六、常见报错排查

| 现象 | 原因 |
|---|---|
| `模型为空` | 设置里没填模型名 |
| 模型不存在 / model not found | 填了第四节里已下线的模型 |
| `invalid_request_error` 且提到 temperature | 填了非模型默认值的温度 |
| `HTTP 401` | API Key 错误或已失效 |
| `HTTP 429` | 账户余额不足或触发频率限制 |
| 翻译结果为空 | 模型返回了空内容，换模型或重试 |

## 七、自行编译

需要 Android Studio（含 Android SDK）与 JDK 17+。本模块需放在 MT 官方 `mt-plugin-v3-demo` 工程（或结构相同的工程）内，与 `translator-util` 一起参与构建：

```bash
# 打包插件（产物：<模块目录>/build/outputs/mt-plugin/by.plugin.translator.kimi.mtp）
./gradlew :translator-kimi:packageReleaseMtp

# 或在 Android Studio 里直接运行模块，安装到设备后会推送到 MT 管理器
```

## 八、项目信息

| 项 | 值 |
|---|---|
| 插件 ID | `by.plugin.translator.kimi` |
| 代码包名 | `by.mt.plugin` |
| 设置界面 | `by.mt.plugin.MainPreference` |
| 翻译引擎接口 | `by.mt.plugin.KimiTranslationEngine` |
| 接口地址 | `https://api.moonshot.cn/v1/chat/completions`（OpenAI 兼容） |
| API Key 申请 | <https://platform.kimi.com/console/api-keys> |
| 模型列表（官方） | <https://platform.kimi.com/docs/models> |

## 九、致谢

基于 MT 官方插件模板与 DeepSeek 翻译插件修改。
