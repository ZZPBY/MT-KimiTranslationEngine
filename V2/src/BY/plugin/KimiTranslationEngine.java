package BY.plugin;

import android.support.annotation.NonNull;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

公共 class KimiTranslationEngine extends BaseTranslationEngine {

    private static final String API_BASE_URL = "https://api.moonshot.cn/v1/chat/completions";
    private static final String DEFAULT_MODEL = "kimi-k2-turbo-preview";
    private static final String DEFAULT_MAX_TOKENS = "2048";
    private static final String DEFAULT_TEMPERATURE = "0.3";
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<String, String>() {{
        put("auto", "自动检测");
        put("zh", "中文");
        put("en", "英语");
        put("ja", "日语");
        put("ko", "韩语");
        put("fr", "法语");
        put("de", "德语");
        put("es", "西班牙语");
        put("ru", "俄语");
        put("it", "意大利语");
        put("pt", "葡萄牙语");
        put("ar", "阿拉伯语");
        put("th", "泰语");
        put("vi", "越南语");
        put("id", "印尼语");
        put("ms", "马来语");
        put("tr", "土耳其语");
        put("pl", "波兰语");
        put("nl", "荷兰语");
        put("sv", "瑞典语");
        put("cs", "捷克语");
    }};

    private String apiKey;
    private String model;
    private String maxTokensStr;
    private String temperatureStr;

    @Override
    protected void init() {
        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences preferences = getContext().getPreferences();
        apiKey = preferences.getString("api_key", "");
        model = preferences.getString("model", DEFAULT_MODEL);
        maxTokensStr = preferences.getString("max_tokens", DEFAULT_MAX_TOKENS);
        temperatureStr = preferences.getString("temperature", DEFAULT_TEMPERATURE);
    }

    private int getMaxTokens() {
        try {
            return Integer.parseInt(maxTokensStr);
        } catch (NumberFormatException e) {
            return 2048;
        }
    }

    private double getTemperature() {
        try {
            return Double.parseDouble(temperatureStr);
        } catch (NumberFormatException e) {
            return 0.3;
        }
    }

    @NonNull
    @Override
    public String name() {
        return "Kimi翻译";
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return Arrays.asList("auto", "zh", "en", "ja", "ko", "fr", "de", "es", "ru", "it", 
            "pt", "ar", "th", "vi", "id", "ms", "tr", "pl", "nl", "sv", "cs");
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return Arrays.asList("zh", "en", "ja", "ko", "fr", "de", "es", "ru", "it", 
            "pt", "ar", "th", "vi", "id", "ms", "tr", "pl", "nl", "sv", "cs");
    }

    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        String displayName = LANGUAGE_MAP.get(language);
        if (displayName != null) {
            return displayName;
        }
        return super.getLanguageDisplayName(language);
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        loadSettings();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "错误：请在插件设置中配置API Key";
        }

        String prompt = buildTranslationPrompt(text, sourceLanguage, targetLanguage);

        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                return callKimiApi(prompt);
            } catch (SocketTimeoutException e) {
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "错误：请求被中断";
                    }
                }
            } catch (IOException e) {
                getContext().log(e);
                throw e;
            } catch (Exception e) {
                getContext().log(e);
                throw new IOException(e.getMessage(), e);
            }
        }

        return "错误：连接超时，请检查网络连接后重试";
    }

    private String buildTranslationPrompt(String text, String sourceLanguage, String targetLanguage) {
        String sourceLangName = getLanguageDisplayName(sourceLanguage);
        String targetLangName = getLanguageDisplayName(targetLanguage);

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的翻译助手。请将以下");
        if (!"auto".equals(sourceLanguage)) {
            prompt.append(sourceLangName);
        } else {
            prompt.append("文本");
        }
        prompt.append("翻译成").append(targetLangName).append("。\n\n");
        prompt.append("翻译要求：\n");
        prompt.append("1. 只返回翻译结果，不要添加任何解释或说明\n");
        prompt.append("2. 保持原文的格式和特殊字符（如%s、%d等占位符）不变\n");
        prompt.append("3. 确保翻译准确、自然、流畅\n");
        prompt.append("4. 如果是代码或技术术语，请保持原样不翻译\n\n");
        prompt.append("待翻译文本：\n");
        prompt.append(text);

        return prompt.toString();
    }

    private String callKimiApi(String prompt) throws Exception {
        URL url = new URL(API_BASE_URL);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("temperature", getTemperature());
            requestBody.put("max_completion_tokens", getMaxTokens());

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的翻译助手，只返回翻译结果，不添加任何额外说明。");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            requestBody.put("messages", messages);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return parseTranslationResponse(response.toString());
            } else if (responseCode == 401) {
                throw new IOException("API Key无效或已过期");
            } else if (responseCode == 429) {
                throw new IOException("请求过于频繁，请稍后再试");
            } else if (responseCode >= 500) {
                throw new IOException("Kimi服务器错误，请稍后再试");
            } else {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                throw new IOException("HTTP错误 " + responseCode + ": " + errorResponse.toString());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String parseTranslationResponse(String jsonResponse) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonResponse);

        if (jsonObject.has("choices")) {
            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                if (firstChoice.has("message")) {
                    JSONObject message = firstChoice.getJSONObject("message");
                    if (message.has("content")) {
                        return message.getString("content").trim();
                    }
                }
            }
        }

        throw new IOException("无法解析API响应");
    }

    @Override
    public void onStart() {
        loadSettings();
    }

    @Override
    public boolean onError(Exception e) {
        getContext().log(e);
        return false;
    }
}
