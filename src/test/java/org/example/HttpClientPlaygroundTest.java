package org.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpClientPlaygroundTest {

    @Nested
    class MockWebServerTest {
        private MockWebServer mockWebServer;

        @BeforeEach
        void beforeEach() throws Exception {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        }

        @AfterEach
        void afterEach() throws Exception {
            // MockWebServer の停止
            mockWebServer.shutdown();
        }

        @Test
        void リクエストのタイムアウト値までにレスポンスがないとき_HttpTimeoutExceptionがスローされること() throws Exception {
            String mockServerUrl = mockWebServer.url("/test").toString();

            // モックレスポンスを定義
            mockWebServer.enqueue(new MockResponse()
                    .setBody("Hello, World!")
                    .setResponseCode(200)
                    .setHeadersDelay(3, SECONDS));
                    //.setBodyDelay(3, SECONDS));

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(mockServerUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(1))
                    .build();

            assertThrows(HttpTimeoutException.class, () -> httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        }
    }

    @Nested
    class MockServerTest {
        ClientAndServer mockServer;

        @BeforeEach
        public void startMockServer() {
            // 空いているポートでMockServerを起動
            mockServer = startClientAndServer(0);  // ポート0は自動で空きポートを使用
        }

        @AfterEach
        public void stopMockServer() {
            mockServer.stop();
        }

        @Test
        public void リクエストのタイムアウト値までにレスポンスがないとき_HttpTimeoutExceptionがスローされること() throws Exception {
            // MockServerが使用しているポート番号を取得
            int port = mockServer.getPort();

            mockServer.when(
                    request()
                            .withMethod("GET")
                            .withPath("/test")
            ).respond(
                    response()
                            .withStatusCode(200)
                            .withBody("Hello, World!")
                            .withDelay(new Delay(SECONDS, 3))
            );

            HttpClient httpClient = HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/test"))
                    .GET()
                    .timeout(Duration.ofSeconds(1))
                    .build();

            assertThrows(HttpTimeoutException.class, () -> httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        }
    }
}
