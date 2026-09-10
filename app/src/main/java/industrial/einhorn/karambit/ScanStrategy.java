package industrial.einhorn.karambit;

import java.io.IOException;

/**
 * ScanStrategy — the real, pluggable seam this whole app is built around (2026-09-10, founder
 * real-time: "keep it pluggable and extensible we may very well want to build an in app terminal
 * right there to connect to it (not yet just sayin)"). Any real, single host:port probe — a bare
 * TCP connect (v0, see {@link SshPortScanStrategy}), later a banner grab, an HTTP HEAD, a real
 * SSH handshake attempt — implements this one real interface. {@link ScanEngine} depends only on
 * this interface, never on a concrete strategy, so adding a second real probe kind later means
 * writing one new class, not touching the engine.
 */
public interface ScanStrategy {
    /**
     * Probe a single host:port pair. Real, honest contract: returns a real {@link ScanResult}
     * (never throws for an ordinary closed/unreachable port — that's a real, expected scan
     * outcome, not an error) but MAY throw {@link IOException} for a genuinely exceptional
     * condition (e.g. the local network interface itself vanished mid-scan).
     */
    ScanResult scan(String host, int port, int timeoutMillis) throws IOException;
}
