package pl.huczeq.rtspplayer.ui.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;

public class DropDownListView extends MaterialAutoCompleteTextView {

    public DropDownListView(@NonNull Context context) {
        super(context);
        init();
    }

    public DropDownListView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public DropDownListView(@NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        if(selectedItem == null)
            return "";
        if(selectedItem instanceof Producer) {
            return ((Producer) selectedItem).getName();
        }else if(selectedItem instanceof Model) {
            return ((Model) selectedItem).getName();
        }
        return super.convertSelectionToString(selectedItem);
    }

    protected void init() { }
}
