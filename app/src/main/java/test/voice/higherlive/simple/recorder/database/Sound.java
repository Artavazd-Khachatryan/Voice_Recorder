package test.voice.higherlive.simple.recorder.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Sound {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String path;

    public String name;

    public long time;

    public long duration;

}
