package com.tanyong.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baidu on 16/2/23.
 * Flickr网络类
 */
public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final String API_KEY = "1d20e3154098d1abbb85cb8218d2c9c7";

    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
                    //.appendQueryParameter("method", "flickr.photos.getRecent")
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
                    //.appendQueryParameter("page",""+page)
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int byteRead = 0;
            byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, byteRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(items, jsonObject);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    public List<GalleryItem> fetchRecentPhotos(int page) {
        String url = ENDPOINT.buildUpon()
                .appendQueryParameter("method", FETCH_RECENTS_METHOD)
                .appendQueryParameter("page", "" + page)
                .build().toString();
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query, int page) {
        String url = ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("text", query)
                .appendQueryParameter("page", "" + page)
                .build().toString();
        return downloadGalleryItems(url);
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonObject)
            throws IOException, JSONException {
        JSONObject photosJO = jsonObject.getJSONObject("photos");
        JSONArray photoJA = photosJO.getJSONArray("photo");
        for (int i = 0; i < photoJA.length(); i++) {
            JSONObject photoJO = photoJA.getJSONObject(i);
            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setId(photoJO.getString("id"));
            galleryItem.setCaption(photoJO.getString("title"));
            if (!photoJO.has("url_s")) {
                continue;
            }
            galleryItem.setUrl(photoJO.getString("url_s"));
            galleryItem.setOwner(photoJO.getString("owner"));
            items.add(galleryItem);
        }
    }
}
