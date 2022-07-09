package test.voice.higherlive.simple.recorder.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import test.voice.higherlive.simple.recorder.R;

import static test.voice.higherlive.simple.recorder.activity.RecordSaveActivity.SOUND_NAME_KAY;


@SuppressLint("ValidFragment")
public class SoundSetNameDialog extends DialogFragment {

    EditText etSoundName;
    Button btnOk;

    String soundName;

    SoundSaveListener soundSaveListener = (name) -> {};


    public SoundSetNameDialog(Bundle bundle) {
        soundName = bundle.getString(SOUND_NAME_KAY);
    }

    public static SoundSetNameDialog newInstance(Bundle bundle) {
        SoundSetNameDialog fragment = new SoundSetNameDialog(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_name, container, false);
        init(view);
        setListener();
        return view;
    }



    private void init(View view) {
        etSoundName = view.findViewById(R.id.et_sound_name);
        etSoundName.setText(soundName);
        btnOk = view.findViewById(R.id.btn_ok);
    }

    private void setListener() {
        btnOk.setOnClickListener(v -> {
            if(chack()){
                soundName = etSoundName.getText().toString().trim();
                soundSaveListener.save(soundName);
                dismiss();
            }
        });
    }

    private boolean chack() {
        String soundName = etSoundName.getText().toString().trim();
        if (soundName.isEmpty()){
            etSoundName.setError("Name is empty");
            return false;
        }

        return true;
    }

    public void setSoundSaveListener(SoundSaveListener soundSaveListener) {
        this.soundSaveListener = soundSaveListener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface SoundSaveListener{
        void save(String name);
    }

}
