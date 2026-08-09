package dev.minkin.ingot;

import android.app.Application;

import java.io.IOException;

public class IngotApplication extends Application {
    public AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            container = new AppContainer(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
