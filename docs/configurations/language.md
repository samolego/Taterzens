---
layout: page
title: Language file
parent: Configuration
---

# Language file

---

## Changing language


The default provided language file is `en_us.json`.

It's up to you whether you'll edit the values directly or create a custom
file with translated messages. When doing the latter, make sure you edit the
`language` field in your `config.json`.

```diff
{
-  "language": "en_us"
+  "language": "custom_language"
}
```
 
## Contributing
todo
{: .label .label-yellow }

Want to contribute your translation of Taterzen language file for others to enjoy?
Submit a PR (Pull Request) on GitHub for the `translations` branch.

## Latest language file

This represents the latest language file, generated automatically.

<button class="btn btn-blue" onclick="fetchNewestLanguage(JSON.parse(localStorage.getItem('TaterzensVersion')))">Refresh</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="language">Loading ... </code></pre>
</div>

<script src="../language.js"></script>
