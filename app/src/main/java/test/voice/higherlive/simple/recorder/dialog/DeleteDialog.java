package test.voice.higherlive.simple.recorder.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import test.voice.higherlive.simple.recorder.R;


public class DeleteDialog extends DialogFragment {

    Button btnOk;
    Button btnCancel;

    OkCallback okCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete, container, false);
        init(view);
        setListener();
        return view;
    }


    public void setOkCallback(OkCallback okCallback) {
        this.okCallback = okCallback;
    }

    private void init(View view) {
        btnOk = view.findViewById(R.id.btn_ok);
        btnCancel = view.findViewById(R.id.btn_cancele);
    }

    private void setListener() {

        btnOk.setOnClickListener(v -> {
            okCallback.ok();
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    public interface OkCallback {
        void ok();
    }
}
