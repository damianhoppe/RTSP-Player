package pl.huczeq.rtspplayer.ui.adapters.dropdown;

import android.content.Context;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.urltemplates.Model;
import pl.huczeq.rtspplayer.ui.adapters.base.BaseArrayAdapter;

public class ModelsDropdownAdapter extends BaseArrayAdapter<Model> {

    public ModelsDropdownAdapter(Context context) {
        super(context, false, R.layout.support_simple_spinner_dropdown_item, 0, R.string.none);
    }

    @Override
    protected Model buildNoneItem() {
        return null;
    }

    @Override
    public String itemToString(Model item) {
        return item.getName();
    }
}
