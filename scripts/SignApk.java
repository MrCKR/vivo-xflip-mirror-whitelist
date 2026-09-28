import com.android.apksig.ApkSigner;
import com.android.apksig.ApkVerifier;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

/** Optional local signing tool; no key or password is bundled. */
public final class SignApk {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: SignApk keystore alias input.apk output.apk");
        }
        String storeValue = System.getenv("SIGNING_STORE_PASSWORD");
        if (storeValue == null || storeValue.isEmpty()) {
            throw new IllegalStateException("SIGNING_STORE_PASSWORD is not set");
        }
        String keyValue = System.getenv("SIGNING_KEY_PASSWORD");
        char[] storePassword = storeValue.toCharArray();
        char[] keyPassword = (keyValue == null ? storeValue : keyValue).toCharArray();
        try {
            KeyStore ks = KeyStore.getInstance(new File(args[0]), storePassword);
            PrivateKey key = (PrivateKey) ks.getKey(args[1], keyPassword);
            List<X509Certificate> certs = new ArrayList<>();
            for (java.security.cert.Certificate c : ks.getCertificateChain(args[1])) {
                certs.add((X509Certificate) c);
            }
            ApkSigner.SignerConfig signer = new ApkSigner.SignerConfig.Builder(args[1], key, certs).build();
            new ApkSigner.Builder(Collections.singletonList(signer))
                .setInputApk(new File(args[2])).setOutputApk(new File(args[3]))
                .setMinSdkVersion(33).setV1SigningEnabled(true)
                .setV2SigningEnabled(true).setV3SigningEnabled(true).build().sign();
            ApkVerifier.Result result = new ApkVerifier.Builder(new File(args[3]))
                .setMinCheckedPlatformVersion(33).build().verify();
            System.out.println("Verified=" + result.isVerified());
            if (!result.isVerified()) throw new IllegalStateException("APK signature verification failed");
        } finally {
            Arrays.fill(storePassword, '\0');
            Arrays.fill(keyPassword, '\0');
        }
    }
}
