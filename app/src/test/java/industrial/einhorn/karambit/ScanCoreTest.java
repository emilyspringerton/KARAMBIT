package industrial.einhorn.karambit;

import industrial.einhorn.karambit.generated.ScanDecisions;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ScanCoreTest — real, plain-JVM verification for KARAMBIT's core scan logic (no Android SDK, no
 * emulator, no JUnit — see BUILD.bazel's own header comment on why). Same real "PASS: .../FAIL:
 * ..." counted-assertion convention this whole monorepo's own test suites already use.
 *
 * Real, honest split, mirroring what's actually hermetic: PARENA's own generated
 * {@code ScanDecisions} and {@link ScanEngine}'s own loop logic are checked against a fake,
 * in-memory {@link ScanStrategy} (fast, deterministic, no real sockets); {@link
 * SshPortScanStrategy} itself is checked against a REAL {@link ServerSocket} bound to
 * {@code 127.0.0.1} on an ephemeral port — a genuine, real TCP connect/close round trip, not a
 * mock, proving the actual socket-based scan logic really works end to end.
 */
public final class ScanCoreTest {
    private static int pass = 0;
    private static int fail = 0;

    private static void check(boolean cond, String msg) {
        if (cond) {
            pass++;
            System.out.println("PASS: " + msg);
        } else {
            fail++;
            System.out.println("FAIL: " + msg);
        }
    }

    public static void main(String[] args) throws Exception {
        // --- real PARENA-generated decision logic, exercised directly ---
        check(ScanDecisions.isTargetPort(22), "port 22 is the real v0 target port");
        check(!ScanDecisions.isTargetPort(80), "port 80 is correctly NOT the target port");
        check(ScanDecisions.hasMoreHosts(0, 5), "index 0 of 5 hosts means more hosts remain");
        check(!ScanDecisions.hasMoreHosts(5, 5), "index 5 of 5 hosts means no more hosts remain (real off-by-one check)");
        check(ScanDecisions.nextScanIndex(3) == 4, "next-scan-index advances by exactly one");
        check(ScanDecisions.classifyScanResult(true) == 1, "an open result classifies to real status 1");
        check(ScanDecisions.classifyScanResult(false) == 0, "a closed result classifies to real status 0");

        // --- ScanEngine's own real host-enumeration loop, against a fake, deterministic strategy ---
        {
            List<String> hostsSeen = Collections.synchronizedList(new ArrayList<>());
            ScanStrategy fake = (host, port, timeoutMillis) -> {
                hostsSeen.add(host);
                boolean open = host.endsWith(".2") || host.endsWith(".4");
                return new ScanResult(host, port, ScanDecisions.classifyScanResult(open));
            };
            List<ScanResult> results = new CopyOnWriteArrayList<>();
            ScanEngine engine = new ScanEngine(fake, 100);
            engine.scan("10.0.0.", 1, 5, 22, results::add);

            check(hostsSeen.size() == 5, "ScanEngine probes exactly every host in the given inclusive range (real off-by-one check)");
            check(hostsSeen.contains("10.0.0.1") && hostsSeen.contains("10.0.0.5"),
                "ScanEngine's own range is real and inclusive at both ends");
            check(results.size() == 5, "ScanEngine reports one real result per host, including closed ones");
            long openCount = results.stream().filter(ScanResult::isOpen).count();
            check(openCount == 2, "ScanEngine correctly reports exactly the real open hosts from the fake strategy");
        }

        // --- SshPortScanStrategy, against a REAL local TCP server (a genuine socket round trip) ---
        {
            try (ServerSocket server = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
                int realOpenPort = server.getLocalPort();
                Thread acceptThread = new Thread(() -> {
                    try {
                        server.accept().close();
                    } catch (IOException ignored) {
                        // real, expected once the test closes the server socket below
                    }
                });
                acceptThread.setDaemon(true);
                acceptThread.start();

                SshPortScanStrategy strategy = new SshPortScanStrategy();
                ScanResult openResult = strategy.scan("127.0.0.1", realOpenPort, 500);
                check(openResult.isOpen(), "a real, genuinely listening local TCP port is correctly reported OPEN");

                int probablyClosedPort = realOpenPort + 1 <= 65535 ? realOpenPort + 1 : realOpenPort - 1;
                ScanResult closedResult = strategy.scan("127.0.0.1", probablyClosedPort, 500);
                check(!closedResult.isOpen(), "a real, non-listening local TCP port is correctly reported closed");
            }
        }

        System.out.println();
        System.out.println(pass + " passed, " + fail + " failed");
        if (fail > 0) {
            System.exit(1);
        }
    }
}
