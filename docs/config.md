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

The default provided language file is `en_us`. All available languages can be found on [GitHub](https://github.com/samolego/Taterzens/tree/master/common/src/main/resources/data/taterzens/lang).


If you are already running Minecraft, you can use a command
to change language. Supported languages will show up in suggestions.
```
/taterzens config edit language <language>
```

Otherwise, open up the config file and change the following line
```diff
{
-  "language": "en_us"
+  "language": "custom_language"
}
```

### Translations

Missing a language? Feel free to [translate Taterzens](https://github.com/samolego/Taterzens#translation-contributions)!

???+ note
    Taterzens also supports per-player translations using [Server Translations Mod](https://github.com/arthurbambou/Server-Translations).


## Latest config

This represents the latest config file, generated automatically.
Look at the `_comment` fields to get better explanation of the option.

<button class="md-button" onclick="fetchNewestConfig(JSON.parse(localStorage.getItem('TaterzensVersion')))">
    Refresh
</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="config">Loading ... </code></pre>
</div>

<script src="../scripts/config.js"></script>