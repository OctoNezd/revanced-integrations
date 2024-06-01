package app.revanced.integrations.syncforreddit;

import android.content.Context;

import com.laurencedawson.reddit_sync.ui.activities.WebViewActivity;

import app.revanced.integrations.shared.fixes.slink.BaseFixSLinksPatch;

/** @noinspection unused*/
public class FixSLinksPatch extends BaseFixSLinksPatch {
    private FixSLinksPatch() {
        webViewActivity = WebViewActivity.class;
    }

    public static boolean resolveSLink(Context context, String link) {
        return getInstance().resolve(context, link);
    }

    public static void staticSetAccessToken(String accessToken) {
        getInstance().setAccessToken(accessToken);
    }

    public static BaseFixSLinksPatch getInstance() {
        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
        return INSTANCE;
    }
}
