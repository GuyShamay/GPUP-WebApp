package admin.util;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class HttpClientUtil {
    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void runAsyncWithBody(String finalUrl, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static void uploadFileAsync(String finalUrl, File selectedFile, Callback callback) {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", selectedFile.getName(),
                        RequestBody.create(selectedFile, MediaType.parse("text/plain")))
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    public static Response runSync(String finalUrl) throws IOException {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        return call.execute();
    }

    public static void shutdown() {
        System.out.println("Shutting down ADMIN CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
