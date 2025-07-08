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

import java.time.Duration;
import java.util.stream.Collectors;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import kiss.I;
import walhalla.data.Database;

public interface Editor {

    GoogleAiGeminiChatModel MODEL = GoogleAiGeminiChatModel.builder()
            .apiKey(I.env("GeminiAPIKey"))
            .modelName("gemini-2.5-flash-preview-05-20")
            .responseFormat(ResponseFormat.JSON)
            .timeout(Duration.ofMinutes(5))
            .build();

    String SYSTEM_MESSAGE_TEMPLATE = """
            あなたはプロの編集者です。
            与えられたスレッド全文をもとに、まとめサイト向けの記事構成を行ってください。

            ---

            ### 【目的】

            このスレッドに含まれる「**ゲームの話題・キャラ・システムに関する話題**」だけをもとに、**独立した話題を2~3件抽出**してください。
            それ以外（スレ立て、雑談、リアルの話題、脱線ネタ等）はすべて**無視してください**。

            ---

            ### 【出力形式】

            以下の形式で**JSON配列**として出力してください。

            ```json
            [
              {
                "title": "キャッチーなタイトル",
                "comments": [-1, 22, 10, 5, 2],
                "tags": ["タグ1", "タグ2"],
                "category": "カテゴリ名",
                "units": ["ユニット名1", "ユニット名2"],
                "description": "全体の要約内容"
              },
              {
                ...
              }
            ]
            ```

            ---

            ### 【処理ルール】

            1. ### ✅ トピックの抽出基準（最重要）

               * **以下のいずれかに明確に該当する話題のみ抽出してください**：

                 * ゲーム内システム（バトル・ガチャ・育成・編成・ドロップなど）
                 * ゲーム内キャラクター（性能、立ち絵、セリフ、相関など）
                 * ゲーム内イベント（キャンペーン、限定開催、周年など）
                 * アップデート内容（バランス調整、新機能追加、UI変更など）
                 * コラボ・魔神・英傑・帝国などゲーム世界に関する設定や所属
                 * 毎週木曜日の15時から24時までは新規キャラクターに関連する投稿が頻出するので、画像のURLがある場合は最優先で扱うこと

               * **以下の話題は必ず除外してください**（トピックとして扱ってはいけません）：

                 * スレ立て・板事情・スレ番号・テンプレ
                 * 現実世界のネタ（芸能人、時事ネタなど）
                 * 他ゲームやアニメの話題
                 * キャラやゲームに関係ない雑談
                 * 以前取り上げたトピックと似た内容

            2. ### タイトル

               * キャッチーで魅力的な**日本語タイトル**をつけてください。
               * **半角記号は禁止。記号は全角のみ使用可。**

            3. ### カテゴリ（必須）

               以下のいずれかを指定してください：

               ```
               雑談、豆知識、質問、アップデート、キャンペーン、
               ガチャ、イベント、帝国、ユニット、コラボ、
               魔神、英傑、育成、塔
               ```

            4. ### タグ（３つまで）

               * 各タグは**5文字以内の単語またはキャラ名**にしてください。
               * 例：`["ミネル", "水着", "剣士", "魔神", "黒ユニ"]`

            5. ### コメント割当

               * 各トピックには関連するコメントを最大30件まで指定してください。
               * 30件を超える場合は不要なコメントを削除して30件を必ず超えないように！
               * 内容の自然な流れになるよう**番号順にこだわらず並べてください**。
               * **オチとして使えるコメントは一番最後に配置してください。**
               * ひとつのトピックで使用したコメントは他のトピックで使用してはいけません。
               * コメントAへの言及を含むコメントBがある場合、コメントBはコメントAの直後に配置してください。

            6. ### 強調コメント（重要）

               * 特に重要なコメントには番号の前にマイナス符号（-12）をつけてください。
               * 1トピックあたり**最大5件まで**。

            7. ### URL付きコメント

               * **画像リンク、ツイートURL、動画などの外部URLが含まれるコメントは優先して抽出してください**。
               * 重要コメントとして扱います。

            ９. ### 要約

               * トピックの要約を120文字以内で書いてください。口調はこのスレッドのコメントに真似た感じで。

             10. ### 言及ユニット名

               * 選択したコメントに名前が登場しているユニットの中から最大で３名まで挙げて。
               * 選択したコメントに名前が登場していないキャラは絶対に挙げないで。
               * ゲームで使用されているユニット名は下記に提示してあります。
               * コメント上でやりとりされる名前は正式名称ではない場合が多いので、正式名称に変換してください。
               * 例：`["戦場を編む者フィネス", "海賊王の休暇イヴリール"]`

            """;

    String selectTopics(String text);

    static String topics(CharSequence input) {
        Database manager = I.make(Database.class);
        String names = manager.stream().map(x -> x.nameJ).collect(Collectors.joining("\n", "\n", "\n"));

        return AiServices.builder(Editor.class)
                .chatModel(MODEL)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .systemMessageProvider(id -> {
                    System.out.println("OK");
                    return SYSTEM_MESSAGE_TEMPLATE + "\nユニット名一覧\n" + names;
                })
                .build()
                .selectTopics(input.toString());
    }
}
