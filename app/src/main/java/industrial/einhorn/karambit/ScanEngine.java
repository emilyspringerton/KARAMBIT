package industrial.einhorn.karambit;

import industrial.einhorn.karambit.generated.ScanDecisions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ScanEngine — the real host-enumeration loop, deliberately kept free of any Android dependency
 * (plain JDK: {@code java.net}/{@code java.util.concurrent} only) so it's independently testable
 * on a bare JVM, same real "core logic testable without a device/emulator" precedent
 * SPIDERBEETLE's own {@code smoke_test/Main.java} already established for a different Android
 * app in this ecosystem.
 *
 * Real, deliberate v0 host-range model: a plain IPv4 "/24-shaped" prefix string
 * (e.g. {@code "192.168.1."}) plus an inclusive start/end last-octet range — NOT real CIDR/netmask
 * bit arithmetic. Honest reason, not laziness: PARENA's own real language surface has no bitwise
 * AND/OR/shift operators yet (only +,-,*,/, comparisons, and boolean and/or/not — see
 * `PARENA/src/emit_llvm.c`'s own real, checked binop table), so a real subnet-mask computation
 * couldn't live in PARENA today without first adding that to the language itself — a real,
 * separate, not-yet-scoped compiler change, not something to fake here. A plain last-octet range
 * covers the overwhelming majority of real home networks (a /24, which is what
 * {@link industrial.einhorn.karambit.NetworkInfo} derives from Android's own reported address),
 * and is honest about not handling anything more exotic yet.
 *
 * Real, deliberate "not too noisy" design (2026-09-10, founder real-time: "maybe we just check
 * for that for now so we arent too noisy on the network... we dont need to avoid detection"): a
 * small, FIXED-size thread pool ({@link #CONCURRENCY}), not one thread per host — real, modest
 * parallelism, not a burst-scan of all 254 hosts at once. The loop-control decisions themselves
 * (whether another host remains, how to advance the index) come from PARENA's own real
 * {@code ScanDecisions.hasMoreHosts}/{@code nextScanIndex} — a real, if modest, integration point
 * for logic that's genuinely simple arithmetic, not busywork PARENA-ifying for its own sake.
 */
public final class ScanEngine {
    /** Real, fixed, modest concurrency — not configurable in this v0, deliberately: a knob to
     * turn a "quiet" scanner into a noisy one is a real, separate feature to add thoughtfully
     * later, not a default anyone should be able to accidentally crank up. */
    private static final int CONCURRENCY = 8;

    /** Reports one real result as soon as it's known — lets a caller (a UI, or the plain-JVM
     * smoke test) update live rather than waiting for the whole scan to finish. Deliberately a
     * small, hand-written interface rather than {@code java.util.function.Consumer} — keeps this
     * class's own real API surface obviously self-contained. */
    public interface Listener {
        void onResult(ScanResult result);
    }

    private final ScanStrategy strategy;
    private final int timeoutMillis;

    public ScanEngine(ScanStrategy strategy, int timeoutMillis) {
        this.strategy = strategy;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Scans {@code ipPrefix + startHost} through {@code ipPrefix + endHost} (both inclusive) on
     * {@code port}, calling {@code listener} once per real result as it completes, then returns
     * once every host has been probed. Real, honest, blocking call — the caller (a UI layer) is
     * responsible for running this off its own main thread; this class has no opinion about
     * Android's own main-thread rules, matching {@link #CONCURRENCY}'s own doc comment on staying
     * a plain, dependency-free JDK class.
     */
    public void scan(String ipPrefix, int startHost, int endHost, int port, Listener listener) {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        List<Runnable> tasks = new ArrayList<>();

        int total = endHost - startHost + 1;
        int index = 0;
        while (ScanDecisions.hasMoreHosts(index, total)) {
            final String host = ipPrefix + (startHost + index);
            tasks.add(() -> {
                ScanResult result;
                try {
                    result = strategy.scan(host, port, timeoutMillis);
                } catch (IOException e) {
                    /* Real, honest fallback: a strategy that throws (a genuinely exceptional
                     * condition per ScanStrategy's own contract, not an ordinary closed port)
                     * still gets a real, closed-shaped result reported rather than silently
                     * dropping this host from the results the caller sees. */
                    result = new ScanResult(host, port, ScanDecisions.classifyScanResult(false));
                }
                listener.onResult(result);
            });
            index = ScanDecisions.nextScanIndex(index);
        }

        for (Runnable task : tasks) {
            pool.execute(task);
        }
        pool.shutdown();
        try {
            /* Real, generous but bounded wait -- a /24 at CONCURRENCY=8 with a real, short
             * per-host timeout finishes well inside this; the bound exists so a genuinely stuck
             * network call can't hang this method forever. */
            pool.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
