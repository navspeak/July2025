package com.example.spring;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class CIExample {

    public static void main(String[] args) {
        try {
            List<byte[]> keep = new ArrayList<>();
            long maxHeap   = Runtime.getRuntime().maxMemory();
            long totalHeap = Runtime.getRuntime().totalMemory();
            long freeHeap  = Runtime.getRuntime().freeMemory();
            long usedHeap  = totalHeap - freeHeap;

            System.out.println("Max Heap     = " + maxHeap);
            System.out.println("Total Heap   = " + totalHeap);
            System.out.println("Used Heap    = " + usedHeap);
            System.out.println("Free Heap    = " + freeHeap);
            while (true) {
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
                keep.add(new byte[1_000_000]); // ~1MB
            }
        } catch (OutOfMemoryError e) {
            System.out.println("OOM");
        }
    }

}

@Component
class A {
    public void sendEmail(String e) {
    }
}

@Service
class B {
    private A a;

    public B(A a) {
        this.a = a;
    }

    void createAndSend() {
        String e = "email";
        a.sendEmail(e);
    }
}


