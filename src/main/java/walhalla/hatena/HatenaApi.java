package walhalla.hatena;

import com.github.scribejava.core.builder.api.DefaultApi10a;

/**
 * はてなブックマークのOAuth 1.0a API定義
 * 
 * @see <a href="https://developer.hatena.ne.jp/ja/documents/auth/apis/oauth/consumer">はてなOAuth</a>
 */
public class HatenaApi extends DefaultApi10a {

    private static class InstanceHolder {
        private static final HatenaApi INSTANCE = new HatenaApi();
    }

    public static HatenaApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://www.hatena.com/oauth/initiate?scope=write_public";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://www.hatena.com/oauth/token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return "https://www.hatena.ne.jp/oauth/authorize";
    }
}
