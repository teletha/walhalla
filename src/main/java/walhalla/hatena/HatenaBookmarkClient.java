package walhalla.hatena;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import kiss.I;
import kiss.Signal;

/**
 * はてなブックマークOAuth認証クライアント
 */
public class HatenaBookmarkClient {

    private final OAuth10aService service;

    private OAuth1AccessToken accessToken;

    private final Path tokenCacheFile;

    /**
     * コンストラクタ
     * 
     */
    public HatenaBookmarkClient() {
        this("oob", getDefaultTokenCacheFile());
    }

    /**
     * コンストラクタ
     * 
     * @param callbackUrl コールバックURL（デスクトップアプリの場合は "oob"）
     */
    public HatenaBookmarkClient(String callbackUrl) {
        this(callbackUrl, getDefaultTokenCacheFile());
    }

    /**
     * コンストラクタ（トークンキャッシュファイル指定）
     * 
     * @param callbackUrl コールバックURL（デスクトップアプリの場合は "oob"）
     * @param tokenCacheFile トークンをキャッシュするファイルパス
     */
    public HatenaBookmarkClient(String callbackUrl, Path tokenCacheFile) {
        this.service = new ServiceBuilder(I.env("HatenaBookmarkConsumerKey")).apiSecret(I.env("HatenaBookmarkConsumerSecret"))
                .callback(callbackUrl)
                .build(HatenaApi.instance());
        this.tokenCacheFile = tokenCacheFile;

        // キャッシュされたトークンを自動的に読み込む
        loadTokenFromCache();
    }

    /**
     * デフォルトのトークンキャッシュファイルパスを取得
     */
    private static Path getDefaultTokenCacheFile() {
        return Path.of(".hatena");
    }

    /**
     * キャッシュされたトークンを読み込む
     */
    private void loadTokenFromCache() {
        if (tokenCacheFile != null && Files.exists(tokenCacheFile)) {
            try {
                String content = Files.readString(tokenCacheFile);
                String[] parts = content.split("\n");
                if (parts.length >= 2) {
                    String token = parts[0].trim();
                    String tokenSecret = parts[1].trim();
                    this.accessToken = new OAuth1AccessToken(token, tokenSecret);
                    System.out.println("キャッシュからトークンを読み込みました");
                }
            } catch (IOException e) {
                System.err.println("トークンキャッシュの読み込みに失敗しました: " + e.getMessage());
            }
        }
    }

    /**
     * トークンをキャッシュファイルに保存
     */
    private void saveTokenToCache() {
        if (tokenCacheFile != null && accessToken != null) {
            try {
                String content = accessToken.getToken() + "\n" + accessToken.getTokenSecret();
                Files.writeString(tokenCacheFile, content);
                System.out.println("トークンをキャッシュに保存しました: " + tokenCacheFile);
            } catch (IOException e) {
                System.err.println("トークンキャッシュの保存に失敗しました: " + e.getMessage());
            }
        }
    }

    /**
     * 認証が必要かどうかをチェック
     * 
     * @return 認証が必要な場合true
     */
    public boolean needsAuthorization() {
        return accessToken == null;
    }

    /**
     * OAuth認証フローを実行してアクセストークンを取得
     * 既にトークンがキャッシュされている場合はスキップ
     * 
     * @return アクセストークン
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public OAuth1AccessToken authorize() throws IOException, InterruptedException, ExecutionException {
        return authorize(false);
    }

    /**
     * OAuth認証フローを実行してアクセストークンを取得
     * 
     * @param forceReauth trueの場合、キャッシュを無視して強制的に再認証
     * @return アクセストークン
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public OAuth1AccessToken authorize(boolean forceReauth) throws IOException, InterruptedException, ExecutionException {
        // キャッシュされたトークンがあり、強制再認証でない場合はスキップ
        if (!forceReauth && accessToken != null) {
            System.out.println("既に認証済みです（キャッシュから読み込み済み）");
            return accessToken;
        }

        // Step 1: リクエストトークンを取得
        System.out.println("リクエストトークンを取得中...");
        System.out.println("エンドポイント: " + service.getApi().getRequestTokenEndpoint());
        final OAuth1RequestToken requestToken = service.getRequestToken();
        System.out.println("リクエストトークン取得成功");

        // Step 2: ユーザーに認証URLを表示
        final String authorizationUrl = service.getAuthorizationUrl(requestToken);
        System.out.println("以下のURLにアクセスして認証してください:");
        System.out.println(authorizationUrl);
        System.out.println();
        System.out.print("認証後に表示されるPINコードを入力してください: ");

        // Step 3: PINコードを取得
        final Scanner scanner = new Scanner(System.in);
        final String oauthVerifier = scanner.nextLine();

        // Step 4: アクセストークンを取得
        System.out.println("アクセストークンを取得中...");
        this.accessToken = service.getAccessToken(requestToken, oauthVerifier);

        System.out.println("認証成功！");

        // トークンをキャッシュに保存
        saveTokenToCache();

        return accessToken;
    }

    /**
     * キャッシュされたトークンをクリア
     */
    public void clearTokenCache() {
        this.accessToken = null;
        if (tokenCacheFile != null && Files.exists(tokenCacheFile)) {
            try {
                Files.delete(tokenCacheFile);
                System.out.println("トークンキャッシュを削除しました");
            } catch (IOException e) {
                System.err.println("トークンキャッシュの削除に失敗しました: " + e.getMessage());
            }
        }
    }

