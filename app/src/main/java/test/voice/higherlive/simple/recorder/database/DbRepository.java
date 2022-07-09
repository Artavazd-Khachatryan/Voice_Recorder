package test.voice.higherlive.simple.recorder.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface DbRepository {

    @Query("SELECT* FROM Sound")
    Flowable<List<Sound>> getAll();

    @Insert
    void insert(Sound sound);

    @Delete
    void delete(Sound sound);
}
