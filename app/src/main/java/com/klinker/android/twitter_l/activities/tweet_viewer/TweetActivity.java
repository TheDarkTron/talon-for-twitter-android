package com.klinker.android.twitter_l.activities.tweet_viewer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.peekview.PeekViewActivity;
import com.klinker.android.twitter_l.R;
import com.klinker.android.twitter_l.activities.media_viewer.VideoViewerActivity;
import com.klinker.android.twitter_l.activities.media_viewer.image.ImageViewerActivity;
import com.klinker.android.twitter_l.activities.media_viewer.image.TimeoutThread;
import com.klinker.android.twitter_l.activities.profile_viewer.ProfilePager;
import com.klinker.android.twitter_l.data.sq_lite.HomeSQLiteHelper;
import com.klinker.android.twitter_l.listeners.MultipleImageTouchListener;
import com.klinker.android.twitter_l.settings.AppSettings;
import com.klinker.android.twitter_l.utils.ExpansionViewHelper;
import com.klinker.android.twitter_l.utils.NotificationUtils;
import com.klinker.android.twitter_l.utils.ReplyUtils;
import com.klinker.android.twitter_l.utils.TweetLinkUtils;
import com.klinker.android.twitter_l.utils.Utils;
import com.klinker.android.twitter_l.utils.VideoMatcherUtil;
import com.klinker.android.twitter_l.utils.text.TextUtils;
import com.klinker.android.twitter_l.views.TweetView;
import com.klinker.android.twitter_l.views.badges.GifBadge;
import com.klinker.android.twitter_l.views.badges.VideoBadge;
import com.klinker.android.twitter_l.views.widgets.text.FontPrefTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import de.tubs.cs.ibr.eventchain_android.AssessReceiver;
import de.tubs.cs.ibr.eventchain_android.EventCC;
import de.tubs.cs.ibr.eventchain_android.JsonObjectReceiver;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import xyz.klinker.android.drag_dismiss.DragDismissIntentBuilder;
import xyz.klinker.android.drag_dismiss.delegate.DragDismissDelegate;

public class TweetActivity extends PeekViewActivity implements DragDismissDelegate.Callback, JsonObjectReceiver, View.OnClickListener, AssessReceiver {

    public static Intent getIntent(Context context, Cursor cursor) {
        return getIntent(context, cursor, false);
    }

