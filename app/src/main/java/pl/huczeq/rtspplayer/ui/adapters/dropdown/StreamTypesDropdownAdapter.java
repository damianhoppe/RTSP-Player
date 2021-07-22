package pl.huczeq.rtspplayer.ui.adapters.dropdown;

import android.content.Context;
import android.content.res.Resources;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.model.urltemplates.UrlTemplate;
import pl.huczeq.rtspplayer.ui.adapters.base.BaseArrayAdapter;

public class StreamTypesDropdownAdapter extends BaseArrayAdapter<Integer> {

    private final Resources resources;

    public StreamTypesDropdownAdapter(Context context) {
        super(context, R.layout.support_simple_spinner_dropdown_item, List.of(UrlTemplate.StreamType.MAIN_STREAM, UrlTemplate.StreamType.SUB_STREAM));
        this.resources = context.getResources();
    }

    @Override
    public String itemToString(Integer item) {
        return item == 0? resources.getString(R.string.stream_type_label_main_stream) : resources.getString(R.string.stream_type_label_sub_stream);
    }

    @Override
    protected Integer buildNoneItem() {
        return null;
    }
}
