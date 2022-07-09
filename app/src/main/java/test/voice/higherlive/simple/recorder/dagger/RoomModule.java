package test.voice.higherlive.simple.recorder.dagger;

import android.app.Application;
import android.arch.persistence.room.Room;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import test.voice.higherlive.simple.recorder.database.DbRepository;
import test.voice.higherlive.simple.recorder.database.RecordDatabase;

@Module
public class RoomModule {

    private RecordDatabase recordDatabase;

    public RoomModule(Application application) {
        recordDatabase = Room.databaseBuilder(application, RecordDatabase.class, "sound_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    DbRepository providesRoomDatabase() {
        return recordDatabase.dbRequest();
    }
}
