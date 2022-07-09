package test.voice.higherlive.simple.recorder.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import test.voice.higherlive.simple.recorder.R;
import test.voice.higherlive.simple.recorder.adapter.holder.SoundViewHolder;
import test.voice.higherlive.simple.recorder.database.Sound;

public class SoundAdapter extends RecyclerView.Adapter<SoundViewHolder> {

    private List<Sound> soundList;
    private SoundViewHolder.ItemDeleteCalback itemDeleteCallback;

    private SoundItemClickListener soundItemClickListener = soundViewHolder -> {};


    public SoundAdapter(List<Sound> soundList){
        this.soundList = soundList;
    }

    public void setItemDeleteCallback(SoundViewHolder.ItemDeleteCalback itemDeleteCallback) {
        this.itemDeleteCallback = itemDeleteCallback;
    }

    public void setSoundList(List<Sound> soundList) {
        this.soundList = soundList;
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sound_list_item, viewGroup, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder soundViewHolder, int position) {
        soundViewHolder.setSound(soundList.get(position));
        soundViewHolder.setSoundItemClickListener(soundItemClickListener);
        soundViewHolder.setDeleteCallback(itemDeleteCallback);
        if (soundViewHolder.getAdapterPosition() == 0 && soundViewHolder.viewLine.getVisibility() != View.VISIBLE){
            soundViewHolder.viewLine.setVisibility(View.INVISIBLE);

        }

    }

    @Override
    public int getItemCount() {
        return soundList.size();
    }


    public void setSoundItemClickListener(SoundItemClickListener soundItemClickListener) {
        this.soundItemClickListener = soundItemClickListener;
    }

    public interface SoundItemClickListener{
        void onItemClick(SoundViewHolder soundViewHolder);
    }
}
