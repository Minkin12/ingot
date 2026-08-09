package dev.minkin.ingot;

import android.app.Application;

public class IngotApplication extends Application {
    public AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);

    }
}
