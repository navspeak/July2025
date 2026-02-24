package com.example.brushup;

public class StringIntering {
    /*
- Two literals with same text share the same identity → == true
- new String("...") creates different identities → == false but .equals true
- intern() returns the pooled/canonical identity → often makes == true vs the literal
- Compile-time concatenation gets pooled; runtime concatenation usually doesn’t
     */
    public static void main(String[] args) {

        // ---- Literals (String pool) ----
        String pooledLiteralA = "Navneet";
        String pooledLiteralB = "Navneet";

        // ---- Heap objects ----
        String heapObjectA = new String("Navneet");
        String heapObjectB = new String("Navneet");

        // ---- Intern results ----
        String internFromLiteral = pooledLiteralA.intern();   // already pooled
        String internFromHeap = heapObjectA.intern();         // returns pooled

        // ---- A value not yet present as a literal in code ----
        String heapNewValue = new String("Navneet_");
        String pooledFromNewValue = heapNewValue.intern();    // canonical pooled ref

        // ---- Inline intern ----
        String inlineIntern = new String("Navneet").intern();

        // ---- Another heap object again ----
        String anotherHeap = new String("Navneet");

        System.out.println("=========== OBJECT IDENTITY (identityHashCode) ===========");
        printId("pooledLiteralA", pooledLiteralA);
        printId("pooledLiteralB", pooledLiteralB);
        printId("heapObjectA", heapObjectA);
        printId("heapObjectB", heapObjectB);
        printId("internFromLiteral", internFromLiteral);
        printId("internFromHeap", internFromHeap);
        printId("heapNewValue", heapNewValue);
        printId("pooledFromNewValue", pooledFromNewValue);
        printId("inlineIntern", inlineIntern);
        printId("anotherHeap", anotherHeap);

        System.out.println("\n=========== REFERENCE (==) vs VALUE (.equals) ===========");
        compare("pooledLiteralA", pooledLiteralA, "pooledLiteralB", pooledLiteralB);
        compare("pooledLiteralA", pooledLiteralA, "heapObjectA", heapObjectA);
        compare("heapObjectA", heapObjectA, "heapObjectB", heapObjectB);

        compare("pooledLiteralA", pooledLiteralA, "internFromLiteral", internFromLiteral);
        compare("pooledLiteralA", pooledLiteralA, "internFromHeap", internFromHeap);
        compare("heapObjectA", heapObjectA, "internFromHeap", internFromHeap);

        compare("heapNewValue", heapNewValue, "pooledFromNewValue", pooledFromNewValue);
        compare("pooledLiteralA", pooledLiteralA, "inlineIntern", inlineIntern);
        compare("pooledLiteralA", pooledLiteralA, "anotherHeap", anotherHeap);

        System.out.println("\n=========== CONCAT TRICK (compile-time vs runtime) ===========");
        String compileTimeConcat = "Nav" + "neet";          // compile-time => pooled
        String runtimeConcat = "Nav"; runtimeConcat += "neet"; // runtime => new object (usually heap)
        printId("compileTimeConcat", compileTimeConcat);
        printId("runtimeConcat", runtimeConcat);

        compare("pooledLiteralA", pooledLiteralA, "compileTimeConcat", compileTimeConcat);
        compare("pooledLiteralA", pooledLiteralA, "runtimeConcat", runtimeConcat);

        System.out.println("\nNOTE: identityHashCode is NOT a memory address, but helps see object identity.");

        String s = "Navneet";
        String s1 = "Nav" + "neet";
        String s3 = "Nav";
        s3 = s3 + "neet";
        System.out.println("s == s1 "+ (s==s1));
        System.out.println("s == s3 "+ (s==s3));
    }

    static void printId(String name, String s) {
        System.out.printf("%-18s -> value='%s', identity=%d%n", name, s, System.identityHashCode(s));
    }

    static void compare(String n1, String a, String n2, String b) {
        System.out.printf("%-18s == %-18s ? %-5s | equals ? %-5s%n",
                n1, n2, (a == b), a.equals(b));
    }

}
/*
        HEAP MEMORY
   -----------------------
   |                     |
   |   String Pool       |  ← part of heap
   |   "Navneet"         |
   |                     |
   |   Heap Objects      |
   |   new String()      |
   |                     |
   -----------------------

   JVM flag
-XX:+UseStringDeduplication

G1 GC can automatically deduplicate strings in heap even if you don’t call intern()
 */