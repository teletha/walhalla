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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class UnitTest {

    @Test
    void parseCharcterDataPlatinum() {
        String text = """
                {{Unit infobox
                |name    = Demon Lord of the Abyss Abgrund <!--1553-->
                |gender  = FEMALE
                |rank    = Platinum
                |class   = Demon Lord
                |jpname  = 深潭の魔将アブグルント<br>''Shintan no Ma Shō Abugurunto''
                |artist  = ねめ猫⑥
                }}

                '''Demon Lord of the Abyss Abgrund''' is a [[:Category:Rarity:Platinum|platinum]] [[:Category:Demon Lords|demon lord]].

                Obtained from:
                * [[Shrine#Imperial_Summoning|Imperial Summoning]]

                See also:
                * [[Chibi Abgrund]]

                __TOC__
                <br clear="all"/>

                == Stats ==
                {{Unitlist start|ability=yes}}
                {{:Abgrund/stats}}
                {{Unitlist end}}

                == Skill ==
                {{:Skill/White Demon's Incitement|Platinum}}

                == Skill Awakening ==
                {{Skill awakening list begin}}
                {{Skill awakening item
                |name     = Abgrund
                |skill    = White Demon's Incitement
                |effect   = For 35 seconds, attacks become ranged (250), and allied Imperial or Demon units gain 1.2x increased attack and 1.4x increased defense.
                |reuse    = 30
                |awSkill  = Love-Child's Song
                |awEffect = For 35 seconds, attack increases by 1.7x, and attacks become ranged (280), ignore defense, and attacks twice consecutively. All allies gain 15% increased attack.
                |awReuse  = 35
                }}
                {{Skill awakening list end}}

                == Ability ==
                {{Abilitylist start}}
                {{:Ability/Imperfect Manifestation}}
                {{:Ability/She Who Came From the Abyss}}
                {{Abilitylist end}}

                == Class Attributes ==
                {{:Class/Demon Lords}}

                == Affection ==
                === Quotes - highlight the pink lines to see them. ===
                {{Quote table
                |%1      =   0% |quote1  = <!-- こんな幼気な少女をつかまえて、 怪物だなんて失礼とは思わない？ -->
                |%2      =  15% |quote2  = <!-- 噂はよく聞いていたけれど、 貴方ってとても素敵な人だわ。 -->
                |%3      =  30% |quote3  = <!-- 深淵を覗くとき、深淵もまた。 私はちゃんと忠告したからね？ -->
                |%4      =  50% |quote4  = <!-- お好きに触れてくれて構わないわ。 引きずり込まれても知らないけど。 -->
                |%5      =  60% |quote5  = <!-- そんなに怯えないでくださいな。 別に取って食べたりしないから。 -->
                |%6      =  80% |quote6  = <!-- 貴方の全部が知りたいの。 全部って言ったら全部よ。 -->
                |%7      = 100% |quote7  = <!-- 簡単に堕ちたりしないでね？ それじゃあつまらないでしょ？ -->
                |quote9  =       <!-- ……普通にガードが固いわ。 もっと近づきたいのだけど。 -->
                |quote10 =       <!-- 貴方が堕ちるその瞬間を、 一番近くで見ていてあげる。 -->
                |quote8  =       <!-- 書類仕事が溜まっているみたいね、 メフィストにでもやらせなさいな。 -->
                }}

                === Scenes ===
                {{Scenes
                |%1      =  30% |Scene 1 =
                |%2      = 100% |Scene 2 =
                }}

                == Awakening Materials ==
                {{:Awakening/Demon Lords|Platinum}}

                == Trivia ==
                '''love child''' (pl. love children)
                <br>(euphemistic) A child born as a result of a romantic liaison between parents not married to one another; an illegitimate child.
                <br>[https://www.wordsense.eu/love_child/ Source]

                ''Abgrund'' is German for "Abyss".

                == Gallery ==
                {{gallery|auto=
                Abgrund_Icon.png
                Abgrund_AW_Icon.png
                Abgrund_Render.png
                Abgrund_AW_Render.png
                Abgrund_Sprite.png
                Abgrund_Sprite.gif
                Abgrund_Attack_Sprite.gif
                Abgrund_Death_Sprite.png
                Abgrund_AW_Sprite.png
                Abgrund_AW_Sprite.gif
                Abgrund_AW_Attack_Sprite.gif
                Abgrund_AW_Death_Sprite.png
                |AA=
                Abgrund AA AW Render.png
                }}
                """;

        Unit unit = new Unit();
        unit.parseWikiCharacterData("Abgrund", text);

        assert unit.id == 0;
        assert unit.name.equals("Abgrund");
        assert unit.gender == Gender.女性;
        assert unit.rarity == Rarity.白;
        assert unit.nameJ.equals("深潭の魔将アブグルント");
        assert unit.artist.equals("ねめ猫⑥");
        assert unit.reuse == 30;
        assert unit.reuseAW == 35;
        assert unit.affection.size() == 10;
        assert unit.affection.get(0).equals("こんな幼気な少女をつかまえて、 怪物だなんて失礼とは思わない？");
        assert unit.affection.get(1).equals("噂はよく聞いていたけれど、 貴方ってとても素敵な人だわ。");
        assert unit.affection.get(2).equals("深淵を覗くとき、深淵もまた。 私はちゃんと忠告したからね？");
        assert unit.affection.get(3).equals("お好きに触れてくれて構わないわ。 引きずり込まれても知らないけど。");
        assert unit.affection.get(4).equals("そんなに怯えないでくださいな。 別に取って食べたりしないから。");
        assert unit.affection.get(5).equals("貴方の全部が知りたいの。 全部って言ったら全部よ。");
        assert unit.affection.get(6).equals("簡単に堕ちたりしないでね？ それじゃあつまらないでしょ？");
        assert unit.affection.get(7).equals("書類仕事が溜まっているみたいね、 メフィストにでもやらせなさいな。");
        assert unit.affection.get(8).equals("……普通にガードが固いわ。 もっと近づきたいのだけど。");
        assert unit.affection.get(9).equals("貴方が堕ちるその瞬間を、 一番近くで見ていてあげる。");
    }

    @Test
    void parseCharcterDataBlack() {
        String text = "{{Unit infobox\\n|name = Fire and Iron's Oath Vulcano <!--2475-->\\n|gender = FEMALE\\n|rank = Black\\n|class = Blacksmith\\n|jpname = 火と鉄の誓約ウルカノ<br>''Hi to Tetsu no Seiyaku Urukano''\\n|artist = wagi（彩色協力：n032）\\n}}\\n\\n'''Fire and Iron's Oath Vulcano''' is a [[:Category:Rarity:Black|black]] [[:Category:Blacksmiths|blacksmith]].\\n\\nObtained from:\\n* <!-- Obtain method goes here -->\\n\\nSee also:\\n* [[Vulcano]]\\n__TOC__\\n<br clear=\"all\"/>\\n== Stats ==\\n{{Unitlist start|ability=yes}}\\n{{:Vulcano (Bride)/stats}}\\n{{Unitlist end}}\\n\\n== Skill ==\\n{{:Skill/A Bouquet For The Firepit|Black}}\\n\\n== Skill Awakening ==\\n{{Skill awakening list begin}}\\n{{Skill awakening item\\n|name = Vulcano (Bride)\\n|skill = A Bouquet For The Firepit\\n|effect = For 30 seconds, attack increases by 2.3x. Attacks up to 2 enemies within range (240) simultaneously, dealing true damage. Self and strengthening targets gain 1.2x increased attack and defense.\\n|reuse = 45\\n\\n|awSkill = Burnt-Out Love's Furnace\\n|awEffect = Strengthening targets gain increased attack and defense. Upon activation, UP is consumed, and the buff is increased accordingly (+0.266%/1UP, max is +40% at 150UP). Infinite duration. Cannot be automatically activated.\\n|awReuse = 70\\n}}{{Skill awakening list end}}\\n\\n== Ability ==\\n{{Abilitylist start}}\\n{{:Ability/Blacksmithing God's Love}}\\n{{:Ability/Unceasing Heat, Unbreakable Love}}\\n{{Abilitylist end}}\\n\\n== Class Attributes ==\\n{{:Class/Blacksmiths}}\\n\\n== Affection ==\\n=== Quotes - highlight the pink lines to see them. ===\\n{{Quote table\\n|%1 =   0% |quote1 = <!-- 亜神を花嫁にしようたぁ、 お前も大きく出たもんだ。 -->\\n|%2 =  15% |quote2 = <!-- あたしは火と鉄の亜神だぞ、 教会よりも鉄火場が似合う。 -->\\n|%3 =  30% |quote3 = <!-- こんなドレスを用意しやがって、 職人として粗末にはできねぇな。 -->\\n|%4 =  50% |quote4 = <!-- よく似合ってるってか？ 言ってろ、バーカ。 -->\\n|%5 =  60% |quote5 = <!-- 真っ赤な薔薇、 あたしの髪と同じだな。 -->\\n|%6 =  80% |quote6 = <!-- あたしに手を出せば火傷するぜ。 その覚悟があるってんなら── -->\\n|%7 = 100% |quote7 = <!-- 神ってのは嫉妬深いもんだ、 浮気したら本気で泣くからな？ -->\\n|quote9 = <!-- 槌で叩いて人格から鍛え直す。 何のことって、浮気の対処法。 -->\\n|quote10 = <!-- ほら、あたし謹製の指輪だ。 左手薬指にでも嵌めとけよ。 -->\\n|quote8 = <!-- よし、工房に行こうぜ旦那様。 結婚祝いに何か作ってやるぜ？ -->\\n}}\\n\\n=== Scenes ===\\n{{Scenes\\n|%1 =  30% |Scene 1 =\\n|%2 = 100% |Scene 2 =\\n}}\\n\\n== Awakening Materials ==\\n{{:Awakening/Blacksmiths|Black}}\\n\\n== Gallery ==\\n{{gallery|auto=\\nVulcano (Bride)_Icon.png\\nVulcano (Bride)_AW_Icon.png\\nVulcano (Bride)_Render.png\\nVulcano (Bride)_AW_Render.png\\nVulcano (Bride)_Sprite.png\\nVulcano (Bride)_Sprite.gif\\nVulcano (Bride)_Attack_Sprite.gif\\nVulcano (Bride)_Death_Sprite.png\\nVulcano (Bride)_AW_Sprite.png\\nVulcano (Bride)_AW_Sprite.gif\\nVulcano (Bride)_AW_Attack_Sprite.gif\\nVulcano (Bride)_AW_Death_Sprite.png\\n}}";

        Unit unit = new Unit();
        unit.parseWikiCharacterData("Vulcano", text);

        assert unit.id == 0;
        assert unit.name.equals("Vulcano");
        assert unit.gender == Gender.女性;
        assert unit.rarity == Rarity.黒;
        assert unit.nameJ.equals("火と鉄の誓約ウルカノ");
        assert unit.artist.equals("wagi");
        assert unit.reuse == 45;
        assert unit.reuseAW == 70;
        assert unit.affection.size() == 10;
        assert unit.affection.get(0).equals("亜神を花嫁にしようたぁ、 お前も大きく出たもんだ。");
        assert unit.affection.get(1).equals("あたしは火と鉄の亜神だぞ、 教会よりも鉄火場が似合う。");
        assert unit.affection.get(2).equals("こんなドレスを用意しやがって、 職人として粗末にはできねぇな。");
        assert unit.affection.get(3).equals("よく似合ってるってか？ 言ってろ、バーカ。");
        assert unit.affection.get(4).equals("真っ赤な薔薇、 あたしの髪と同じだな。");
        assert unit.affection.get(5).equals("あたしに手を出せば火傷するぜ。 その覚悟があるってんなら──");
        assert unit.affection.get(6).equals("神ってのは嫉妬深いもんだ、 浮気したら本気で泣くからな？");
        assert unit.affection.get(7).equals("よし、工房に行こうぜ旦那様。 結婚祝いに何か作ってやるぜ？");
        assert unit.affection.get(8).equals("槌で叩いて人格から鍛え直す。 何のことって、浮気の対処法。");
        assert unit.affection.get(9).equals("ほら、あたし謹製の指輪だ。 左手薬指にでも嵌めとけよ。");
    }

    @Test
    void parseCharcterDataSapphire() {
        String text = "{{Limited\\n|code = yes\\n|current = \\n|date = \\n}}\\n\\n\\n{{Unit infobox\\n|name = Imperial Sealing Swordswoman Forte\\n|gender = Female\\n|rank = Sapphire\\n|class = Monster Breaker\\n|jpname = 帝国封印剣士フォルテ<br>''Teikoku Fūin Ken-shi Forute''\\n|artist = [https://www.pixiv.net/member.php?id=286217 七原冬雪]\\n|body = Height: 155cm / B:87 W:58 H:83\\n|profile3EngText = \\nAn Imperial swordswoman who was dispatched to the Kingdom under the [[Leora|Imperial Marshal Leora]]'s instructions as their compensation for cooperating in the fight against a Majin. Although she lacks emotions, she tries to faithfully obey her orders to understand the Prince better. Her own existence is a top military secret, and she keeps any details regarding the power within the sealed sword she wields to herself. She thinks of [[Solare]], the Imperial God Tree's Envoy, as her most cherished friend.\\n|profile3JpnText = 魔神との戦いで協力してもらった見返りにと帝国元帥レオラの指示で王国に派兵された帝国の女剣士。感情が乏しく、王子のことを理解せよという命令を忠実に守ろうとする。存在自体が軍事機密であり、自分のことも手にする封印剣の力についても詳しくは語らない。友人となった帝国神樹使いソラーレを大切に思っている。\\n}}\\n\\n''\"As thanks for your cooperation the other day, Forte was dispatched.\"''\\n\\n'''Imperial Sealing Swordswoman Forte''' is a [[:Category:Rarity:Sapphire|sapphire]]\\n[[:Category:Monster Breakers|monster breaker]].\\n\\nObtained from:\\n* [[Serial Code]] - Millennium War Aigis: White Empire Volume 3 (2017/09/30～)\\n__TOC__\\n<br clear=\"all\"/>\\n== Stats ==\\n{{Unitlist start|ability=yes}}\\n{{:Forte/stats}}\\n{{Unitlist end}}\\n\\n== Skill ==\\n{{:Skill/Sealing Sword Blutgang|sapphire}}\\n\\n== Skill Awakening ==\\n{{Skill awakening list begin}}\\n{{Skill awakening item\\n|name = Forte\\n|skill = Sealing Sword Blutgang\\n|effect = For 30 seconds, defense is increased by 2.5x. If the enemy is a '''[[:Category:Youkai Enemies|Youkai]]''' or '''[[:Category:Demon Enemies|Demon]]''', attack increases by 2.0x. Upon expiry, this unit is paralyzed.\\n|reuse = 30\\n\\n|awSkill = Treasured Sword Blutgang\\n|awEffect = For 30 seconds, defense and magic resistance increase by 3.0x. If the enemy is a '''[[:Category:Youkai Enemies|Youkai]]''' or '''[[:Category:Demon Enemies|Demon]]''', attack increases by 3.0x. Upon expiry, this unit is paralyzed.\\n|awReuse = 40\\n}}\\n{{Skill awakening list end}}\\n\\n== Ability ==\\n{{Abilitylist start}}\\n{{:Ability/Evil Breaker}}\\n{{:Ability/Evil Breaker II}}\\n{{Abilitylist end}}\\n\\n== Class Attributes ==\\n{{:Class/Monster Breakers}}\\n\\n== Affection ==\\n=== Quotes - highlight the pink lines to see them. ===\\n{{Quote table\\n|%1 = 0%   |quote1 = As thanks for your cooperation the other day, Forte was dispatched.\\n|%2 = 15%  |quote2 = Marshal Leora has ordered Forte to understand the Prince better.\\n|%3 = 30%  |quote3 = Is the Prince fighting against Demons or Youkai? If that is so, Forte can be of help. \\n|%4 = 50%  |quote4 = Is Forte slowly understanding the Prince better?\\n|%5 = 60%  |quote5 = Forte has friends...Cherished friends...\\n|%6 = 80%  |quote6 = Forte does not want the Prince to forget about her...\\n|%7 = 100% |quote7 = Trying to understand the Prince, gives Forte's chest a sort of...tingly feeling.\\n|Adjutant  |quote8 = Prince, if you don't tell Forte your orders, Forte won't know what to do.\\n}}\\n=== Scenes ===\\n{{Scenes\\n|%1 = 30%  |Scene 1 = Missionary, Partial Nude (virgin) (ahagao)\\n|%2 = 100% |Scene 2 = Nude, Doggy Style, Pillow Grab (creampie)\\n}}\\n\\n== Awakening Materials ==\\n{{:Awakening/Monster Breakers|sapphire}}\\n\\n== Trivia ==\\n'''Blutgang''' was the weapon of the legendary Germanic hero Háma. Háma (Old English: Hāma), Heimir (Old Norse), or Heime (German) appears in the Anglo-Saxon poems Beowulf and Widsith, in the Scandinavian Þiðrekssaga and in German epics such as Alpharts Tod.\\n<br />[https://en.wikipedia.org/wiki/H%C3%A1ma Wikipedia]\\n\\n== Gallery ==\\n{{gallery\\n|Normal=\\nForte Icon.png\\nForte Render.png\\nForte Sprite.png\\nForte Death Sprite.png\\nForte Sprite.gif\\nForte Attack Sprite.gif\\n|AW=\\nForte AW Icon.png\\nForte AW Render.png\\nForte AW Sprite.png\\nForte AW Death Sprite.png\\nForte AW Sprite.gif\\nForte AW Attack Sprite.gif\\n|AA=\\nForte AA Render.png\\nForte AA AW Render.png\\n|Art=\\nForte_fanart.jpg;Art by the original artist (七原冬雪)\\n}}";

        Unit unit = new Unit();
        unit.parseWikiCharacterData("Forte", text);

        assert unit.id == 0;
        assert unit.name.equals("Forte");
        assert unit.gender == Gender.女性;
        assert unit.rarity == Rarity.青;
        assert unit.nameJ.equals("帝国封印剣士フォルテ");
        assert unit.artist.equals("七原冬雪");
        assert unit.body.equals("Height: 155cm / B:87 W:58 H:83");
        assert unit.reuse == 30;
        assert unit.reuseAW == 40;
        assert unit.affection.size() == 0;
    }

    @Test
    void parseCharcterDataBlackByName() {
        Unit unit = new Unit();
        unit.parseWikiCharacterDataByName("Finesse_(Black)");

        assert unit.id == 0;
        assert unit.name.equals("Finesse_(Black)");
        assert unit.gender == Gender.女性;
        assert unit.rarity == Rarity.黒;
        assert unit.nameJ.equals("戦場を編む者フィネス");
        assert unit.artist.equals("さがら梨々");
        assert unit.reuse == 5;
        assert unit.reuseAW == 5;
        assert unit.affection.size() == 10;
        assert unit.affection.get(0).equals("あたしの力が必要？ いいわ！　報酬次第でね♪");
        assert unit.affection.get(1).equals("あたしに任せておいて。 戦場を引っ掻き回してあげるわ！");
        assert unit.affection.get(2).equals("弟子、弟子かぁ。 ふふふ、初めての弟子だ！");
        assert unit.affection.get(3).equals("じゃーん、手作り教科書！ 持ってることは内緒でね♪");
        assert unit.affection.get(4).equals("あたしの教科書とか、 争奪戦からの戦争が起きそう……。");
        assert unit.affection.get(5).equals("王子とずっと一緒にいられたら、 毎日楽しそうよね……。");
        assert unit.affection.get(6).equals("一緒に世界、救っちゃおうか。 ふたりで歴史に名を残すのよ！");
        assert unit.affection.get(7).equals("はい、サインの準備だけして？ 書類仕事はあたしの役目でしょ。");
        assert unit.affection.get(8).equals("戦場には軍師、人生には伴侶。 ここに最適な人材がいるぞぉ？");
        assert unit.affection.get(9).equals("えっ、恋愛の戦術も知りたい？ あたしで実践する気だなぁ？");
    }

    @Test
    void parseStatsBlackHero() {
        String text = "{{Unitlist start|ability=yes}}\\n<onlyinclude>{{{{{format|Unitlist item}}}\\n|1 = {{{1|}}}\\n\\n|hero = y          |awaken = y\\n|name = Finesse (Black)|gender = female   |rarity = Black\\n|race = Human      |affiliation = Hero\\n\\n|c1Class   = Master of the Board\\n|c1MinHp   = -     |c1MinAtk  = -     |c1MinDef  = -     |c1Resist  = 0\\n|c1MaxHp   = -     |c1MaxAtk  = -     |c1MaxDef  = -     |c1MaxLvl  = 99\\n|c1Block   = 1     |c1Range   = 250   |c1MaxCost = 22    |c1MinCost = 19\\n|c1Skill   = Fianna-Style Saturation Attack\\n|c1Ability = Battlefield Clairvoyance\\n\\n|c3Class   = Master of the Board\\n|c3MinHp   = 3110  |c3MinAtk  = 649   |c3MinDef  = 312   |c3Resist  = 0\\n|c3MaxHp   = 3645  |c3MaxAtk  = 770   |c3MaxDef  = 364   |c3MaxLvl  = 99\\n|c3Block   = 1     |c3Range   = 250   |c3MaxCost = 22    |c3MinCost = 19\\n|100AffBonus = HP+450<br>ATK+180\\n|150AffBonus = ATK+90\\n|c3SRange = 325\\n|c3Skill = Overrun!\\n|c3SAWRange= 375\\n|c3Ability = Battlefield Clairvoyance\\n}}</onlyinclude>\\n{{Unitlist end}}";

        Unit unit = new Unit();
        unit.parseWikiStats(text);

        assert unit.hero == true;
        assert unit.race.contains(Attribute.Human);
        assert unit.stats1.hp == 3645;
        assert unit.stats1.atk == 770;
        assert unit.stats1.def == 364;
        assert unit.stats1.mr == 0;
        assert unit.stats1.range == 250;
        assert unit.stats1.rangeSkill == 325;
        assert unit.stats1.rangeSkillAW == 375;
        assert unit.stats1.block == 1;
        assert unit.stats1.cost == 22;
        assert unit.stats1.costMin == 19;

        assert unit.bounus100.size() == 2;
        assert unit.bounus100.get(0).equals("HP+450");
        assert unit.bounus100.get(1).equals("攻撃力+180");
        assert unit.bounus150.size() == 1;
        assert unit.bounus150.get(0).equals("攻撃力+90");

        assert unit.stats2A == null;
        assert unit.stats2B == null;
    }

    @Test
    void parseStatsBlack() {
        String text = "{{Unitlist start|ability=yes}}\\n<onlyinclude>{{{{{format|Unitlist item}}}\\n|1 = {{{1|}}}\\n\\n|awaken = y        |awaken2A = y      |awaken2B = y\\n|name = Lasithi (Bride)|gender = female   |rarity = Black\\n|race = Birdfolk   |affiliation = Flying|seasonal = June Bride |cat4 = Bowman\\n\\n|c1Class   = Sky Shooter\\n|c1MinHp   = 1317  |c1MinAtk  = 305   |c1MinDef  = 126   |c1Resist  = 0\\n|c1MaxHp   = 1834  |c1MaxAtk  = 414   |c1MaxDef  = 197   |c1MaxLvl  = 80\\n|c1Block   = 0     |c1Range   = 260   |c1MaxCost = 25    |c1MinCost = 22\\n|100AffBonus = HP+672<br>ATK+134\\n|150AffBonus = ATK+90\\n|c1Skill = Veil Strike\\n|c1SRange  = 338\\n|c1Ability = Gale Ceremony\\n|c1ACostMod= +7\\n\\n|c3Class   = Arrow Wing\\n|c3MinHp   = 1839  |c3MinAtk  = 415   |c3MinDef  = 198   |c3Resist  = 0\\n|c3MaxHp   = 2358  |c3MaxAtk  = 524   |c3MaxDef  = 262   |c3MaxLvl  = 99\\n|c3Block   = 0     |c3Range   = 260   |c3MaxCost = 27    |c3MinCost = 24\\n|c3Skill = Love is Something to be Secretly Unleashed\\n|c3SRange  = 338\\n|c3SAWRange= 338\\n|c3Ability = Assault Virgin Road\\n|c3ACostMod= +7\\n\\n|c4Class   = Sky Supremacy\\n|c4MinHp   = 2628  |c4MinAtk  = 581   |c4MinDef  = 314   |c4Resist  = 0\\n|c4MaxHp   = 3341  |c4MaxAtk  = 718   |c4MaxDef  = 341\\n|c4Block   = 0     |c4Range   = 260   |c4MaxCost = 27    |c4MinCost = 24\\n|c4SRange  = 338\\n|c4SAWRange= 338\\n\\n|c5Class   = Zenith Shooter\\n|c5MinHp   = 2494  |c5MinAtk  = 540   |c5MinDef  = 380   |c5Resist  = 0\\n|c5MaxHp   = 3013  |c5MaxAtk  = 621   |c5MaxDef  = 380\\n|c5Block   = 0     |c5Range   = 290   |c5MaxCost = 27    |c5MinCost = 24\\n|c5SRange  = 377\\n|c5SAWRange= 377\\n}}</onlyinclude>\\n{{Unitlist end}}";

        Unit unit = new Unit();
        unit.parseWikiStats(text);

        assert unit.hero == false;
        assert unit.military.contains(Attribute.Flying);
        assert unit.race.contains(Attribute.Birdfolk);

        assert unit.stats2A.hp == 3341;
        assert unit.stats2A.atk == 718;
        assert unit.stats2A.def == 341;
        assert unit.stats2A.mr == 0;
        assert unit.stats2A.range == 260;
        assert unit.stats2A.rangeSkill == 338;
        assert unit.stats2A.rangeSkillAW == 338;
        assert unit.stats2A.block == 0;
        assert unit.stats2A.cost == 27;
        assert unit.stats2A.costMin == 24;

        assert unit.stats2B.hp == 3013;
        assert unit.stats2B.atk == 621;
        assert unit.stats2B.def == 380;
        assert unit.stats2B.mr == 0;
        assert unit.stats2B.range == 290;
        assert unit.stats2B.rangeSkill == 377;
        assert unit.stats2B.rangeSkillAW == 377;
        assert unit.stats2B.block == 0;
        assert unit.stats2B.cost == 27;
        assert unit.stats2B.costMin == 24;

        assert unit.bounus100.size() == 2;
        assert unit.bounus100.get(0).equals("HP+672");
        assert unit.bounus100.get(1).equals("攻撃力+134");
        assert unit.bounus150.size() == 1;
        assert unit.bounus150.get(0).equals("攻撃力+90");
    }

    @Test
    void parseStatsBlack2() {
        Unit unit = new Unit();
        unit.parseWikiStatsByName("Tytto_(Swimsuit)");

        assert unit.hero == false;
        assert unit.season.contains(Attribute.Summer);
        assert unit.military.contains(Attribute.Magician);

        assert unit.stats2A.hp == 1526;
        assert unit.stats2A.atk == 204;
        assert unit.stats2A.def == 155;
        assert unit.stats2A.mr == 30;
        assert unit.stats2A.range == 260;
        assert unit.stats2A.rangeSkill == 650;
        assert unit.stats2A.rangeSkillAW == 650;
        assert unit.stats2A.block == 0;
        assert unit.stats2A.cost == 7;
        assert unit.stats2A.costMin == 4;

        assert unit.stats2B.hp == 1744;
        assert unit.stats2B.atk == 225;
        assert unit.stats2B.def == 204;
        assert unit.stats2B.mr == 55;
        assert unit.stats2B.range == 280;
        assert unit.stats2B.rangeSkill == 700;
        assert unit.stats2B.rangeSkillAW == 700;
        assert unit.stats2B.block == 0;
        assert unit.stats2B.cost == 7;
        assert unit.stats2B.costMin == 4;
    }

    @Test
    void parseStatsBlack3() {
        String text = """
                {{Unitlist start|ability=yes}}
                <onlyinclude>{{{{{format|Unitlist item}}}
                |1 = {{{1|}}}

                |awaken = y        |awaken2A = y      |awaken2B = y
                |name = Achillea   |gender = female   |rarity = Black
                |race = Half-God

                |c1Class   = Myth Inheritor
                |c1MinHp   = 2711  |c1MinAtk  = 716   |c1MinDef  = 651   |c1Resist  = 0
                |c1MaxHp   = 3510  |c1MaxAtk  = 832   |c1MaxDef  = 715   |c1MaxLvl  = 80
                |c1Block   = 2     |c1Range   =       |c1MaxCost = 34    |c1MinCost = 29
                |100AffBonus = HP+630<br>ATK+126
                |150AffBonus = {{Tt|Skill Cooldown|SCD}}-23%
                |c1Skill = Champion of Wars
                |c1SRange  = 180
                |c1Ability = Half-God of Fortitude

                |c3Class   = Child of Legend
                |c3MinHp   = 3515  |c3MinAtk  = 833   |c3MinDef  = 716   |c3Resist  = 0
                |c3MaxHp   = 4050  |c3MaxAtk  = 910   |c3MaxDef  = 793   |c3MaxLvl  = 99
                |c3Block   = 2     |c3Range   =       |c3MaxCost = 34    |c3MinCost = 29
                |c3Skill = Achillea of Gales
                |c3SRange  = 180
                |c3SAWBlock= 3     |c3SAWRange= 120
                |c3Ability = Invulnerable Half-God

                |c4Class   = Story Weaver
                |c4MinHp   = 4325  |c4MinAtk  = 911   |c4MinDef  = 846   |c4Resist  = 0
                |c4MaxHp   = 4860  |c4MaxAtk  = 975   |c4MaxDef  = 910
                |c4Block   = 2     |c4Range   =       |c4MaxCost = 34    |c4MinCost = 29
                |c4SRange  = 180
                |c4SAWBlock= 3     |c4SAWRange= 120

                |c5Class   = Awakened Deity
                |c5MinHp   = 4054  |c5MinAtk  = 963   |c5MinDef  = 793   |c5Resist  = 0
                |c5MaxHp   = 4455  |c5MaxAtk  = 1079  |c5MaxDef  = 845
                |c5Block   = 2     |c5Range   =       |c5MaxCost = 34    |c5MinCost = 29
                |c5SRange  = 180
                |c5SAWBlock= 3     |c5SAWRange= 120
                }}</onlyinclude>
                {{Unitlist end}}
                """;

        Unit unit = new Unit();
        unit.parseWikiStats(text);

        assert unit.hero == false;
        assert unit.race.contains(Attribute.HalfGod);

        assert unit.stats2A.hp == 4860;
        assert unit.stats2A.atk == 975;
        assert unit.stats2A.def == 910;
        assert unit.stats2A.mr == 0;
        assert unit.stats2A.range == 0;
        assert unit.stats2A.rangeSkill == 180;
        assert unit.stats2A.rangeSkillAW == 120;
        assert unit.stats2A.block == 2;
        assert unit.stats2A.cost == 34;
        assert unit.stats2A.costMin == 29;

        assert unit.stats2B.hp == 4455;
        assert unit.stats2B.atk == 1079;
        assert unit.stats2B.def == 845;
        assert unit.stats2B.mr == 0;
        assert unit.stats2B.range == 0;
        assert unit.stats2B.rangeSkill == 180;
        assert unit.stats2B.rangeSkillAW == 120;
        assert unit.stats2B.block == 2;
        assert unit.stats2B.cost == 34;
        assert unit.stats2B.costMin == 29;

        assert unit.bounus100.size() == 2;
        assert unit.bounus100.get(0).equals("HP+630");
        assert unit.bounus100.get(1).equals("攻撃力+126");
        assert unit.bounus150.size() == 1;
        assert unit.bounus150.get(0).equals("再使用時間-23%");

    }
}
