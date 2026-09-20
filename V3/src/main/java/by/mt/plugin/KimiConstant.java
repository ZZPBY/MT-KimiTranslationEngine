package by.mt.plugin;

public interface KimiConstant {

    /**
     * API Key 的存储键
     */
    String KIMI_API_KEY_PREFERENCE_KEY = "KIMI_API_KEY";

    String KIMI_API_KEY_DEFAULT = "";

    /**
     * 模型名称的存储键，默认留空，由用户自行填写
     */
    String KIMI_MODEL_PREFERENCE_KEY = "KIMI_MODEL";

    String KIMI_MODEL_DEFAULT = "";

    /**
     * 温度的存储键，默认留空表示请求中不发送 temperature 参数
     */
    String KIMI_TEMPERATURE_PREFERENCE_KEY = "KIMI_TEMPERATURE";

    String KIMI_TEMPERATURE_DEFAULT = "";

    /**
     * Kimi (Moonshot AI) OpenAI 兼容接口地址
     */
    String KIMI_API_URL = "https://api.moonshot.cn/v1/chat/completions";

    /**
     * API Key 申请地址
     */
    String KIMI_API_KEY_URL = "https://platform.kimi.com/console/api-keys";

    /**
     * 官方模型列表（含已下线模型说明）
     */
    String KIMI_MODELS_URL = "https://platform.kimi.com/docs/models";

    /**
     * 项目主页
     */
    String GITHUB_URL = "https://github.com/ZZPBY/MT-KimiTranslationEngine";
}
