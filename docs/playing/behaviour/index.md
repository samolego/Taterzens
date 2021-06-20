---
layout: page
title: Behaviour
parent: Play
has_children: true
nav_order: 5
---


# Behaviour

---


## Changing the behaviour

Default Taterzen behaviour will be [passive](./passive.html).
*~~It's up to you whether you dare to change it.~~*

If you use any other types than the default one, you'd
probably want to change their invulnerable status as well.
To achieve that, use
```
/npc edit invulnerable <true/false>
```

(If you didn't know, you can use default
vanilla way to achieve the same effect as well.)
```
/data merge entity <taterzen uuid> {Invulnerable:1b}
```
