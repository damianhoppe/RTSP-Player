package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.DataManager;
import pl.huczeq.rtspplayer.data.objects.Camera;
import pl.huczeq.rtspplayer.data.threads.ImageLoadingThread;
import pl.huczeq.rtspplayer.interfaces.OnCameraChanged;
import pl.huczeq.rtspplayer.interfaces.OnListItemSelected;
import pl.huczeq.rtspplayer.interfaces.OnMenuItemSelected;

public class BackupsListAdapter extends ArrayAdapter<String> {

    private final String TAG = "BackupsListAdapter";

    private List<String> backups;
    private Context context;

    public BackupsListAdapter(@NonNull Context context, List<String> backups) {
        super(context, -1, backups);
        this.context = context;
        this.backups = backups;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.item_backup, parent, false);
        final TextView tvName = view.findViewById(R.id.tvName);
        final TextView tvDetails = view.findViewById(R.id.tvDetails);
        String name = backups.get(position);
        name = name.substring(0, name.indexOf("."));

        tvName.setText(name.substring(0, name.lastIndexOf('-')-1).trim());
        tvDetails.setText(name.substring(name.lastIndexOf('-')+1).trim());
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }

    @Override
    public String getItem(int i) {
        return this.backups.get(i);
    }

    @Override
    public int getCount() {
        return this.backups.size();
    }

    public void setList(List<String> list) {
        this.backups = list;
        notifyDataSetChanged();
    }

    public void remove(int index) {
        this.backups.remove(index);
        notifyDataSetChanged();
    }
}
