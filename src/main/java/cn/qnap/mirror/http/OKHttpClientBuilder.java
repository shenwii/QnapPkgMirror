package cn.qnap.mirror.http;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OKHttpClientBuilder {
    /**
     * 构建OkHttpClient.Builder对象
     * 
     * @param verifySsl 是否验证SSL证书链
     * @return OkHttpClient.Builder对象
     */
    public static OkHttpClient.Builder buildOKHttpClient(boolean verifySsl) {
        try {
            // 创建TrustManager数组以接受所有证书
            TrustManager[] trustAllCerts = buildTrustManagers();

            // 初始化SSL上下文
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 获取SSLSocketFactory
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // 创建OkHttpClient.Builder
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.dns(host -> {
                var inetAddresses = InetAddress.getAllByName(host);
                return Arrays.stream(inetAddresses).sorted(Comparator.comparingInt(obj -> (obj instanceof Inet6Address ? 0 : 1))).toList();
            });

            // 如果不需要验证SSL证书，设置hostnameVerifier为接受所有主机名
            if (!verifySsl)
                builder.hostnameVerifier((hostname, session) -> true);

            return builder;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            return new OkHttpClient.Builder();
        }
    }

    /**
     * 构建TrustManager数组以接受所有证书
     * 
     * @return 包含一个X509TrustManager对象的TrustManager数组
     */
    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
        };
    }
}
