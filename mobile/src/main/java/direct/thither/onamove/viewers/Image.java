package direct.thither.onamove.viewers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;

import com.squareup.picasso.Picasso;

public class Image extends AppCompatImageView{
    private String src_url;
    private String query;

    private int default_h;
    private int default_w;

    public Image(Context context, String src, String qParam, int w, int h) {
        super(context);
        src_url = src;
        query = qParam;
        default_h = h;
        default_w = w;
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        String url = src_url;
        if(query!=null){
            url+=query+Integer.toString(default_w)+"x"+Integer.toString(default_h);
        }
        Picasso.get()
                .load(url)
                 .into(this);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
