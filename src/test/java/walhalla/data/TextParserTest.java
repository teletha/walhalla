/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data;

import org.junit.jupiter.api.Test;

public class TextParserTest {

    @Test
    void extractArtistName() {
        assert TextParser.extractArtistName("七原冬雪").equals("七原冬雪");
        assert TextParser.extractArtistName("[https://www.pixiv.net/member.php?id=286217 七原冬雪]").equals("七原冬雪");
        assert TextParser.extractArtistName("[http://frisky.ivory.ne.jp/top.html カスカベアキラ]").equals("カスカベアキラ");
        assert TextParser.extractArtistName("[https://twitter.com/wagyu wagi (みなとそふと)]").equals("wagi");
        assert TextParser.extractArtistName("みなとそふと　wagi").equals("wagi");
        assert TextParser
                .extractArtistName("[https://twitter.com/cyocyo_tasaka 田阪新之助]<br>(Coloring collaboration: [http://exit776.sakura.ne.jp/ 立見いづく])")
                .equals("田阪新之助");
        assert TextParser
                .extractArtistName("[http://www1.plala.or.jp/chesha/　わざきた] <br> (Coloring collaboration: [http://shokushu.jp/ 触手さん])")
                .equals("わざきた");
        assert TextParser.extractArtistName("すめらぎ琥珀（彩色協力：シルキーズプラス）").equals("すめらぎ琥珀");
        assert TextParser.extractArtistName("[https://www.pixiv.net/member.php?id=39123643 Belko]").equals("Belko");
        assert TextParser.extractArtistName("非公開 (Undisclosed)<br/>Possibly [http://www116.sakura.ne.jp/~kuromoji/index.htm もりたん]")
                .equals("");
        assert TextParser.extractArtistName("非公開").equals("");
        assert TextParser.extractArtistName("クリエイティブチームくまさん TOKIAME").equals("クリエイティブチームくまさん TOKIAME");
        assert TextParser.extractArtistName("[http://www.pixiv.net/member.php?id=3043057 yaman**]").equals("yaman＊＊");
        assert TextParser.extractArtistName("yaman**").equals("yaman＊＊");
        assert TextParser.extractArtistName("[https://twitter.com/kibanndagohann/ Kibanda Gohan]").equals("Kibanda Gohan");
        assert TextParser.extractArtistName("ゾウノセ(イベントCG：Creative Cluster Group)").equals("ゾウノセ");
        assert TextParser.extractArtistName("ちり（メカ：おきえん）").equals("ちり");
        assert TextParser.extractArtistName("一斎楽（制作協力・Creative Cluster Group）").equals("一斎楽");
        assert TextParser.extractArtistName("村上ゆいち<br>(Scene Artist：CreativeCluster)").equals("村上ゆいち");
        assert TextParser
                .extractArtistName("[http://puus.sakura.ne.jp/ はんぺん]<br>(Coloring collaboration: [http://exit776.sakura.ne.jp/ 立見いづく])")
                .equals("はんぺん");
        assert TextParser
                .extractArtistName("一斎楽<!-- No official link provided on gcwiki. Japanese players don't even seem to know for sure how to pronounce his name (see http://sennenaigis.blog.fc2.com/blog-entry-1833.html), though the first guess seem to be \\\"Issai-raku\\\" -->")
                .equals("一斎楽");

        assert TextParser.extractArtistName("[''???'']").equals("");
        assert TextParser.extractArtistName("[http://grafismo.co.jp/ Junta (group link)]").equals("Junta");
        assert TextParser.extractArtistName("[http://grafismo.co.jp/ にゃんだりおん (Group link)]").equals("にゃんだりおん");
        assert TextParser.extractArtistName("Undisclosed").equals("");
        assert TextParser.extractArtistName("ヘリを [https:/www.pixiv.net/en/users/341823 pixiv] [https:/twitter.com/herioscope twitter]")
                .equals("ヘリを");
    }
}