/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.flickr.util;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.SSLSocketFactory;

import io.mapsquare.osmcontributor.flickr.oauth.NoSSLv3SocketFactory;
import io.mapsquare.osmcontributor.rest.utils.StringConverter;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

public class FlickrPhotoUtils {

    public static RestAdapter adapter;

    public static RestAdapter getAdapter() {
        if (adapter == null) {
            OkHttpClient okHttpClient = new OkHttpClient();
            try {
                SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(new URL("https://api.flickr.com/services/rest"));
                okHttpClient.setSslSocketFactory(NoSSLv3Factory);
                adapter = new RestAdapter.Builder()
                        .setConverter(new StringConverter())
                        .setEndpoint("https://api.flickr.com/services")
                        .setClient(new OkClient(okHttpClient))
                        .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("---------------------->"))
                        .build();
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }
        }
        return adapter;
    }
}
