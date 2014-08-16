package com.jujujuijk.android.asynctask;

import java.io.IOException;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;
import com.jujujuijk.android.rssreader.R;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.Toast;

public class WallpaperSetter extends AsyncTask<Bitmap, Void, Boolean> {

    private Bitmap m_image;
	private Context m_context;

	public WallpaperSetter() {
		m_context = ApplicationContextProvider.getContext();
	}

	@Override
	protected Boolean doInBackground(Bitmap... images) {
		if (images.length == 0)
			return false;

		m_image = images[0];

		WallpaperManager myWallpaperManager = WallpaperManager
				.getInstance(ApplicationContextProvider.getContext());
		try {
			int w = myWallpaperManager.getDesiredMinimumWidth();
			int h = myWallpaperManager.getDesiredMinimumHeight();

			Bitmap scaled;
			if (w > 0 && h > 0) {

				int imageWidth = m_image.getWidth();
				int imageHeight = m_image.getHeight();

				float scaleFactor = Math.min((float) w / imageWidth, (float) h
						/ imageHeight);

				imageWidth = (int) (scaleFactor * imageWidth);
				imageHeight = (int) (scaleFactor * imageHeight);

				Bitmap image = Bitmap.createScaledBitmap(m_image, imageWidth,
						imageHeight, true);

				scaled = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				scaled.eraseColor(Color.BLACK);

				Canvas scaledCanvas = new Canvas(scaled);

				scaledCanvas.drawBitmap(image, (w - imageWidth) / 2,
						(h - imageHeight) / 2, new Paint());
			} else {
				scaled = m_image;
			}

			myWallpaperManager.setBitmap(scaled);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (success != null && success == true) {
			Toast.makeText(
					m_context,
					m_context.getResources().getString(R.string.wallpaper_done),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(
					m_context,
					m_context.getResources()
							.getString(R.string.wallpaper_error),
					Toast.LENGTH_SHORT).show();
		}
	}
}
