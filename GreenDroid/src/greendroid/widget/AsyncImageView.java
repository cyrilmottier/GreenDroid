package greendroid.widget;

import greendroid.image.ImageProcessor;
import greendroid.image.ImageRequest;
import greendroid.image.ImageRequest.ImageRequestCallback;
import greendroid.util.Config;
import greendroid.util.GDUtils;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.cyrilmottier.android.greendroid.R;

/**
 * <p>
 * A {@link AsyncImageView} is a network-aware {@link ImageView}. It may display
 * images from the web according to a URL. {@link AsyncImageView} takes care of
 * loading asynchronously images on the Internet. It also caches images in an
 * application-wide cache to prevent loading images several times.
 * </p>
 * <p>
 * Clients may listen the {@link OnImageViewLoadListener} to be notified of the
 * current image loading state.
 * </p>
 * <p>
 * {@link AsyncImageView} may be extremely useful in {@link ListView}'s row. To
 * prevent your {@link AsyncImageView} from downloading while scrolling or flinging
 * it is a good idea to pause it using {@link #setPaused(boolean)} method. Once
 * the scrolling/flinging is over, <em>un-pause</em> your {@link AsyncImageView}s
 * using <code>setPaused(false)</code>
 * </p>
 * 
 * @author Cyril Mottier
 */
public class AsyncImageView extends ImageView implements ImageRequestCallback {

    private static final String LOG_TAG = AsyncImageView.class.getSimpleName();

    /**
     * Clients may listen to {@link AsyncImageView}Êchanges using a
     * {@link OnImageViewLoadListener}.
     * 
     * @author Cyril Mottier
     */
    public static interface OnImageViewLoadListener {

        void onLoadingStarted(AsyncImageView imageView);

        void onLoadingEnded(AsyncImageView imageView, Bitmap image);

        void onLoadingFailed(AsyncImageView imageView, Throwable throwable);
    }
    
    private static final int IMAGE_SOURCE_UNKNOWN = -1;
    private static final int IMAGE_SOURCE_RESOURCE = 0;
    private static final int IMAGE_SOURCE_DRAWABLE = 1;
    private static final int IMAGE_SOURCE_BITMAP = 2;

    private int mImageSource;
    private Bitmap mDefaultBitmap;
    private Drawable mDefaultDrawable;
    private int mDefaultResId;

    private String mUrl;
    private ImageRequest mRequest;
    private boolean mPaused;

    private Bitmap mBitmap;
    private OnImageViewLoadListener mOnImageViewLoadListener;
    private ImageProcessor mImageProcessor;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initializeDefaultValues();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);

        setUrl(a.getString(R.styleable.AsyncImageView_url));

        Drawable d = a.getDrawable(R.styleable.AsyncImageView_defaultSrc);
        if (d != null) {
            setDefaultImageDrawable(d);
        }

        a.recycle();
    }

    private void initializeDefaultValues() {
        mImageSource = IMAGE_SOURCE_UNKNOWN;
        mPaused = false;
    }

    public boolean isLoading() {
        return mRequest != null;
    }

    public boolean isLoaded() {
        return mRequest == null && mBitmap != null;
    }

    public void setPaused(boolean paused) {
        if (mPaused != paused) {
            mPaused = paused;
            if (!paused) {
                reload();
            }
        }
    }
    
    public void reload() {
        reload(false);
    }

    public void reload(boolean force) {
        if (mRequest == null && mUrl != null) {

            // Prior downloading the image ... let's look in a cache !
            // TODO cyril: This is a synchronous call ... make it asynchronous
            mBitmap = null;
            if (!force) {
                mBitmap = GDUtils.getImageCache(getContext()).get(mUrl);
            }

            if (mBitmap != null) {
                setImageBitmap(mBitmap);
                return;
            }

            if (Config.GD_INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "Cache miss. Starting to load the image at the given URL");
            }

            setDefaultImage();
            mRequest = new ImageRequest(mUrl, this, mImageProcessor);
            mRequest.load(getContext());
        }
    }

    public void stopLoading() {
        if (mRequest != null) {
            mRequest.cancel();
            mRequest = null;
        }
    }

    public void setOnImageViewLoadListener(OnImageViewLoadListener listener) {
        mOnImageViewLoadListener = listener;
    }

    public void setUrl(String url) {

        // Check the url has changed
        if (mBitmap != null && url != null && url.equals(mUrl)) {
            return;
        }

        stopLoading();
        mUrl = url;

        // Setting the url to an empty string force the displayed image to the
        // default image
        if (mUrl == null || mUrl.length() == 0) {
            mBitmap = null;
            setDefaultImage();
        } else {
            if (!mPaused) {
                reload();
            } else {
                // We're paused: let's look in a synchronous and efficient cache
                // prior using the default image.
                mBitmap = GDUtils.getImageCache(getContext()).get(mUrl);
                if (mBitmap != null) {
                    setImageBitmap(mBitmap);
                    return;
                } else {
                    setDefaultImage();
                }
            }
        }
    }

    public void setDefaultImageBitmap(Bitmap bitmap) {
        mImageSource = IMAGE_SOURCE_BITMAP;
        mDefaultBitmap = bitmap;
        setDefaultImage();
    }

    public void setDefaultImageDrawable(Drawable drawable) {
        mImageSource = IMAGE_SOURCE_DRAWABLE;
        mDefaultDrawable = drawable;
        setDefaultImage();
    }

    public void setDefaultImageResource(int resId) {
        mImageSource = IMAGE_SOURCE_RESOURCE;
        mDefaultResId = resId;
        setDefaultImage();
    }

    public void setImageProcessor(ImageProcessor imageProcessor) {
        mImageProcessor = imageProcessor;
    }

    private void setDefaultImage() {
        if (mBitmap == null) {
            switch (mImageSource) {
                case IMAGE_SOURCE_BITMAP:
                    setImageBitmap(mDefaultBitmap);
                    break;
                case IMAGE_SOURCE_DRAWABLE:
                    setImageDrawable(mDefaultDrawable);
                    break;
                case IMAGE_SOURCE_RESOURCE:
                    setImageResource(mDefaultResId);
                    break;
                default:
                    setImageDrawable(null);
                    break;
            }
        }
    }

    static class SavedState extends BaseSavedState {
        String url;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            url = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(url);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.url = mUrl;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setUrl(ss.url);
    }

    public void onImageRequestStarted(ImageRequest request) {
        if (mOnImageViewLoadListener != null) {
            mOnImageViewLoadListener.onLoadingStarted(this);
        }
    }

    public void onImageRequestFailed(ImageRequest request, Throwable throwable) {
        mRequest = null;
        if (mOnImageViewLoadListener != null) {
            mOnImageViewLoadListener.onLoadingFailed(this, throwable);
        }
    }

    public void onImageRequestEnded(ImageRequest request, Bitmap image) {
        mBitmap = image;
        setImageBitmap(image);
        if (mOnImageViewLoadListener != null) {
            mOnImageViewLoadListener.onLoadingEnded(this, image);
        }
        mRequest = null;
    }

    public void onImageRequestCancelled(ImageRequest request) {
        mRequest = null;
        if (mOnImageViewLoadListener != null) {
            mOnImageViewLoadListener.onLoadingFailed(this, null);
        }
    }
}
