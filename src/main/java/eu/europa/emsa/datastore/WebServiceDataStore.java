package eu.europa.emsa.datastore;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.io.FileUtils;
import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by drakiko on 14/01/2015.
 */
public class WebServiceDataStore extends ContentDataStore {

    private static final Logger logger = Logging.getLogger(WebServiceDataStore.class);

    private URL url;

    private String lat;

    private String lon;

    private CoordinateReferenceSystem projection;

    ArrayList<Name> names = new ArrayList<Name>();

    String name = "";

    public String getName() {
        return name;
    }

    public WebServiceDataStore(String url, String service, String lat, String lon, String projectionString) {
        super();
        try {
            this.url = new URL (url + File.separator + service);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.name = this.url.getFile();
        this.lat = lat;
        this.lon = lon;
        // SHOULD GET THE WADL HERE or do nothing
        try {
            setProjection(CRS.decode(projectionString));
        } catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected List<Name> createTypeNames() throws IOException {
        if (names.isEmpty()) {
            String name = getUrl().getPath();
            Name typeName = new NameImpl(name);
            names.add(typeName);
        }
        return names;
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new WebServiceFeatureSource(entry, Query.ALL);
    }

    /**
     * Provide a geometery factory
     * if none has been set then a JTS GeometeryFactory is returned.
     */
    @Override
    public GeometryFactory getGeometryFactory() {
        GeometryFactory fac = super.getGeometryFactory();
        if (fac == null) {
            fac = JTSFactoryFinder.getGeometryFactory(null);
            setGeometryFactory(fac);
        }
        return fac;
    }


    public void setProjection(CoordinateReferenceSystem projection) {
        this.projection = projection;
    }

    public CoordinateReferenceSystem getProjection() {
        return projection;
    }

    public URL getUrl() {
        return url;
    }


    public String getLon() {
        return lon;
    }

    public String getLat() {
        return lat;
    }


}
