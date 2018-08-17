# Elite Bot

This is a Discord Bot meant to help with Elite Dangerous.

## Request Format (Phase 1)

You can make the following type of requests:

### Find a system by name and get the details

Will give you all the details and stations for a specific system.
```
system: <system name>
```

### Find the nearest system with criteria

Will find the nearest system that matches the criteria.

```
find: system near: <system name> \
 pad: <L|Large> \
 allegiance: <fed|empire|independent> \
 state: <state> \
 security: <low|med|high> \
 power: <power name> C|E

```

**Fields**

| Field      | Required | Values                                                   |
| ---------- | -------- | -------------------------------------------------------- |
| from       | true     | Name of a system                                         |
| to         | true     | system                                                   |
| pad        | false    | L, large, M, medium, etc                                 |
| allegiance | false    | fed, empire, independent                                 |
| security   | false    | low, med, high                                           |
| power      | false    | Grom, Winters, Hudson, etc. followed by a **C** or **E** |

### Find the nearest interstellar factors

Will find the nearest interstellar factors to you.

```
find: interstellar factors near: <system> pad: <L|Large>
```

### Find the distance between two systems

Gives you the distance between two systems.

```
distance: <system 1> to: <system 2>
```

## *Future Phases*

### Find the nearest material trader

### Find me a material