    public static Intent getIntent(Context context, Cursor cursor, boolean isSecondAccount) {
        String screenname = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_SCREEN_NAME));
        String name = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_NAME));
        String text = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TEXT));
        long time = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TIME));
        String picUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PIC_URL));
        String otherUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_URL));
        String users = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_USERS));
        String hashtags = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_HASHTAGS));
        long id = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_TWEET_ID));
        String profilePic = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_PRO_PIC));
        String otherUrls = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_URL));
        String gifUrl = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_ANIMATED_GIF));
        long videoDuration = cursor.getLong(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_MEDIA_LENGTH));
        String retweeter;
        try {
            retweeter = cursor.getString(cursor.getColumnIndex(HomeSQLiteHelper.COLUMN_RETWEETER));
        } catch (Exception e) {
            retweeter = "";
        }
        String link = "";

        boolean hasGif = gifUrl != null && !gifUrl.isEmpty();
        boolean displayPic = !picUrl.equals("") && !picUrl.contains("youtube");
        if (displayPic) {
            link = picUrl;
        } else {
            link = otherUrls.split("  ")[0];
        }

        Log.v("tweet_page", "clicked");
        Intent viewTweet = new Intent(context, TweetActivity.class);
        viewTweet.putExtra("name", name);
        viewTweet.putExtra("screenname", screenname);
        viewTweet.putExtra("time", time);
        viewTweet.putExtra("tweet", text);
        viewTweet.putExtra("retweeter", retweeter);
        viewTweet.putExtra("webpage", link);
        viewTweet.putExtra("other_links", otherUrl);
        viewTweet.putExtra("picture", displayPic);
        viewTweet.putExtra("tweetid", id);
        viewTweet.putExtra("proPic", profilePic);
        viewTweet.putExtra("users", users);
        viewTweet.putExtra("hashtags", hashtags);
        viewTweet.putExtra("animated_gif", gifUrl);
        viewTweet.putExtra("second_account", isSecondAccount);
        viewTweet.putExtra("video_duration", videoDuration);

        applyDragDismissBundle(context, viewTweet);

        return viewTweet;
    }

    public static void applyDragDismissBundle(Context context, Intent intent) {

        DragDismissIntentBuilder.Theme theme = DragDismissIntentBuilder.Theme.LIGHT;
        AppSettings settings = AppSettings.getInstance(context);

        if (settings.blackTheme) {
            theme = DragDismissIntentBuilder.Theme.BLACK;
        } else if (settings.darkTheme) {
            theme = DragDismissIntentBuilder.Theme.DARK;
        }

        new DragDismissIntentBuilder(context)
                .setPrimaryColorValue(settings.blackTheme ? Color.BLACK : Color.TRANSPARENT)
                .setDragElasticity(DragDismissIntentBuilder.DragElasticity.XLARGE)
                .setShowToolbar(false)
                .setTheme(theme)
                .build(intent);
    }

    private static final long NETWORK_ACTION_DELAY = 200;

    public static final String USE_EXPANSION = "use_expansion";
    public static final String EXPANSION_DIMEN_LEFT_OFFSET = "left_offset";
    public static final String EXPANSION_DIMEN_TOP_OFFSET = "top_offset";
    public static final String EXPANSION_DIMEN_WIDTH = "view_width";
    public static final String EXPANSION_DIMEN_HEIGHT = "view_height";

    public Context context;
    public AppSettings settings;
    public SharedPreferences sharedPrefs;

    public String name;
    public String screenName;
    public String tweet;
    public long time;
    public String retweeter;
    public String webpage;
    public String proPic;
    public boolean picture;
    public long tweetId;
    public String[] users;
    public String[] hashtags;
    public String[] otherLinks;
    public String linkString;
    public boolean isMyTweet = false;
    public boolean isMyRetweet = true;
    public boolean secondAcc = false;
    public String gifVideo;
    public boolean isAConversation = false;
    public long videoDuration = -1;
    private String medias;

    protected boolean fromLauncher = false;
    protected boolean fromNotification = false;

    private boolean sharedTransition = false;

    private String TAG = "TweetActivity";
    private String eventId;
    EventCC eventCC;
    private boolean boundService = false; // only send transactions to the EventCC if this flag is true
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to EventCC, cast the IBinder and get LocalService instance
            EventCC.LocalBinder binder = (EventCC.LocalBinder) service;
            eventCC = binder.getService();
            boundService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundService = false;
        }
    };

    private Date startTime;

    @Override
    protected void onStart() {
        // start timer
        startTime = new Date();
        super.onStart();
        Intent intent = new Intent(this, EventCC.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        loadMediaEntities();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.setSharedContentTransition(this);
        super.onCreate(savedInstanceState);

        DragDismissDelegate delegate = new DragDismissDelegate(this, this);
        delegate.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_slide_up, 0);
    }

    @Override
    public View onCreateContent(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Utils.setTaskDescription(this);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        int notificationId = getIntent().getIntExtra("notification_id", -1);
        if (notificationId != -1) {
            notificationManager.cancel(notificationId);
            NotificationUtils.cancelGroupedNotificationWithNoContent(this);
            fromNotification = true;
        }

        context = this;
        settings = AppSettings.getInstance(this);
        sharedPrefs = AppSettings.getSharedPreferences(context);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if (getIntent().getBooleanExtra("share_trans", false)) {
            sharedTransition = false;
        }

        getFromIntent();

        ArrayList<String> webpages = new ArrayList<String>();

        if (otherLinks == null) {
            otherLinks = new String[0];
        }

        if (gifVideo == null) {
            gifVideo = "no gif surfaceView";
        }
        boolean hasWebpage;
        boolean youtube = false;
        if (otherLinks.length > 0 && !otherLinks[0].equals("")) {
            for (String s : otherLinks) {
                if (s.contains("youtu") &&
                        !(s.contains("channel") || s.contains("user") || s.contains("playlist"))) {
                    youtubeVideo = s;
                    youtube = true;
                    break;
                } else {
                    if (!s.contains("pic.twitt")) {
                        webpages.add(s);
                    }
                }
            }

            if (webpages.size() >= 1) {
                hasWebpage = true;
            } else {
                hasWebpage = false;
            }

        } else {
            hasWebpage = false;
        }

        if (hasWebpage && webpages.size() == 1) {
            String web = webpages.get(0);
            if (web.contains(tweetId + "/photo/1") ||
                    VideoMatcherUtil.containsThirdPartyVideo(web)) {
                hasWebpage = false;
                gifVideo = webpages.get(0);
            }
        }

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        Utils.setUpTweetTheme(context, settings);

        View root = inflater.inflate(R.layout.tweet_activity_new, parent, false);

        if (youtube ||
                (null != gifVideo && !android.text.TextUtils.isEmpty(gifVideo) &&
                        (gifVideo.contains(".mp4") || gifVideo.contains(".m3u8") ||
                                gifVideo.contains("/photo/1") ||
                                VideoMatcherUtil.containsThirdPartyVideo(gifVideo)))) {
            displayPlayButton = true;
        }

        root.findViewById(R.id.line).setBackgroundColor(settings.themeColors.accentColor);
        setUIElements(root);

        String page = webpages.size() > 0 ? webpages.get(0) : "";
        String embedded = page;

        for (int i = 0; i < webpages.size(); i++) {
            if (TweetView.isEmbeddedTweet(webpages.get(i))) {
                embedded = webpages.get(i);
                break;
            }
        }

        if (hasWebpage && TweetView.isEmbeddedTweet(tweet)) {
            final CardView view = (CardView) root.findViewById(R.id.embedded_tweet_card);

            final long embeddedId = TweetLinkUtils.getTweetIdFromLink(embedded);

            if (embeddedId != 0l) {
                view.setVisibility(View.INVISIBLE);
                new TimeoutThread(new Runnable() {
                    @Override
                    public void run() {
                        Twitter twitter = getTwitter();

                        try {
                            Thread.sleep(NETWORK_ACTION_DELAY);
                        } catch (Exception e) {
                        }

                        try {
                            final Status s = twitter.showStatus(embeddedId);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TweetView v = new TweetView(context, s);
                                    v.setCurrentUser(settings.myScreenName);
                                    v.setSmallImage(true);

                                    view.removeAllViews();
                                    view.addView(v.getView());
                                    view.setVisibility(View.VISIBLE);
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        }

        return root;
    }

    boolean displayPlayButton = false;
    public VideoView video;
    public boolean videoError = false;

    String youtubeVideo = "";

    @Override
    public void onBackPressed() {
        if (!hidePopups()) {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        // this is used in the onStart() for the home fragment to tell whether or not it should refresh
        // tweetmarker. Since coming out of this will only call onResume(), it isn't needed.
        //sharedPrefs.edit().putBoolean("from_activity", true).apply();

        if (expansionHelper != null) {
            expansionHelper.stop();
        }

        super.finish();
        overridePendingTransition(0, R.anim.activity_slide_down);
    }

    public boolean hidePopups() {
        if (expansionHelper != null && expansionHelper.hidePopups()) {
            return true;
        }

        return false;
    }

    public void getFromIntent() {
        Intent from = getIntent();

        name = from.getStringExtra("name");
        screenName = from.getStringExtra("screenname");
        tweet = from.getStringExtra("tweet");
        time = from.getLongExtra("time", 0);
        retweeter = from.getStringExtra("retweeter");
        webpage = from.getStringExtra("webpage");
        tweetId = from.getLongExtra("tweetid", 0);
        picture = from.getBooleanExtra("picture", false);
        proPic = from.getStringExtra("proPic");
        secondAcc = from.getBooleanExtra("second_account", false);
        gifVideo = from.getStringExtra("animated_gif");
        isAConversation = from.getBooleanExtra("conversation", false);
        videoDuration = from.getLongExtra("video_duration", -1);

        try {
            users = from.getStringExtra("users").split("  ");
        } catch (Exception e) {
            users = null;
        }

        try {
            hashtags = from.getStringExtra("hashtags").split("  ");
        } catch (Exception e) {
            hashtags = null;
        }

        try {
            linkString = from.getStringExtra("other_links");
            otherLinks = linkString.split("  ");
        } catch (Exception e) {
            otherLinks = null;
        }

        if (screenName != null) {
            if (screenName.equals(settings.myScreenName)) {
                isMyTweet = true;
            } else if (screenName.equals(retweeter)) {
                isMyRetweet = true;
            }
        }
    }

    public Twitter getTwitter() {
        if (secondAcc) {
            return Utils.getSecondTwitter(this);
        } else {
            return Utils.getTwitter(this, settings);
        }
    }

    private void setTransitionNames() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            profilePic.setTransitionName("pro_pic");
            nametv.setTransitionName("name");
            screennametv.setTransitionName("screen_name");
            tweettv.setTransitionName("tweet");
            image.setTransitionName("image");
        }
    }

    public CircleImageView profilePic;
    public ImageView image;
    public FontPrefTextView retweetertv;
    public FontPrefTextView repliesTv;
    public FontPrefTextView timetv;
    public FontPrefTextView nametv;
    public FontPrefTextView screennametv;
    public FontPrefTextView tweettv;
    public FontPrefTextView eventCCtv;
    public Button plus;
    public Button minus;

    boolean replace;
    boolean embeddedTweetFound;

    public void setUIElements(final View layout) {

        //findViewById(R.id.content_container).setPadding(0,0,0,0);

        nametv = (FontPrefTextView) layout.findViewById(R.id.name);
        screennametv = (FontPrefTextView) layout.findViewById(R.id.screen_name);
        tweettv = (FontPrefTextView) layout.findViewById(R.id.tweet);
        retweetertv = (FontPrefTextView) layout.findViewById(R.id.retweeter);
        repliesTv = (FontPrefTextView) layout.findViewById(R.id.reply_to);
        profilePic = (CircleImageView) layout.findViewById(R.id.profile_pic);
        image = (ImageView) layout.findViewById(R.id.image);
        timetv = (FontPrefTextView) layout.findViewById(R.id.time);
        eventCCtv = (FontPrefTextView) layout.findViewById(R.id.trust);
        plus = layout.findViewById(R.id.bt_asses_positive);
        minus = layout.findViewById(R.id.bt_assess_negative);

        // button onClick listeners for assessment
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);

        tweettv.setTextSize(settings.textSize);
        screennametv.setTextSize(settings.textSize - 2);
        nametv.setTextSize(settings.textSize + 4);
        timetv.setTextSize(settings.textSize - 3);
        retweetertv.setTextSize(settings.textSize - 3);
        repliesTv.setTextSize(settings.textSize - 2);
        eventCCtv.setTextSize(settings.textSize - 3);

        View.OnClickListener viewPro = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hidePopups()) {
                    ProfilePager.start(context, name, screenName, proPic);
                }
            }
        };

        glide(proPic, profilePic);
        profilePic.setOnClickListener(viewPro);

        layout.findViewById(R.id.person_info).setOnClickListener(viewPro);
        nametv.setOnClickListener(viewPro);
        screennametv.setOnClickListener(viewPro);

        final String replies = ReplyUtils.getReplyingToHandles(tweet);
        if (isAConversation && settings.compressReplies && replies != null && !replies.isEmpty()) {
            tweet = tweet.replace(replies, "");

            final String replyToText = context.getString(R.string.reply_to);
            final String othersText = context.getString(R.string.others);

            if (ReplyUtils.showMultipleReplyNames(replies)) {
                repliesTv.setText(replyToText + " " + replies);
                TextUtils.linkifyText(context, repliesTv, findViewById(R.id.tweet_background), true, "", false);
            } else {
                final String firstPerson = replies.split(" ")[0];
                repliesTv.setText(replyToText + " " + firstPerson + " & " + othersText);

                Link others = new Link(othersText)
                        .setUnderlined(false)
                        .setTextColor(settings.themeColors.accentColor)
                        .setOnClickListener(new Link.OnClickListener() {
                            @Override
                            public void onClick(String clickedText) {
                                final String[] repliesSplit = replies.split(" ");
                                new AlertDialog.Builder(context).setItems(replies.split(" "), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ProfilePager.start(context, repliesSplit[which]);
                                    }
                                }).show();
                            }
                        });
                Link first = new Link(firstPerson)
                        .setUnderlined(false)
                        .setTextColor(settings.themeColors.accentColor)
                        .setOnClickListener(new Link.OnClickListener() {
                            @Override
                            public void onClick(String clickedText) {
                                ProfilePager.start(context, firstPerson);
                            }
                        });

                LinkBuilder.on(repliesTv).addLink(others).addLink(first).build();
            }

            repliesTv.setVisibility(View.VISIBLE);
        }

        if (picture || displayPlayButton) { // if there is a picture already loaded (or we have a vine/twimg surfaceView)

            if (displayPlayButton && VideoMatcherUtil.containsThirdPartyVideo(gifVideo)) {
                image.setBackgroundResource(android.R.color.black);
            } else {
                glide(webpage, image);
            }

            if (displayPlayButton) {
                layout.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
                if (gifVideo != null && VideoMatcherUtil.isTwitterGifLink(gifVideo)) {
                    ((ImageView) layout.findViewById(R.id.play_button)).setImageDrawable(new GifBadge(this));
                } else {
                    ((ImageView) layout.findViewById(R.id.play_button)).setImageDrawable(new VideoBadge(this, videoDuration));
                }
            }

            MultipleImageTouchListener listener = new MultipleImageTouchListener(webpage);
            image.setOnTouchListener(listener);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!hidePopups()) {
                        if (displayPlayButton) {
                            String links = "";
                            for (String s : otherLinks) {
                                links += s + "  ";
                            }

                            VideoViewerActivity.startActivity(context, tweetId, gifVideo, links);
                        } else {
                            ImageViewerActivity.Companion.startActivity(context, tweetId, image, listener.getImageTouchPosition(), webpage.split(" "));
                        }
                    }
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.setClipToOutline(true);
            }

            ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
            layoutParams.height = (int) getResources().getDimension(R.dimen.header_condensed_height);
            image.setLayoutParams(layoutParams);

        } else {
            // remove the picture
            image.setVisibility(View.GONE);
        }

        nametv.setText(name);
        screennametv.setText("@" + screenName);

        replace = false;
        embeddedTweetFound = TweetView.isEmbeddedTweet(tweet);

        if (settings.inlinePics && (tweet.contains("pic.twitter.com/") || embeddedTweetFound)) {
            if (tweet.lastIndexOf(".") == tweet.length() - 1) {
                replace = true;
            }
        }

        try {
            tweettv.setText(replace ?
                    tweet.substring(0, tweet.length() - (embeddedTweetFound ? 33 : 25)) :
                    tweet);
        } catch (Exception e) {
            tweettv.setText(tweet);
        }
        tweettv.setTextIsSelectable(true);

        //Date tweetDate = new Date(time);
        setTime(time);

        if (retweeter != null && retweeter.length() > 0) {
            retweetertv.setText(getResources().getString(R.string.retweeter) + retweeter);
            retweetertv.setVisibility(View.VISIBLE);
        }

        String text = tweet;
        String extraNames = "";

        String screenNameToUse;

        if (secondAcc) {
            screenNameToUse = settings.secondScreenName;
        } else {
            screenNameToUse = settings.myScreenName;
        }

        if (text.contains("@")) {
            for (String s : users) {
                if (!s.equals(screenNameToUse) && !extraNames.contains(s) && !s.equals(screenName)) {
                    extraNames += "@" + s + " ";
                }
            }
        }

        if (retweeter != null && !retweeter.equals("") && !retweeter.equals(screenNameToUse) && !extraNames.contains(retweeter)) {
            extraNames += "@" + retweeter + " ";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransitionNames();
        }

        // last bool is whether it should open in the external browser or not
        TextUtils.linkifyText(context, retweetertv, null, true, linkString, false);
        TextUtils.linkifyText(context, tweettv, null, true, linkString, false);

        expansionHelper = new ExpansionViewHelper(context, tweetId);
        expansionHelper.setSecondAcc(secondAcc);
        expansionHelper.setBackground(layout.findViewById(R.id.content));
        expansionHelper.setInReplyToArea((LinearLayout) layout.findViewById(R.id.conversation_area));
        expansionHelper.setWebLink(otherLinks);
        expansionHelper.setUser(screenName);
        expansionHelper.setText(text);
        expansionHelper.setUpOverflow();
        expansionHelper.fromNotification(fromNotification);
        expansionHelper.setLoadCallback(new ExpansionViewHelper.TweetLoaded() {
            @Override
            public void onLoad(Status status) {
                if (status != null) {
                    setTime(status.getCreatedAt().getTime());
                }
            }
        });

        LinearLayout ex = (LinearLayout) layout.findViewById(R.id.expansion_area);
        ex.addView(expansionHelper.getExpansion());
    }

    private void setTime(long time) {
        String timeDisplay;


        DateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
        DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        if (settings.militaryTime) {
            dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
            timeFormatter = new SimpleDateFormat("kk:mm");
        }

        Locale locale = context.getResources().getConfiguration().locale;
        if (locale != null && !locale.getLanguage().equals("en")) {
            dateFormatter = android.text.format.DateFormat.getDateFormat(context);
        }

        if (!settings.militaryTime) {
            timeDisplay = timeFormatter.format(time) + "\n" + dateFormatter.format(time);
        } else {
            timeDisplay = timeFormatter.format(time).replace("24:", "00:") + "\n" +
                    dateFormatter.format(time);
        }

        timetv.setText(timeDisplay);
    }

    private ExpansionViewHelper expansionHelper;

    private void glide(String url, ImageView target) {
        try {
            Glide.with(TweetActivity.this).load(url)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(target);
        } catch (Exception e) {
            // activity destroyed
        }
    }

    private void loadMediaEntities() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // load the images
                // convert strings to urls
                try {
                    Status status = getTwitter().showStatus(tweetId);
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    for (MediaEntity m : status.getMediaEntities()) {
                        try {
                            URL url = new URL(m.getMediaURLHttps());
                            InputStream in = new BufferedInputStream(url.openStream());
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n;
                            while (-1 != (n = in.read(buf))) {
                                out.write(buf, 0, n);
                            }
                            out.close();
                            in.close();

                            response.write(out.toByteArray());
                        } catch (Exception ex) {
                            Log.e(TAG, "Error loading media for Event Chain. Could not verify hash.");
                            ex.printStackTrace();
                            break;
                        }
                    }

                    medias = new String(response.toByteArray());

                } catch (TwitterException e) {
                    Log.e(TAG, "Could not connect to twitter. Hash cannot be verified");
                    e.printStackTrace();
                }

                for (int i = 0; !boundService; i++) { // wait for the eventCC to connect
                    if (i > 10) { // stop after 10 seconds
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }

                eventCC.queryEventsByDescription(TweetActivity.this, String.valueOf(tweetId));
            }
        }).start();
    }

    @Override
    public void display(JSONObject events) {
        // events contains all events from the block chain network, that have the twitter id = 'tweetId'
        // we will simply use the first one. events is always non-null
        // if there is no event than the Tweet does not have a rating

        try {
            JSONArray array = events.getJSONArray("events");
            JSONObject event = array.getJSONObject(0);
            double trust = event.getDouble("trustworthiness");
            eventId = event.getString("id");
            eventCCtv.setText("Trust: " + trust);

            if (event.getString("image").equalsIgnoreCase(getMd5((replace ?
                    // need to remove the url that is at the end of the post if an image is attached
                    tweet.substring(0, tweet.length() - (embeddedTweetFound ? 33 : 25)).trim() :
                    tweet)
                    + screenName
                    + time
                    + medias))) {
                eventCCtv.setTextColor(Color.GREEN);
            } else {
                eventCCtv.setTextColor(Color.RED);
            }
        } catch (JSONException e) {
            Log.i(TAG, "No trust for this event");
        }

        // stop timer
        long duration = new Date().getTime() - startTime.getTime();
        Log.i(TAG, "Read Event in: " + duration + "ms");
    }

    String getMd5(String input) {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Date startAssessment;
    /**
     * assess event either positive or negative
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_asses_positive && boundService) {
            eventCC.assessEvent(this, eventId, 1, "", "");
            startAssessment = new Date();
        } else if (view.getId() == R.id.bt_assess_negative && boundService) {
            eventCC.assessEvent(this, eventId, -1, "", "");
            startAssessment = new Date();
        } else if (view.getId() == R.id.trust && eventId != null) {
            // start new activity with list of assessments
            Log.i(TAG, "showing assessments");
            Intent showAssessments = new Intent(this, ShowAssessActivity.class);
            showAssessments.putExtra("eventId", eventId);
            startActivity(showAssessments);
        }
    }

    @Override
    public void displayAssessment(JSONObject assessment) {
        // reload the assessment
        try {
            double trust = assessment.getDouble("trustworthiness");
            eventCCtv.setText("Trust: " + trust);
        } catch (JSONException e) {
            Log.e(TAG, "Error getting trust from JSON: " + assessment);
            e.printStackTrace();
        }

        // stop timer
        long duration = new Date().getTime() - startAssessment.getTime();
        Log.i(TAG, "Made Assessment in: " + duration + "ms");
    }
}
