package eu.europa.emsa.datastore.factory;

import eu.europa.emsa.datastore.WebServiceDataStore;
import org.geotools.data.*;
import org.geotools.util.KVP;
import org.geotools.util.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by drakiko on 14/01/2015.
 */
public class WebServiceDataStoreFactory extends AbstractDataStoreFactory implements DataStoreFactorySpi {

    private static final Logger LOGGER = Logging.getLogger(WebServiceDataStoreFactory.class);

   // static HashSet<Param> params = new HashSet<DataAccessFactory.Param>();

    static HashSet<Param> params = new HashSet<DataAccessFactory.Param>();

    public static final Param LAT = new Param("lat", String.class,
            "Name of Latitude or X value", true,"LAT");

    public static final Param LON = new Param("lon", String.class,
            "Name of Longitude or Y value", true,"LON");

    public static final Param URLP = new Param("path", String.class,
            "A URL pointing to the web service containing the data", true, null);

    public static final Param PROJECTION = new Param("projection", String.class,
            "EPSG code of projection", true,"EPSG:4326");

    public static final Param SERVICENAME = new Param("service", String.class, "name of the service",
            true);

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {

        String service = (String) SERVICENAME.lookUp(params);
        String lat = ((String) LAT.lookUp(params));
        String lon = ((String) LON.lookUp(params));
        String url = (String) URLP.lookUp(params);
        String projectionString = (String) PROJECTION.lookUp(params);

        WebServiceDataStore webServiceDataStore = new WebServiceDataStore(url,service, lat, lon, projectionString);
        return webServiceDataStore;
    }

    public String getDisplayName() {
        return "Web Service DataStore";
    }

    @Override
    public String getDescription() {
        return "A DataStore backed by a web service call";
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canProcess(Map params) {
        if (!super.canProcess(params)) {

            return false; // was not in agreement with getParametersInfo
        }

        try {
            URL url = (URL) URLP.lookUp(params);
            File f = DataUtilities.urlToFile(url);
           //todo: change it
            boolean accept = url.getFile().toUpperCase().endsWith("XLS")||url.getFile().toUpperCase().endsWith("XLSX");
            if(accept) {
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Param[] getParametersInfo() {
        LinkedHashMap map = new LinkedHashMap();
        setupParameters(map);
        return (Param[]) map.values().toArray(new Param[map.size()]);
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> stringSerializableMap) throws IOException {
        throw new UnsupportedOperationException("Read only datastore");
    }
    void setupParameters(LinkedHashMap map) {
        map.put(URLP.key, URLP);
        map.put(PROJECTION.key, PROJECTION);
        map.put(LAT.key, LAT);
        map.put(LON.key, LON);
        map.put(SERVICENAME.key, SERVICENAME);
        map.put(PROJECTION.key, PROJECTION);
    }
}
