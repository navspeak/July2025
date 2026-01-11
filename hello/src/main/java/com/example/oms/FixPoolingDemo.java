package com.example.oms;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FixPoolingDemo {

    // -------------------------------
    // ChildOrder object with reset
    // -------------------------------
    static class ChildOrder {
        String clOrdId;
        String symbol;
        int quantity;

        ChildOrder reset() {
            this.clOrdId = null;
            this.symbol = null;
            this.quantity = 0;
            return this;
        }
    }

    // -------------------------------
    // Simple object pool for ChildOrder
    // -------------------------------
    static class ChildOrderPool {
        private final Queue<ChildOrder> pool = new ConcurrentLinkedQueue<>();

        ChildOrder borrow() {
            ChildOrder o = pool.poll();
            return o != null ? o : new ChildOrder();
        }

        void release(ChildOrder o) {
            pool.offer(o.reset());
        }
    }

    // -------------------------------
    // Thread-local FIX message buffer
    // -------------------------------
    private static final ThreadLocal<StringBuilder> fixBuffer =
            ThreadLocal.withInitial(StringBuilder::new);

    // -------------------------------
    // Serialize ChildOrder to FIX message
    // -------------------------------
    static String serializeFix(ChildOrder order) {
        StringBuilder sb = fixBuffer.get();
        sb.setLength(0); // reset buffer

        sb.append("8=FIX.4.2|");
        sb.append("35=D|"); // NewOrderSingle
        sb.append("11=").append(order.clOrdId).append("|");
        sb.append("55=").append(order.symbol).append("|");
        sb.append("38=").append(order.quantity).append("|");

        return sb.toString();
    }

    // -------------------------------
    // Demo main
    // -------------------------------
    public static void main(String[] args) {

        ChildOrderPool pool = new ChildOrderPool();

        // Simulate multi-threaded order creation
        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                ChildOrder order = pool.borrow();
                order.clOrdId = Thread.currentThread().getName() + "-" + i;
                order.symbol = "AAPL";
                order.quantity = 100 + i;

                String fix = serializeFix(order);
                System.out.println(Thread.currentThread().getName() + " -> " + fix);

                pool.release(order);

                // Simulate small delay
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        };

        Thread t1 = new Thread(task, "Trader-1");
        Thread t2 = new Thread(task, "Trader-2");

        t1.start();
        t2.start();
    }
}

