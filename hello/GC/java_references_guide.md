# Java Reference Types and WeakHashMap Guide

## 1. Reference Types Overview

  ----------------------------------------------------------------------------------------------------------------------
  Reference Type            Sample Code                                                          What They Do
  ------------------------- -------------------------------------------------------------------- -----------------------
  Strong Reference          `Person p = new Person();`                                           Object will not be
                                                                                                 garbage collected as
                                                                                                 long as strong
                                                                                                 reference exists

  Soft Reference            `SoftReference<Person> ref = new SoftReference<>(p);`                Collected only under
                                                                                                 memory pressure

  Weak Reference            `WeakReference<Person> ref = new WeakReference<>(p);`                Collected eagerly on
                                                                                                 next GC cycle

  Phantom Reference         `PhantomReference<Person> ref = new PhantomReference<>(p, queue);`   Used for post-cleanup
                                                                                                 tracking; object
                                                                                                 already gone
  ----------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 2. Code Snippets

### Strong Reference

``` java
Person p = new Person("John");
System.out.println(p);
```

------------------------------------------------------------------------

### Soft Reference

``` java
Person p = new Person("John");
SoftReference<Person> softRef = new SoftReference<>(p);

p = null; // remove strong reference
System.gc();

System.out.println(softRef.get()); // may still be present unless memory pressure
```

------------------------------------------------------------------------

### Weak Reference

``` java
Person p = new Person("John");
WeakReference<Person> weakRef = new WeakReference<>(p);

p = null;
System.gc();

System.out.println(weakRef.get()); // usually null after GC
```

------------------------------------------------------------------------

### Phantom Reference (simplified)

``` java
ReferenceQueue<Person> queue = new ReferenceQueue<>();
Person p = new Person("John");

PhantomReference<Person> phantomRef =
    new PhantomReference<>(p, queue);

p = null;
System.gc();

Reference<?> ref = queue.poll();
if (ref != null) {
    System.out.println("Object ready for cleanup");
}
```

Phantom references are used when you need to run cleanup logic **after
the object has been finalized and is about to be removed from memory**
(e.g., native resource cleanup).

------------------------------------------------------------------------

## 3. HashMap vs WeakHashMap

  Feature                          HashMap           WeakHashMap
  -------------------------------- ----------------- --------------------------------
  Key Reference Type               Strong            Weak
  GC removes entry automatically   ❌ No             ✅ Yes
  Predictability                   Stable            Entries may disappear after GC
  Use case                         General purpose   Caches, listeners, metadata
  Thread-safe                      ❌ No             ❌ No

------------------------------------------------------------------------

## Key Takeaways

-   Strong references keep objects alive
-   Soft references survive until memory pressure
-   Weak references are collected eagerly
-   Phantom references are used for cleanup tracking
-   WeakHashMap automatically removes entries when keys are no longer
    strongly referenced
