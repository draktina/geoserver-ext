package org.cwatch.datastore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by drakiko on 15/01/2015.
 */
public class WebServiceFeatureSource extends ContentFeatureSource implements SimpleFeatureSource {

    private Query lastQuery = null;
    private WebServiceDataStore dataStore;
    private URL url;

    private ArrayList<SimpleFeature> features, filteredFeatures;
  //  private HttpRestClient httpRestClient = new HttpRestClient();

    private String lat;

    private String lon;


    public WebServiceFeatureSource(ContentEntry entry) {
        super(entry, Query.ALL);
    }
    /**
     * create a FeatureSource with the specified Query
     *
     * @param entry
     * @param query
     * - a query containing a filter that will be applied to the data
     */
    public WebServiceFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);

        dataStore = (WebServiceDataStore) entry.getDataStore();

        url = dataStore.getUrl();
        lat = dataStore.getLat();
        lon = dataStore.getLon();

        features = new ArrayList<SimpleFeature>();
        filteredFeatures = new ArrayList<SimpleFeature>();

        if (schema==null){
            schema=getSchema();
        }

        GeometryFactory geometryFactory = dataStore.getGeometryFactory();

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);

        // Extract info from web call here? params of call?
        Point p = geometryFactory.createPoint(new Coordinate(0.0, 0.0));
        builder.set("the_geom", p);

        SimpleFeature feature = builder.buildFeature(null);
        features.add(feature);

       filterFeatures(query);
    }

    /**
     * Calculates the bounds of a specified query.
     *
     * @param query - the query to be applied.
     */
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        ReferencedEnvelope env = new ReferencedEnvelope(getSchema().getCoordinateReferenceSystem());
        if (lastQuery != query)
            filterFeatures(query);
        for (SimpleFeature feature : filteredFeatures) {
            Point p = (Point) feature.getDefaultGeometry();
            env.expandToInclude(p.getCoordinate());
        }
        return env;
    }


    @Override
    protected int getCountInternal(Query query) throws IOException {
        if (lastQuery != query)
            filterFeatures(query);
        return filteredFeatures.size();
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        if (lastQuery != query)
            filterFeatures(query);
        try {
            return new WebServiceFeatureReader(filteredFeatures, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * regenerate the filteredFeatures list if the query has changed since the last time we did
     * this.
     *
     * @param query
     */
    private void filterFeatures(Query query) {
        filteredFeatures = new ArrayList<SimpleFeature>();
        for (SimpleFeature feature : features) {
            if (query.getFilter().evaluate(feature)) {
                filteredFeatures.add(feature);
            }
        }
        lastQuery = query;
    }
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {

     //   connectToUri() of wadl to get the schema and add all elements!
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(entry.getName());
        tb.setCRS(dataStore.getProjection());
        tb.add("vessel", String.class);
        tb.add("the_geom", Point.class);
        //tb.add("");
       //build the type (it is immutable and cannot be modified)

        final SimpleFeatureType SCHEMA = tb.buildFeatureType();
        return SCHEMA;
    }

    private void connectToUri() {
       // httpRestClient.restGet(url.getPath())
    }

    public Query getLastQuery() {
        return lastQuery;
    }

    public URL getUrl() { return this.url;}
}
