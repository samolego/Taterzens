---
layout: page
title: 源代码构建
parent: Installation
---

# 源代码构建

---

## 构建模组

使用源代码时请注意备份，否则现有的模组NPC将会被覆盖！

* 请从[GH actions](https://github.com/samolego/Taterzens/actions/workflows/build.yml)网站上下载本模组的最新测试版！

* 自行构建模组。
```bash
git clone https://github.com/samolego/Taterzens.git
cd Taterzens
chmod +x gradlew
gradlew build
```
这些文件都可以在`build/libs`中找到。