package com.example.oms;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FixPoolingDemoFlyWeight {

    // -------------------------------
    // ChildOrder object
    // -------------------------------
    static class ChildOrder {
        String clOrdId;
        String symbol;
        int quantity;

        ChildOrder reset() {
            clOrdId = null;
            symbol = null;
            quantity = 0;
            return this;
        }
    }

    // -------------------------------
    // ChildOrder Pool
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
    // Thread-local ByteBuffer
    // -------------------------------
    private static final ThreadLocal<ByteBuffer> fixBuffer =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(256));

    // -------------------------------
    // Flyweight: static part of FIX
    // -------------------------------
    static class FixFlyweight {
        private final byte[] headerBytes;

        FixFlyweight(String header) {
            this.headerBytes = header.getBytes(StandardCharsets.US_ASCII);
        }

        byte[] getHeaderBytes() {
            return headerBytes;
        }
    }

    private static final FixFlyweight NEW_ORDER_HEADER =
            new FixFlyweight("8=FIX.4.2|35=D|");

    // -------------------------------
    // Serialize ChildOrder using Flyweight
    // -------------------------------
    static ByteBuffer serializeFix(ChildOrder order) {
        ByteBuffer buf = fixBuffer.get();
        buf.clear();

        // write static header from Flyweight
        buf.put(NEW_ORDER_HEADER.getHeaderBytes());

        // append dynamic fields
        put(buf, "11=" + order.clOrdId + "|");
        put(buf, "55=" + order.symbol + "|");
        put(buf, "38=" + order.quantity + "|");

        buf.flip();
        return buf;
    }

    private static void put(ByteBuffer buf, String s) {
        buf.put(s.getBytes(StandardCharsets.US_ASCII));
    }

    // -------------------------------
    // Demo main
    // -------------------------------
    public static void main(String[] args) {

        ChildOrderPool pool = new ChildOrderPool();

        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                ChildOrder order = pool.borrow();
                order.clOrdId = Thread.currentThread().getName() + "-" + i;
                order.symbol = "AAPL";
                order.quantity = 100 + i;

                ByteBuffer fixBuf = serializeFix(order);
                System.out.println(Thread.currentThread().getName() + " -> " +
                        new String(fixBuf.array(), 0, fixBuf.limit(), StandardCharsets.US_ASCII));

                pool.release(order);

                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        };

        Thread t1 = new Thread(task, "Trader-1");
        Thread t2 = new Thread(task, "Trader-2");

        t1.start();
        t2.start();
    }
}
