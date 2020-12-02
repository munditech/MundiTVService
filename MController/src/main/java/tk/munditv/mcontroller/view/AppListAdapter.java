package tk.munditv.mcontroller.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tk.munditv.libtvservice.util.PInfo;
import tk.munditv.libtvservice.dmc.DMCControl;
import tk.munditv.mcontroller.R;
import tk.munditv.mcontroller.app.MainApplication;

public class AppListAdapter extends RecyclerView.Adapter {

    private final static String TAG = AppListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<PInfo> mList;

    public AppListAdapter(Context context, ArrayList<PInfo> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applist_view, null);
        return new AppListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PInfo pInfo = mList.get(position);
        AppListHolder mHolder = (AppListHolder) holder;

        mHolder.getView().setTag(pInfo);
        mHolder.getText().setText(pInfo.getAppname());
        mHolder.getImage().setImageDrawable(drawIcon(pInfo.getIcon()));
        mHolder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PInfo info = (PInfo) v.getTag();
                DMCControl dmcControl = new DMCControl(null,
                        3, MainApplication.dmrDeviceItem,
                        MainApplication.upnpService,
                        null, null, null);
                String name = "[COMMAND]" + info.getAppname();
                dmcControl.setCommand(name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private Drawable drawIcon(byte[] icon) {
        Bitmap bitmap = bytes2Bitmap(icon);
        return bitmap2Drawable(bitmap);
    }

    private static Bitmap bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    private static Drawable bitmap2Drawable(Bitmap bitmap) {
        @SuppressWarnings("deprecation")
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        Drawable d = (Drawable) bd;
        return d;
    }

    private class AppListHolder extends RecyclerView.ViewHolder{

        private View mView;
        private ImageView mImage;
        private TextView mText;

        public AppListHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mImage = itemView.findViewById(R.id.app_icon);
            mText = itemView.findViewById(R.id.app_name);
        }

        public ImageView getImage() {
            return mImage;
        }

        public TextView getText() {
            return mText;
        }

        public View getView() {
            return mView;
        }

    };
}
