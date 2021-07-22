package pl.huczeq.rtspplayer.ui.adapters.dropdown;

import android.content.Context;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.urltemplates.Producer;
import pl.huczeq.rtspplayer.ui.adapters.base.BaseArrayAdapter;

public class ProducersDropdownAdapter extends BaseArrayAdapter<Producer> {

    public ProducersDropdownAdapter(Context context) {
        super(context, true, R.layout.support_simple_spinner_dropdown_item, 0, R.string.none);
    }

    @Override
    protected Producer buildNoneItem() {
        return new Producer((String) null);
    }

    @Override
    public String itemToString(Producer item) {
        return item.getName();
    }
}
