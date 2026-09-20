package by.mt.plugin;

import android.text.InputType;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.dialog.LoadingDialog;

public class MainPreference implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.addHeader("{plugin_name}");

        // 1. API Key
        builder.addInput("{kimi_api_key}", KimiConstant.KIMI_API_KEY_PREFERENCE_KEY)
                .defaultValue(KimiConstant.KIMI_API_KEY_DEFAULT)
                .summary("{kimi_api_key_summary}");

        // 2. 模型自定义输入
        builder.addInput("{kimi_model}", KimiConstant.KIMI_MODEL_PREFERENCE_KEY)
                .defaultValue(KimiConstant.KIMI_MODEL_DEFAULT)
                .hint("{kimi_model_hint}")
                .summary("{kimi_model_summary}");

        // 3. 温度设置，留空表示不发送 temperature 参数
        builder.addInput("{kimi_temperature}", KimiConstant.KIMI_TEMPERATURE_PREFERENCE_KEY)
                .defaultValue(KimiConstant.KIMI_TEMPERATURE_DEFAULT)
                .hint("{kimi_temperature_hint}")
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .summary("{kimi_temperature_summary}");

        // 4. 翻译测试
        builder.addText("{kimi_test_title}").summary("{kimi_test_summary}").onClick((pluginUI, preferenceItem) -> {
            String apiKey = context.getPreferences().getString(
                    KimiConstant.KIMI_API_KEY_PREFERENCE_KEY,
                    KimiConstant.KIMI_API_KEY_DEFAULT
            ).trim();
            String model = context.getPreferences().getString(
                    KimiConstant.KIMI_MODEL_PREFERENCE_KEY,
                    KimiConstant.KIMI_MODEL_DEFAULT
            ).trim();
            String temperature = context.getPreferences().getString(
                    KimiConstant.KIMI_TEMPERATURE_PREFERENCE_KEY,
                    KimiConstant.KIMI_TEMPERATURE_DEFAULT
            ).trim();
            if (apiKey.isEmpty()) {
                pluginUI.showToastL("{kimi_api_key_empty}");
                return;
            }
            if (model.isEmpty()) {
                pluginUI.showToastL("{kimi_model_empty}");
                return;
            }
            LoadingDialog loadingDialog = new LoadingDialog(pluginUI).setMessage("{kimi_test_loading}").show();
            new Thread(() -> {
                try {
                    String translated = KimiTranslationEngine.requestTranslation(apiKey, model, temperature, "apple", "English", "Chinese");
                    if (translated.trim().isEmpty()) {
                        pluginUI.showToastL("{kimi_test_empty_result}");
                    } else {
                        pluginUI.showToastL(context.getString("{kimi_test_success}") + "apple → " + translated);
                    }
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = e.toString();
                    }
                    pluginUI.showToastL(context.getString("{kimi_test_failed}") + message);
                } finally {
                    loadingDialog.dismiss();
                }
            }).start();
        });

        // 5. 申请地址
        builder.addText("{kimi_reg_url}")
                .summary("{kimi_reg_url_summary}")
                .url(KimiConstant.KIMI_API_KEY_URL);

        // 6. GitHub 详情
        builder.addText("{kimi_github}")
                .summary("{kimi_github_summary}")
                .url(KimiConstant.GITHUB_URL);
    }
}
