---
layout: page
title: Source Installation
parent: Installation
---

# Source Installation

---

## Compilation of Taterzens

Here be dragons!
It might happen that your existing Taterzens will get wiped!
Proceed with caution!

* Download latest unstable version from [GH actions](https://github.com/samolego/Taterzens/actions/workflows/build.yml).

* Build the mod yourself.
```bash
git clone https://github.com/samolego/Taterzens.git
cd Taterzens
chmod +x gradlew
gradlew build
```
The files can be found in `build/libs` folder.