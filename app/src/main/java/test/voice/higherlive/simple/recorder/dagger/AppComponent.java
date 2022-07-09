package test.voice.higherlive.simple.recorder.dagger;

import javax.inject.Singleton;

import dagger.Component;
import test.voice.higherlive.simple.recorder.activity.MainActivity;
import test.voice.higherlive.simple.recorder.activity.RecordSaveActivity;
import test.voice.higherlive.simple.recorder.database.DbRepository;

@Singleton
@Component(modules = {AppModule.class, RoomModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(RecordSaveActivity activity);

    DbRepository dbRepository();
}
