package BY.plugin;

import android.content.SharedPreferences;
import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class KimiPluginPreference implements PluginPreference {

    private static final String[] MODEL_OPTIONS = {
        "kimi-k2-turbo-preview",
        "kimi-k2.5",
        "kimi-k2-0905-preview",
        "kimi-k2-0711-preview",
        "kimi-k2-thinking-turbo",
        "kimi-k2-thinking",
        "moonshot-v1-8k",
        "moonshot-v1-32k",
        "moonshot-v1-128k"
    };

    private static final String[] MODEL_DISPLAY_NAMES = {
        "Kimi K2 Turbo (推荐)",
        "Kimi K2.5",
        "Kimi K2 0905",
        "Kimi K2 0711",
        "Kimi K2 Thinking Turbo",
        "Kimi K2 Thinking",
        "Moonshot V1 8K",
        "Moonshot V1 32K",
        "Moonshot V1 128K"
    };

    private static final String[] TEMPERATURE_OPTIONS = {
        "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"
    };

    private static final String[] MAX_TOKENS_OPTIONS = {
        "512", "1024", "2048", "4096", "8192"
    };

    @Override
    public void onBuild(MTPluginContext context, Builder builder) {
        SharedPreferences preferences = context.getPreferences();

        builder.addHeader("API设置");

        builder.addInput("API Key", "api_key")
            .summary("请输入从Kimi开放平台获取的API Key")
            .defaultValue("")
            .validator(new Input.Validator() {
                @Override
                public String validate(String value) {
                    if (value == null || value.trim().isEmpty()) {
                        return "API Key不能为空";
                    }
                    if (!value.startsWith("sk-")) {
                        return "API Key格式不正确，应以sk-开头";
                    }
                    return null;
                }
            });

        builder.addText("获取API Key")
            .summary("点击前往Kimi开放平台获取API Key")
            .url("https://platform.moonshot.cn/console");

        builder.addHeader("模型设置");

        List modelList = builder.addList("选择模型", "model")
            .summary("选择要使用的Kimi模型");

        for (int i = 0; i < MODEL_OPTIONS.length; i++) {
            modelList.addItem(MODEL_DISPLAY_NAMES[i], MODEL_OPTIONS[i]);
        }

        builder.addHeader("高级设置");

        List temperatureList = builder.addList("Temperature参数", "temperature")
            .summary("控制翻译的创造性，值越低越保守准确");

        for (String temp : TEMPERATURE_OPTIONS) {
            String label = temp;
            if (temp.equals("0.1")) label = temp + " (最保守)";
            else if (temp.equals("0.5")) label = temp + " (平衡)";
            else if (temp.equals("1.0")) label = temp + " (最创造性)";
            temperatureList.addItem(label, temp);
        }

        List maxTokensList = builder.addList("最大Token数", "max_tokens")
            .summary("限制单次翻译的最大输出长度");

        for (String tokens : MAX_TOKENS_OPTIONS) {
            String label = tokens;
            if (tokens.equals("512")) label = tokens + " (短文本)";
            else if (tokens.equals("2048")) label = tokens + " (推荐)";
            else if (tokens.equals("8192")) label = tokens + " (长文本)";
            maxTokensList.addItem(label, tokens);
        }

        builder.addHeader("关于");

        builder.addText("Github详情")
            .summary("点击跳转")
            .url("https://github.com/ZZPBY/MT-KimiTranslationEngine");

        builder.addText("官方文档")
            .summary("点击查看Kimi API文档")
            .url("https://platform.moonshot.cn/docs");

        builder.addText("使用说明")
            .summary("1. 先在Kimi开放平台注册账号并获取API Key\n" +
                    "2. 在上方API Key处填入你的密钥\n" +
                    "3. 根据需要调整模型和参数设置\n" +
                    "4. 返回MT管理器翻译界面即可使用");
    }
}
