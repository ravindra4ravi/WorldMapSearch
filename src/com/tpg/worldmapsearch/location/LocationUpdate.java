package com.tpg.worldmapsearch.location;

import com.google.android.gms.maps.model.LatLng;

public interface LocationUpdate {
  public abstract void updateLocation(LatLng lat);
}
