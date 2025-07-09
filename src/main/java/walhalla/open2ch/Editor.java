/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.open2ch;

import java.io.InputStream;
import java.time.Duration;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import kiss.I;

public interface Editor {

    GoogleAiGeminiChatModel MODEL = GoogleAiGeminiChatModel.builder()
            .apiKey(I.env("GeminiAPIKey"))
            .modelName("gemini-2.5-flash")
            .responseFormat(ResponseFormat.JSON)
            .timeout(Duration.ofMinutes(5))
            .build();

    String selectTopics(String text);

    static String topics(CharSequence input) {
        return AiServices.builder(Editor.class)
                .chatModel(MODEL)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(id -> prompt("topic"))
                .build()
                .selectTopics(input.toString());
    }

    private static String prompt(String name) {
        InputStream input = ClassLoader.getSystemResourceAsStream("walhalla/prompt/" + name + ".md");
        try (input) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}