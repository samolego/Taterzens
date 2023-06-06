---
layout: page
title: Configurations
parent: Configuration
---

# Configuring Taterzens

---

## Changing configuration

Configuration file includes some properties for
newly created Taterzens. Find all the settings below.


## Changing language

The default provided language file is `en_us`.
All available languages can also be found on [GitHub](https://github.com/samolego/Taterzens/tree/master/common/src/main/resources/data/taterzens/lang).

<details>
  <summary id="lang_count">All languages</summary>
    <ul id="languages">
        <li>en_us</li>
    </ul>
</details>


If you are already running Minecraft, you can use a command
to change language. Supported languages will show up in suggestions.
```
/taterzens config edit language <language>
```

Otherwise, open up the config file and change the following line

<!--Ugly but works :/-->
<div class="highlight"><pre id="__code_1">
<span></span><button class="md-clipboard md-icon" title="Copy to clipboard" data-clipboard-target="#__code_1 > code"></button><code>{
<span class="gd">-  "language": "en_us"</span>
<span class="gi" id="custom_language">+  "language": "custom_language"</span>
}
</code></pre></div>



### Translations

Missing a language? Feel free to [translate Taterzens](https://github.com/samolego/Taterzens#translation-contributions)!

???+ note
    Taterzens also supports per-player translations using [Server Translations Mod](https://github.com/arthurbambou/Server-Translations).


## In-game editing

Taterzens also supports editing config in-game. See below video for example.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/F9KT2RAN3IA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Latest config

This represents the latest config file, generated automatically.
Look at the `_comment` fields to get better explanation of the option.



<button class="md-button" onclick="fetchNewData(JSON.parse(localStorage.getItem('TaterzensVersion')))">
    Refresh
</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="config">Loading ... </code></pre>
</div>

<script src="../../scripts/config.js"></script>
