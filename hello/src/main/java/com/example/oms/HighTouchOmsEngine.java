package com.example.oms;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class HighTouchOmsEngine {

    /* ========= DOMAIN ========= */
    enum Zone { US, EU, APAC }
    enum State { NEW, SENT, PARTIAL, FILLED, FAILED }

    record Instruction(String symbol, long qty, Zone primary, List<Zone> fallback) {}
    record ExecReport(long filledQty, boolean finalFill) {}

    static class ChildOrder {
        String clOrdId;
        String symbol;
        long totalQty;
        Zone primary;
        List<Zone> fallback;

        volatile long filled;
        volatile State state = State.NEW;

        ChildOrder() { clOrdId = UUID.randomUUID().toString(); symbol = ""; totalQty = 0; primary = Zone.US; fallback = List.of(); }

        void init(Instruction i) {
            filled = 0;
            state = State.NEW;
            clOrdId = UUID.randomUUID().toString();
            symbol = i.symbol();
            totalQty = i.qty();
            primary = i.primary();
            fallback = i.fallback();
        }

        long remaining() { return totalQty - filled; }
    }

    static class Basket {
        final String basketId;
        final Map<String, ChildOrder> children = new ConcurrentHashMap<>();
        final AtomicLong totalFilled = new AtomicLong();

        Basket(String id) { this.basketId = id; }

        void update() {
            boolean done = children.values().stream()
                    .allMatch(c -> c.state == State.FILLED || c.state == State.FAILED);
            if (done) System.out.println("Basket " + basketId + " COMPLETE. Filled=" + totalFilled.get());
        }
    }

    /* ========= EXECUTION ========= */
    private static final Map<Zone, ExecutorService> EXEC = Map.of(
            Zone.US, Executors.newFixedThreadPool(4),
            Zone.EU, Executors.newFixedThreadPool(4),
            Zone.APAC, Executors.newFixedThreadPool(4)
    );

    /* ========= OBJECT POOL ========= */
    private static final Queue<ChildOrder> orderPool = new ConcurrentLinkedQueue<>();

    private static ChildOrder borrowOrder(Instruction instr) {
        ChildOrder o = orderPool.poll();
        if (o == null) o = new ChildOrder();
        o.init(instr);
        return o;
    }

    private static void releaseOrder(ChildOrder o) { orderPool.offer(o); }

    /* ========= THREAD-LOCAL FIX BUFFER ========= */
    private static final ThreadLocal<ByteBuffer> fixBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(256));
    private static final byte[] HEADER_BYTES = "8=FIX.4.2|35=D|".getBytes(StandardCharsets.US_ASCII);

    private static void sendFix(ChildOrder o, Zone z) {
        ByteBuffer buf = fixBuffer.get();
        buf.clear();
        buf.put(HEADER_BYTES);
        buf.put(("11=" + o.clOrdId + "|").getBytes(StandardCharsets.US_ASCII));
        buf.put(("55=" + o.symbol + "|").getBytes(StandardCharsets.US_ASCII));
        buf.put(("38=" + o.remaining() + "|").getBytes(StandardCharsets.US_ASCII));
        buf.put(("207=" + z + "|").getBytes(StandardCharsets.US_ASCII));
        buf.flip();
        System.out.println("SEND -> " + new String(buf.array(), 0, buf.limit(), StandardCharsets.US_ASCII));
    }

    /* ========= ASYNC EXECUTION SIMULATION ========= */
    private static final ScheduledExecutorService SIM_EXEC = Executors.newScheduledThreadPool(4);
    private static final Random RANDOM = new Random();

    private static void asyncExecution(ChildOrder order, Basket basket, Zone zone) {
        // Simulate async venue response after random delay (non-blocking)
        int delay = 50 + RANDOM.nextInt(100); // 50-150ms
        SIM_EXEC.schedule(() -> {
            try {
                // Simulate possible failure
                if (RANDOM.nextDouble() < 0.2) throw new TimeoutException();

                long fill = Math.min(order.remaining(), Math.max(1, order.totalQty / 2));
                boolean done = order.remaining() - fill == 0;
                ExecReport report = new ExecReport(fill, done);
                handleExec(basket, order, report);
            } catch (Exception e) {
                fallback(basket, order, zone);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /* ========= ROUTING + FALLBACK ========= */
    private static void route(Basket basket, ChildOrder order, Zone zone) {
        EXEC.get(zone).submit(() -> {
            sendFix(order, zone);
            order.state = State.SENT;
            asyncExecution(order, basket, zone); // async callback
        });
    }

    private static void fallback(Basket basket, ChildOrder order, Zone failed) {
        for (Zone z : order.fallback) {
            if (z != failed && order.remaining() > 0) {
                route(basket, order, z);
                return;
            }
        }
        order.state = State.FAILED;
        basket.update();
        releaseOrder(order);
    }

    private static void handleExec(Basket basket, ChildOrder order, ExecReport r) {
        order.filled += r.filledQty();
        order.state = r.finalFill() ? State.FILLED : State.PARTIAL;
        basket.totalFilled.addAndGet(r.filledQty());
        basket.update();
        if (order.state == State.FILLED || order.state == State.FAILED) releaseOrder(order);
    }

    /* ========= EXECUTE BASKET ========= */
    public static void executeBasket(Basket basket, List<Instruction> instructions) {
        instructions.forEach(instr -> {
            ChildOrder co = borrowOrder(instr);
            basket.children.put(co.clOrdId, co);
            route(basket, co, co.primary);
        });
    }

    /* ========= DEMO ========= */
    public static void main(String[] args) {
        Basket basket = new Basket("GLOBAL-BASKET-01");
        List<Instruction> instructions = List.of(
                new Instruction("AAPL", 1000, Zone.APAC, List.of(Zone.EU, Zone.US)),
                new Instruction("SAP", 500, Zone.APAC, List.of(Zone.EU)),
                new Instruction("TSLA", 800, Zone.US, List.of(Zone.EU))
        );
        executeBasket(basket, instructions);
    }
}
