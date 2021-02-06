package pl.huczeq.rtspplayer.ui.views;

import android.view.View;
import android.widget.AdapterView;

public class OnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    boolean enable = true;
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(enable) onSelectedItem(adapterView, view, i, l);
        else enable = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        if(enable) onSelectedNothink(adapterView);
        else enable = true;
    }

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        this.enable = false;
    }

    public void onSelectedItem(AdapterView<?> adapterView, View view, int i, long l) {

    }

    public void onSelectedNothink(AdapterView<?> adapterView) {

    }
}
