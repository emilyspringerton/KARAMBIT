package industrial.einhorn.karambit;

/**
 * ScanResult — one real, immutable outcome from a {@link ScanStrategy} run against a single
 * host:port pair. {@code status} comes straight from PARENA's own real
 * {@code ScanDecisions.classifyScanResult} — a real, narrow decision (1 = open, 0 = closed),
 * not re-derived independently in Java, so the "what counts as a hit" logic lives in exactly one
 * place. Deliberately a plain data holder — this is the shape a future in-app terminal feature
 * (see this app's own README/NORTHSTAR — "not yet, just sayin") would act on: given a
 * {@link ScanResult} with {@code status == 1} and a port that looks like SSH, offer to open a
 * session. Not built yet; this class's own shape is designed so that's a real, additive feature
 * later, not a rework.
 */
public final class ScanResult {
    public final String host;
    public final int port;
    public final int status;

    public ScanResult(String host, int port, int status) {
        this.host = host;
        this.port = port;
        this.status = status;
    }

    public boolean isOpen() {
        return status == 1;
    }

    @Override
    public String toString() {
        return host + ":" + port + " -> " + (isOpen() ? "OPEN" : "closed");
    }
}
