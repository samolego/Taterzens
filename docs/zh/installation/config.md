---
layout: page
title: 配置
parent: Configuration
---

# 配置土豆NPC

---

## 更改配置

在本模组的配置文件中可修改一些本模组的属性，可在以下内容中找到配置。

## 更改语言

默认的语言文件是`en_us`。所有可用的语言文件都可以在[GitHub](https://github.com/samolego/Taterzens/tree/master/common/src/main/resources/data/taterzens/lang)上找到。

<details>
  <summary id="lang_count">所有语言</summary>
    <ul id="languages">
        <li>en_us</li>
    </ul>
</details>


如果你已经运行了游戏，那么可以输入以下指令来更改语言：
```
/taterzens config edit language <语言文件>
```

否则请打开配置文件后更改以下这一行：

<!--Ugly but works :/-->
<div class="highlight"><pre id="__code_1">
<span></span><button class="md-clipboard md-icon" title="Copy to clipboard" data-clipboard-target="#__code_1 > code"></button><code>{
<span class="gd">-  "language": "en_us"</span>
<span class="gi" id="custom_language">+  "language": "custom_language"</span>
}
</code></pre></div>



### 翻译

缺少你自己的语言？请尽情地将[土豆NPC](https://github.com/samolego/Taterzens#translation-contributions)模组里的语言更改为你自己国家的语言！

???+ 备注
    土豆NPC也支持使用[服务器的翻译模式](https://github.com/arthurbambou/Server-Translations)为每个玩家设置语言。


## 游戏中的编辑

土豆NPC也支持在游戏中进行编辑配置，请查看下方视频。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/F9KT2RAN3IA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## 最新配置

以下内容代表最新的配置文件，将会自动生成，请多查看`注释`来了解该语言文件的最新内容的使用解释。



<button class="md-button" onclick="fetchNewData(JSON.parse(localStorage.getItem('TaterzensVersion')))">
    刷新
</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="config">加载中 ... </code></pre>
</div>

<script src="../../scripts/config.js"></script>
