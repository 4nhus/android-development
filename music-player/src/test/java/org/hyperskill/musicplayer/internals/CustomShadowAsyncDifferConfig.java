package org.hyperskill.musicplayer.internals;

import android.os.Handler;
import android.os.Looper;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.concurrent.Executor;

import androidx.recyclerview.widget.AsyncDifferConfig;

// version 2.0
@Implements(AsyncDifferConfig.class)
@SuppressWarnings({"unused"})
public class CustomShadowAsyncDifferConfig {

    Executor mainExecutor;

    @Implementation
    public Executor getBackgroundThreadExecutor() {
        if (mainExecutor == null) {
            mainExecutor = new MainThreadExecutor();
        }
        return mainExecutor;
    }

    public static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable r) {
            handler.post(r);
        }
    }
}
