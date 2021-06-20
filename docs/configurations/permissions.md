---
layout: page
title: Permissions file
parent: Configuration
---

# Permissions file

---

## Permission nodes

Taterzens supports LuckPerms. If the mod is installed, it will try to
use the permission nodes, but if player doesn't have them set, it will
default to the required permission level, set in [config](./config.html).

## Latest permission nodes

This represents the latest permissions file, generated automatically.
Look at the `_comment` fields to get better explanation of the option.

<button class="btn btn-blue" onclick="fetchNewestPermissions(JSON.parse(localStorage.getItem('TaterzensVersion')))">Refresh</button>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="permissions">Loading ... </code></pre>
</div>

<script src="./permissions.js"></script>