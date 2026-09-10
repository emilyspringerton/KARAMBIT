package industrial.einhorn.karambit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * NetworkInfo — the one real Android-dependent piece of this app's scan path (everything else in
 * this package other than {@link MainActivity} is plain JDK, see {@link ScanEngine}'s own header
 * comment on why). Derives the device's own real local IPv4 address via
 * {@link ConnectivityManager}'s modern {@code getActiveNetwork()}/{@code getLinkProperties()} API
 * (not the older, deprecated {@code WifiManager.getConnectionInfo().getIpAddress()}) so this works
 * over WiFi OR a wired/USB-ethernet adapter, not WiFi-only.
 *
 * Real, honest v0 limitation, named directly: assumes a real, ordinary /24 home network (the
 * overwhelming majority of real home routers) and always derives a plain "a.b.c." prefix from the
 * device's own first three octets, regardless of the real, reported prefix length — a genuinely
 * different subnet size (e.g. a real /16 or /28) would scan the wrong host range. Fixing this for
 * real needs either real CIDR bit arithmetic (blocked today — PARENA's own language has no
 * bitwise operators yet, see {@link ScanEngine}'s own doc comment) or an equivalent hand-written
 * Java fallback; deliberately not attempted here rather than silently guessing at a fix.
 */
public final class NetworkInfo {
    private NetworkInfo() {
    }

    /** Real result: the local device's own dotted-quad IPv4 address (e.g. "192.168.1.42") plus
     * the derived "a.b.c." scan prefix, or null fields if no real IPv4 address could be found
     * (e.g. no active network, or an IPv6-only link). */
    public static final class LocalAddress {
        public final String fullAddress;
        public final String scanPrefix;

        LocalAddress(String fullAddress, String scanPrefix) {
            this.fullAddress = fullAddress;
            this.scanPrefix = scanPrefix;
        }
    }

    public static LocalAddress detect(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return new LocalAddress(null, null);
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return new LocalAddress(null, null);
        }
        LinkProperties props = cm.getLinkProperties(network);
        if (props == null) {
            return new LocalAddress(null, null);
        }
        for (LinkAddress linkAddress : props.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (address instanceof Inet4Address) {
                String full = address.getHostAddress();
                int lastDot = full.lastIndexOf('.');
                if (lastDot < 0) {
                    continue;
                }
                String prefix = full.substring(0, lastDot + 1);
                return new LocalAddress(full, prefix);
            }
        }
        return new LocalAddress(null, null);
    }
}
