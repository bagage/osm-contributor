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
package io.mapsquare.osmcontributor.ui.managers;


import android.app.Application;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.mapsquare.osmcontributor.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.model.events.PleaseCreatePoiEvent;
import io.mapsquare.osmcontributor.model.events.PleaseDeletePoiEvent;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiNodeRef;
import io.mapsquare.osmcontributor.model.entities.PoiTag;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyNodeRefPositionChange;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyPoiChanges;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyPoiPositionChange;
import io.mapsquare.osmcontributor.ui.events.edition.PoiChangesApplyEvent;
import io.mapsquare.osmcontributor.ui.events.map.PleaseCreateNoTagPoiEvent;
import io.mapsquare.osmcontributor.ui.events.map.PoiNoTypeCreated;
import timber.log.Timber;

public class EditPoiManager {

    PoiManager poiManager;
    Application application;
    PoiNodeRefDao poiNodeRefDao;
    EventBus eventBus;

    @Inject
    public EditPoiManager(PoiManager poiManager, PoiNodeRefDao poiNodeRefDao, Application application, EventBus eventBus) {
        this.poiManager = poiManager;
        this.application = application;
        this.poiNodeRefDao = poiNodeRefDao;
        this.eventBus = eventBus;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiChanges(PleaseApplyPoiChanges event) {
        Timber.d("please apply poi changes");
        Poi editPoi = poiManager.queryForId(event.getPoiChanges().getId());

        if (editPoi.hasChanges(event.getPoiChanges().getTagsMap())) {

            editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

            //this is the edition of a new poi or we already edited this poi
            editPoi.applyChanges(event.getPoiChanges().getTagsMap());
            editPoi.setUpdated(true);
            poiManager.savePoi(editPoi);
            poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
        }

        eventBus.post(new PoiChangesApplyEvent());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiPositionChange(PleaseApplyPoiPositionChange event) {
        Timber.d("Please apply poi position change");
        Poi editPoi = poiManager.queryForId(event.getPoiId());

        editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

        editPoi.setLatitude(event.getPoiPosition().getLatitude());
        editPoi.setLongitude(event.getPoiPosition().getLongitude());
        editPoi.setUpdated(true);
        poiManager.savePoi(editPoi);
        poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyNodeRefPositionChange(PleaseApplyNodeRefPositionChange event) {
        Timber.d("Please apply noderef position change");
        LatLng newLatLng = event.getPoiPosition();

        //apply changes on the noderef
        PoiNodeRef poiNodeRef = poiNodeRefDao.queryForId(event.getPoiId());

        poiNodeRef.setOldPoiId(saveOldVersionOfPoiNodeRef(poiNodeRef));

        poiNodeRef.setLongitude(newLatLng.getLongitude());
        poiNodeRef.setLatitude(newLatLng.getLatitude());
        poiNodeRef.setUpdated(true);
        poiNodeRefDao.createOrUpdate(poiNodeRef);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseCreatePoiEvent(PleaseCreatePoiEvent event) {
        Timber.d("Please create poi");
        Poi poi = event.getPoi();
        poi.setUpdated(true);
        poi.applyChanges(event.getPoiChanges().getTagsMap());
        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(poi.getType().getId());
        eventBus.post(new PoiChangesApplyEvent());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseDeletePoiEvent(PleaseDeletePoiEvent event) {
        Timber.d("Please delete poi");
        Poi poi = event.getPoi();
        if (poi.getId() != null) {
            poi = poiManager.queryForId(poi.getId());
        }

        if (poi.getBackendId() == null) {
            poiManager.deletePoi(poi);
        } else {
            poi.setOldPoiId(saveOldVersionOfPoi(poi));
            poi.setToDelete(true);
            poiManager.savePoi(poi);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseCreateNoTagPoiEvent(PleaseCreateNoTagPoiEvent event) {
        Poi poi = new Poi();
        LatLng latLng = event.getLatLng();

        poi.setLatitude(latLng.getLatitude());
        poi.setLongitude(latLng.getLongitude());
        poi.setType(event.getPoiType());

        List<PoiTag> defaultTags = new ArrayList<>();
        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            if (poiTypeTag.getValue() != null) { // default tags should be set in the corresponding POI
                PoiTag poiTag = new PoiTag();
                poiTag.setKey(poiTypeTag.getKey());
                poiTag.setValue(poiTypeTag.getValue());
                defaultTags.add(poiTag);
            }
        }

        poi.setTags(defaultTags);
        poi.setUpdated(true);

        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(event.getPoiType().getId());

        eventBus.post(new PoiNoTypeCreated());
    }

    private Long saveOldVersionOfPoi(Poi poi) {
        if (poi.getBackendId() == null) {
            return null;
        }
        if (poiManager.countForBackendId(poi.getBackendId()) == 1) {
            Poi old = poi.getCopy();
            old.setOld(true);
            poiManager.savePoi(old);
            return old.getId();
        }
        return poi.getOldPoiId();
    }

    private Long saveOldVersionOfPoiNodeRef(PoiNodeRef poiNodeRef) {
        if (poiNodeRefDao.countForBackendId(poiNodeRef.getNodeBackendId()) == 1) {
            PoiNodeRef old = poiNodeRef.getCopy();
            old.setOld(true);
            poiNodeRefDao.createOrUpdate(old);
            return old.getId();
        }
        return poiNodeRef.getOldPoiId();
    }
}
