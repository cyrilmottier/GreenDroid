package greendroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;

/**
 * <p>
 * Combines the animations of ImageSwitcher with the asynchronous loading of
 * {@link AsyncImageView}.  Expects to have a  <code>ViewSwitcher.ViewFactory</code>
 * which returns an {@link AsyncImageView} from it's {@code makeView} function:
 * </p>
 * <p>
 * 	<code>
 * 	public View makeView() {<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; AsyncImageView asyncImage = new AsyncImageView(mContext);<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; asyncImage.setBackgroundColor(0xFF000000);<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; asyncImage.setScaleType(ImageView.ScaleType.CENTER_CROP);<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; asyncImage.setLayoutParams(new ImageSwitcher.LayoutParams(300,250));<br/>
        &nbsp;&nbsp;&nbsp;&nbsp; return asyncImage;<br/>
	}<br/>
 * 	</code>
 * </p>
 * 
 * 
 * <p>
 * When registered as as an
 * <code>AdapterView.OnItemSelectedListener, the client can conveniently
 *  use {@link #setNextAsyncImageViewUrl(String)} to change the URL of the next {@link AsyncImageView} 
 *  that will be displayed:
 * </p>
 * 
 * <p>
 * <code>
 *		public void onItemSelected(AdapterView parent, View v, int position, long id) {<br/>
 *			&nbsp;&nbsp;&nbsp;&nbsp; mAsyncImageSwitcher.setNextAsyncImageViewUrl(imageUrls[position]);<br/>
 *			&nbsp;&nbsp;&nbsp;&nbsp; mAsyncImageSwitcher.showNext();<br/>
 *		}<br/>
 * </code>
 * </p>
 * 
 * 
 */
public class AsyncImageSwitcher extends ImageSwitcher {

	public AsyncImageSwitcher(Context context) {
		super(context);

	}

	public AsyncImageSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Sets the URL of the next {@link AsyncImageView}
	 * 
	 * @param url
	 *            The url of the image to set.
	 */
	public void setNextAsyncImageViewUrl(String url) {
		AsyncImageView image = (AsyncImageView) this.getNextView();
		image.setUrl(url);
	}

}
