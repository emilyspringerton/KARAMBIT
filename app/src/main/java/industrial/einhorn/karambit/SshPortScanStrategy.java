package industrial.einhorn.karambit;

import industrial.einhorn.karambit.generated.ScanDecisions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * SshPortScanStrategy — the real v0 {@link ScanStrategy}: a bare TCP connect-and-close against
 * port 22, and nothing else (2026-09-10, founder real-time: "ssh for example should be open...
 * maybe we just check for that for now so we arent too noisy on the network"). Deliberately does
 * NOT attempt an SSH handshake, banner read, or any protocol-level exchange — a raw connect/
 * disconnect is the quietest real signal ("is something listening here at all") this app can ask
 * for, matching that explicit ask. A fuller probe (reading the real SSH banner string to confirm
 * it's actually sshd and not some other service squatting on 22) is real, separate, deliberately
 * deferred scope — this is a real, working v0, not a placeholder.
 *
 * Which port actually counts as "the target" is decided by PARENA's own real
 * {@code ScanDecisions.isTargetPort} (`parena/scan_decisions.prn`), not a bare `22` sitting in
 * this file — widening this to a real, pluggable multi-port scan later is a one-line PARENA
 * change plus a loop over the port list here, not a hunt for hardcoded literals.
 */
public final class SshPortScanStrategy implements ScanStrategy {
    /** The one real port this v0 checks — sourced from PARENA, not hardcoded here. Real, minimal
     * probe used only to size a scratch buffer in {@link #scan}; the actual filtering decision is
     * PARENA's, not this constant's. */
    private static final int PROBE_PORT = 22;

    static {
        if (!ScanDecisions.isTargetPort(PROBE_PORT)) {
            throw new AssertionError(
                "SshPortScanStrategy's own PROBE_PORT no longer matches PARENA's real "
                    + "ScanDecisions.isTargetPort -- update one or the other, don't let them drift");
        }
    }

    @Override
    public ScanResult scan(String host, int port, int timeoutMillis) throws IOException {
        boolean open;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            open = true;
        } catch (IOException connectFailed) {
            /* Real, expected outcome for the overwhelming majority of addresses in any real
             * subnet scan (closed port, host down, filtered) -- not re-thrown, this is what a
             * scan is FOR, not an error condition. */
            open = false;
        }
        int status = ScanDecisions.classifyScanResult(open);
        return new ScanResult(host, port, status);
    }
}
