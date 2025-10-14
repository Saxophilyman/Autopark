package org.example.autopark.nplusone;

public class QueryCountHolder {

    private static final ThreadLocal<Counter> holder = ThreadLocal.withInitial(Counter::new);

    public static void reset() {
        holder.set(new Counter());
    }

    public static void incrementSelect() {
        holder.get().select++;
    }

    public static Counter get() {
        return holder.get();
    }

    public static class Counter {
        int select = 0;
        public int getSelect() { return select; }
        @Override
        public String toString() {
            return "Counter{select=" + select + '}';
        }
    }
}
