package greendroid.app;

import greendroid.util.Config;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.cyrilmottier.android.greendroid.R;

/**
 * An equivalent to a PreferenceActivity that manages fancy preferences and an
 * {@link ActionBar}
 * 
 * @author Julien Dupouy
 */
public class GDPreferenceActivity extends PreferenceActivity implements ActionBarActivity {

    private static final String LOG_TAG = GDPreferenceActivity.class.getSimpleName();

    private ActionBarHost mActionBarHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void setContentView(int layoutResID) {
    	super.setContentView(createLayout());
    }

    public int createLayout() {
        return R.layout.gd_preference_list_content;
    }
    
	@Override
	public GDApplication getGDApplication() {
        return (GDApplication) getApplication();
	}

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        onPreContentChanged();
        onPostContentChanged();
    }
    
	@Override
	public void onPreContentChanged() {
        mActionBarHost = (ActionBarHost) findViewById(R.id.gd_action_bar_host);
        if (mActionBarHost == null) {
            throw new RuntimeException("Your content must have an ActionBarHost whose id attribute is R.id.gd_action_bar_host");
        }
        mActionBarHost.getActionBar().setOnActionBarListener(mActionBarListener);
	}

	@Override
	public void onPostContentChanged() {
        boolean titleSet = false;

        final Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(ActionBarActivity.GD_ACTION_BAR_TITLE);
            if (title != null) {
                titleSet = true;
                setTitle(title);
            }
        }

        if (!titleSet) {
            // No title has been set via the Intent. Let's look in the
            // ActivityInfo
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
                if (activityInfo.labelRes != 0) {
                    setTitle(activityInfo.labelRes);
                }
            } catch (NameNotFoundException e) {
                // Do nothing
            }
        }
	}

	@Override
	public ActionBar getGDActionBar() {
		return mActionBarHost.getActionBar();
	}

    @Override
    public void setTitle(CharSequence title) {
        getGDActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }
	
	public ActionBarItem addActionBarItem(ActionBarItem item) {
		return getGDActionBar().addItem(item);
	}
	
	public ActionBarItem addActionBarItem(ActionBarItem item, int itemId) {
		return getGDActionBar().addItem(item, itemId);
	}
	
	public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType) {
		return getGDActionBar().addItem(actionBarItemType);
	}
	
	public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType, int itemId) {
		return getGDActionBar().addItem(actionBarItemType, itemId);
	}

	@Override
	public FrameLayout getContentView() {
		return mActionBarHost.getContentView();
	}
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		return false;
	}

    private OnActionBarListener mActionBarListener = new OnActionBarListener() {
        public void onActionBarItemClicked(int position) {
            if (position == OnActionBarListener.HOME_ITEM) {

                final Class<?> klass = getGDApplication().getHomeActivityClass();
                if (klass != null && !klass.equals(GDPreferenceActivity.this.getClass())) {
                    if (Config.GD_INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "Going back to the home activity");
                    }
                    Intent homeIntent = new Intent(GDPreferenceActivity.this, klass);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }

            } else {
                if (!onHandleActionBarItemClick(getGDActionBar().getItem(position), position)) {
                    if (Config.GD_WARNING_LOGS_ENABLED) {
                        Log.w(LOG_TAG, "Click on item at position " + position + " dropped down to the floor");
                    }
                }
            }
        }
    };

}
