package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.objects.urls.Producer;

public class ProducersSpinnerAdapter extends ArrayAdapter<Producer> {
    Context context;
    List<Producer> producerList;
    public static Producer emptyProducer = new Producer("none");

    public ProducersSpinnerAdapter(Context context, List<Producer> producerList) {
        super(context, R.layout.item_spinner_list, producerList);
        this.context = context;
        this.producerList = producerList;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        String label = "";
        if(i == 0) {
            label = context.getResources().getString(R.string.label_none_url_template);
        }else {
            label = this.producerList.get(i-1).getName();
        }
        TextView tv = (TextView) super.getDropDownView(i, view, viewGroup);
        tv.setText(label);
        return tv;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String label = "";
        if(i == 0) {
            label = context.getResources().getString(R.string.label_none_url_template);
        }else {
            label = this.producerList.get(i-1).getName();
        }
        TextView tv = (TextView) super.getView(i, view, viewGroup);
        tv.setText(label);
        tv.setPadding(0,0,0,0);
        tv.setTextColor(context.getColor(R.color.textColor));
        return tv;
    }

    @Override
    public int getCount() {
        return this.producerList.size()+1;
    }

    @Override
    public Producer getItem(int i) {
        if(i == 0) {
            return emptyProducer;
        }
        return this.producerList.get(i-1);
    }

    public void setProducersList(List<Producer> producerList, Spinner spinner) {
        this.producerList = producerList;
        notifyDataSetChanged();
    }
}
