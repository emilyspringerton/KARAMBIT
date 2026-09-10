package industrial.einhorn.karambit;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainActivity — KARAMBIT's real v0 UI (2026-09-10, founder real-time: "build a toolkit android
 * app to help scan my network to see if i can find it [a headless Raspberry Pi]... just want to
 * quickly check to see if there is a machine on the network with port 22 ready for action").
 *
 * Real, honest v0 flow: on launch, detect the device's own local IP/scan prefix
 * ({@link NetworkInfo}); on button tap, run a real {@link ScanEngine} (backed by the real
 * {@link SshPortScanStrategy}, PARENA-decided classification) across the local /24 on a
 * background executor, appending each real result to the on-screen log as it completes.
 */
public final class MainActivity extends Activity {
    /** Real, single-threaded executor: this Activity only ever has one scan in flight at a time
     * (the button is disabled while a scan runs) — a fixed pool bigger than 1 here would be
     * pointless, {@link ScanEngine}'s own internal pool is where the real per-host concurrency
     * lives. */
    private final ExecutorService uiExecutor = Executors.newSingleThreadExecutor();

    private TextView statusText;
    private TextView resultsText;
    private Button scanButton;
    private ScrollView resultsScroll;
    private String scanPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        resultsText = findViewById(R.id.results_text);
        scanButton = findViewById(R.id.scan_button);
        resultsScroll = (ScrollView) resultsText.getParent();

        scanButton.setOnClickListener(this::onScanClicked);

        NetworkInfo.LocalAddress local = NetworkInfo.detect(this);
        if (local.fullAddress == null) {
            statusText.setText("No local IPv4 address found — connect to a network first.");
            scanButton.setEnabled(false);
            return;
        }
        scanPrefix = local.scanPrefix;
        statusText.setText("This device: " + local.fullAddress
            + "\nWill scan: " + scanPrefix + "1–" + scanPrefix + "254, port 22");
    }

    private void onScanClicked(View unused) {
        scanButton.setEnabled(false);
        resultsText.setText("");
        appendLine("Scanning " + scanPrefix + "1–254 on port 22 …");

        uiExecutor.execute(() -> {
            ScanEngine engine = new ScanEngine(new SshPortScanStrategy(), /* timeoutMillis= */ 300);
            engine.scan(scanPrefix, 1, 254, 22, result -> {
                if (result.isOpen()) {
                    runOnUiThread(() -> appendLine(result.toString()));
                }
            });
            runOnUiThread(() -> {
                appendLine("Scan complete.");
                scanButton.setEnabled(true);
            });
        });
    }

    private void appendLine(String line) {
        resultsText.append(line + "\n");
        resultsScroll.post(() -> resultsScroll.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        uiExecutor.shutdownNow();
        super.onDestroy();
    }
}