    /**
     * 既存のアクセストークンを設定
     * 
     * @param token アクセストークン
     * @param tokenSecret アクセストークンシークレット
     */
    public void setAccessToken(String token, String tokenSecret) {
        this.accessToken = new OAuth1AccessToken(token, tokenSecret);
    }

    /**
     * はてなブックマークAPIにGETリクエストを送信
     * 
     * @param url リクエストURL
     * @return レスポンス
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Response get(String url) throws IOException, InterruptedException, ExecutionException {
        if (accessToken == null) {
            throw new IllegalStateException("アクセストークンが設定されていません。先にauthorize()を実行してください。");
        }

        final OAuthRequest request = new OAuthRequest(Verb.GET, url);
        service.signRequest(accessToken, request);
        Response response = service.execute(request);

        // 401エラーの場合、詳細情報を出力
        if (response.getCode() == 401) {
            System.err.println("401 Unauthorized - トークンが無効または期限切れの可能性があります");
            System.err.println("Response body: " + response.getBody());
            System.err.println("トークンを再取得するには、clearTokenCache()を実行してから再度authorize()を呼び出してください");
        }

        return response;
    }

    /**
     * はてなブックマークAPIにPOSTリクエストを送信
     * 
     * @param url リクエストURL
     * @param body リクエストボディ
     * @return レスポンス
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Response post(String url, String body) throws IOException, InterruptedException, ExecutionException {
        if (accessToken == null) {
            throw new IllegalStateException("アクセストークンが設定されていません。先にauthorize()を実行してください。");
        }

        final OAuthRequest request = new OAuthRequest(Verb.POST, url);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setPayload(body);
        service.signRequest(accessToken, request);
        return service.execute(request);
    }

    /**
     * ブックマークを投稿する
     * 
     * @param targetUrl ブックマークするURL
     * @param comment コメント（省略可）
     * @param tags タグ（カンマ区切り、省略可）
     * @return レスポンス
     */
    public Signal<Response> postBookmark(String targetUrl, String comment, String... tags) {
        if (accessToken == null) {
            throw new IllegalStateException("アクセストークンが設定されていません。先にauthorize()を実行してください。");
        }

        final OAuthRequest request = new OAuthRequest(Verb.POST, "https://bookmark.hatenaapis.com/rest/1/my/bookmark");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        // addBodyParameterではなく、addQuerystringParameterを使用する
        // これにより、パラメータがリクエストボディに "key=value" 形式で追加され、
        // かつOAuthの署名ベース文字列にも含まれる。
        // ScribeJavaの内部実装では、POSTの場合addQuerystringParameterもボディに入る。
        // もしボディに入らない場合は addBodyParameter を使う必要があるが、その場合は
        // 値をエンコード済みで渡すことが重要。
        request.addQuerystringParameter("url", targetUrl);

        if (comment != null && !comment.isEmpty()) {
            request.addQuerystringParameter("comment", comment);
        }

        if (tags != null) {
            for (String tag : tags) {
                request.addQuerystringParameter("tags", tag.trim());
            }
        }

        service.signRequest(accessToken, request);

        return new Signal<Response>((observer, disposer) -> {
            Future<Response> future = service.executeAsync(request);
            try {
                Response response = future.get();
                observer.accept(response);
                observer.complete();
            } catch (Exception e) {
                observer.error(e);
            }
            return disposer.add(future);
        });
    }

    /**
     * ブックマークを削除する
     * 
     * @param targetUrl 削除するブックマークのURL
     * @return レスポンス
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Response deleteBookmark(String targetUrl) throws IOException, InterruptedException, ExecutionException {
        if (accessToken == null) {
            throw new IllegalStateException("アクセストークンが設定されていません。先にauthorize()を実行してください。");
        }

        String url = "https://bookmark.hatenaapis.com/rest/1/my/bookmark?url=" + urlEncode(targetUrl);
        final OAuthRequest request = new OAuthRequest(Verb.DELETE, url);
        service.signRequest(accessToken, request);
        return service.execute(request);
    }

    /**
     * 自分のブックマーク情報を取得する
     * 
     * @param targetUrl 取得するブックマークのURL
     * @return レスポンス
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Response getBookmark(String targetUrl) throws IOException, InterruptedException, ExecutionException {
        String url = "https://bookmark.hatenaapis.com/rest/1/my/bookmark?url=" + urlEncode(targetUrl);
        return get(url);
    }

    /**
     * URLエンコード
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * サービスをクローズ
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (service != null) {
            service.close();
        }
    }
}
