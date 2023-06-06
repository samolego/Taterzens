---
title: Locking
---

# Owning taterzens

---

## Defaults
Every taterzen is locked to a specific player after being created.
*To disable this behavior, edit the [config](../../installation/config).*
```brigadier
/taterzens config edit lockAfterCreation false
```

## Why is it useful?
You might need a taterzen with `command-permission-level` set to `4`, as
you want it to be able to execute *all* [commands](../command_actions/).
But you also want all your players to have their own taterzens and be able to edit them.

This could mean that someone could edit your taterzen and add a `/stop` command to it,
making it stop the server every time when right clicked upon.


## Unlocking
To make taterzen editable for everyone (with permission), use the below command.
```brigadier
/npc unlock
```

## Locking back
If you change you mind, you can lock the taterzen again with the below command.
```brigadier
/npc lock
```
