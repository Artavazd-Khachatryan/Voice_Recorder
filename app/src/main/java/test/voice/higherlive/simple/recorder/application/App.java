package test.voice.higherlive.simple.recorder.application;

import android.app.Application;

import test.voice.higherlive.simple.recorder.dagger.AppComponent;
import test.voice.higherlive.simple.recorder.dagger.AppModule;
import test.voice.higherlive.simple.recorder.dagger.DaggerAppComponent;
import test.voice.higherlive.simple.recorder.dagger.RoomModule;


public class App extends Application {

    private static App INSTANCE;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        createAppComponent();
    }

    private void createAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .roomModule(new RoomModule(this))
                .build();
    }

    public static App getINSTANCE() {
        return INSTANCE;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
