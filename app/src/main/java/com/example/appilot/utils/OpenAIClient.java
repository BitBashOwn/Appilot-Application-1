package com.example.appilot.utils;

import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenAIClient {
    private static final String TAG = "OpenAIClient";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client;
    private final String apiKey;
    private String prompt;

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface OpenAICallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void generateComment(String originalComment, String commentType, int promptNum, OpenAICallback callback) {
        if (promptNum == 1) {
            // Create the prompt for generating a relevant comment
            prompt = "Generate a " + commentType + ", short, engaging reply to this Twitter comment (maximum 280 characters, preferably 50-100 characters). " +
                    "Make it sound natural and conversational. Original comment: \"" + originalComment + "\"";
        } else if (promptNum == 2) {
            prompt = "Generate a short, engaging quote tweet (maximum 280 characters, preferably 50-80 characters) in response to the following tweet. " +
                    "Make it sound natural, conversational, and relevant to the original tweet. Original tweet: \"" + originalComment + "\"";
        }
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 100);
            requestBody.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);

            requestBody.put("messages", messages);
        } catch (Exception e) {
            Log.e(TAG, "Error creating request body: " + e.getMessage());
            callback.onError("Failed to create request: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "OpenAI API call failed: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject firstChoice = choices.getJSONObject(0);
                            JSONObject messageObj = firstChoice.getJSONObject("message");
                            String generatedComment = messageObj.getString("content").trim();

                            // Remove quotes if they exist
                            if (generatedComment.startsWith("\"") && generatedComment.endsWith("\"")) {
                                generatedComment = generatedComment.substring(1, generatedComment.length() - 1);
                            }

                            Log.d(TAG, "Generated comment: " + generatedComment);
                            callback.onSuccess(generatedComment);
                        } else {
                            callback.onError("No response generated");
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "OpenAI API error: " + response.code() + " - " + errorBody);
                        callback.onError("API error: " + response.code() + " - " + errorBody);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing OpenAI response: " + e.getMessage());
                    callback.onError("Failed to parse response: " + e.getMessage());
                } finally {
                    response.close();
                }
            }
        });
    }

    public String generateCommentSync(String originalComment) {
        String prompt = "Generate a short, engaging reply to this Twitter comment (maximum 280 characters, preferably 50-100 characters). " +
                "Make it sound natural and conversational. Original comment: \"" + originalComment + "\"";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 100);
            requestBody.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);

            requestBody.put("messages", messages);
        } catch (Exception e) {
            Log.e(TAG, "Error creating request body: " + e.getMessage());
            return "Great!"; // Fallback to default comment
        }

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject messageObj = firstChoice.getJSONObject("message");
                    String generatedComment = messageObj.getString("content").trim();

                    // Remove quotes if they exist
                    if (generatedComment.startsWith("\"") && generatedComment.endsWith("\"")) {
                        generatedComment = generatedComment.substring(1, generatedComment.length() - 1);
                    }

                    Log.d(TAG, "Generated comment: " + generatedComment);
                    response.close();
                    return generatedComment;
                }
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                Log.e(TAG, "OpenAI API error: " + response.code() + " - " + errorBody);
            }
            response.close();
        } catch (Exception e) {
            Log.e(TAG, "Error calling OpenAI API: " + e.getMessage());
        }

        return "Great!"; // Fallback to default comment
    }
}
