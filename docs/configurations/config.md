---
layout: page
title: Configuration file
parent: Configuration
---

# Configuration file

---

## Changing configuration

Configuration file includes some properties for
newly created Taterzens.
It also allows you to edit the color of particles, used for
[path](../playing/path.html) visualising.

## Latest config

This represents the latest config file, generated automatically.
Look at the `_comment` fields to get better explanation of the option.

<button class="btn btn-blue" onclick="fetchNewestConfig(JSON.parse(localStorage.getItem('TaterzensVersion')))">Refresh</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="config">Loading ... </code></pre>
</div>

<script src="../config.js"></script>