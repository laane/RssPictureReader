package com.jujujuijk.android.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.jujujuijk.android.asynctask.ImageLoader;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.rssreader.ApplicationContextProvider;

import java.util.List;

/**
 * Created by jujujuijk on 10/31/13.
 */
public class LiveWallpaperService extends WallpaperService {

    public LiveWallpaperService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new RssLiveWallPaperEngine();
    }

    private class RssLiveWallPaperEngine extends Engine
            implements ImageLoader.ImageLoaderCallback {

        private final Handler mHandler = new Handler();
        private final Runnable mDrawRunner = new Runnable() {
            @Override
            public void run() {
                checkImage();
            }
        };

        String mPictureUrl = "";

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            mHandler.post(mDrawRunner);
            super.onCreate(surfaceHolder);
        }

        private void checkImage() {
            // HOOK HERE TO FETCH NEW PIC
            Feed feed = null;
            List<Feed> feedList = MyDatabase.getInstance().getAllFeeds();
            synchronized (feedList) {
                for (Feed f : feedList) {
                    if ((f.getNotify() & Feed.Notify.LIVE_WALLPAPER) != 0) {
                        feed = f;
                        break;
                    }
                }
                if (feed == null) {
                    feed = feedList.get(0); // If we chose the live wallpaper not from the app
                    feed.setNotify(feed.getNotify() | Feed.Notify.LIVE_WALLPAPER);
                    MyDatabase.getInstance().updateFeed(feed);
                }
            }

            if (!feed.getPictureLast().equals(mPictureUrl)) {
                mPictureUrl = feed.getPictureLast();
                new ImageLoader(this, 0).execute(mPictureUrl);
            }

            mHandler.removeCallbacks(mDrawRunner);
            mHandler.postDelayed(mDrawRunner, 5000);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mHandler.removeCallbacks(mDrawRunner);
        }

        @Override
        public void onImageLoaderPostExecute(int id, final Bitmap image) {
            new Runnable() {
                @Override
                public void run() {
                    drawFrame(image);
                }
            }.run();
        }

        void drawFrame(Bitmap imageSrc) {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    try {
                        WindowManager wm = (WindowManager) ApplicationContextProvider.getContext().getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();
                        int w, h;

                        Point size = new Point();
                        display.getSize(size);
                        w = size.x;
                        h = size.y;

                        Bitmap scaled;
                        if (w > 0 && h > 0) {

                            int imageWidth = imageSrc.getWidth();
                            int imageHeight = imageSrc.getHeight();

                            float scaleFactor = Math.min((float) w / imageWidth, (float) h
                                    / imageHeight);

                            imageWidth = (int) (scaleFactor * imageWidth);
                            imageHeight = (int) (scaleFactor * imageHeight);

                            Bitmap image = Bitmap.createScaledBitmap(imageSrc, imageWidth,
                                    imageHeight, true);

                            scaled = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            scaled.eraseColor(Color.BLACK);

                            Canvas scaledCanvas = new Canvas(scaled);

                            scaledCanvas.drawBitmap(image, (w - imageWidth) / 2,
                                    (h - imageHeight) / 2, new Paint());

                        } else {
                            scaled = imageSrc;
                        }
                        c.drawBitmap(scaled, 0, 0, null);
                    } catch (Exception e) {
                        System.gc();
                    }
                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
        }

    }

}
