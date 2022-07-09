package test.voice.higherlive.simple.recorder.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Sound.class}, version = 1)
public abstract class RecordDatabase extends RoomDatabase {
    public abstract DbRepository dbRequest();
}
