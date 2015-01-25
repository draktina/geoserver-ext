package eu.europa.emsa.datastore;

import eu.europa.emsa.http.HttpRestClient;
import org.geotools.data.FeatureReader;
import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by drakiko on 15/01/2015.
 */
public class WebServiceFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private WebServiceFeatureSource source;

    private ArrayList<SimpleFeature> features;

    private Iterator<SimpleFeature> iterator;

    private HttpRestClient httpRestClient = new HttpRestClient();

    public WebServiceFeatureReader(ArrayList<SimpleFeature> features, WebServiceFeatureSource webServiceFeatureSource) throws IOException, URISyntaxException {
        this.features = features;
        this.source = webServiceFeatureSource;


        Map params = (Map) this.source.getLastQuery().getHints().get(Hints.VIRTUAL_TABLE_PARAMETERS);

        //source.getDataStore().get
        String test = httpRestClient.restGet(source.getUrl().toString(), "test");

        iterator = features.iterator();

    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return iterator.next();
    }

    @Override
    public boolean hasNext() throws IOException {
        return iterator.hasNext();
    }

    @Override
    public void close() throws IOException {
        iterator = features.iterator();
    }

    /* (non-Javadoc)
      * @see org.geotools.data.FeatureReader#getFeatureType()
    */
    public SimpleFeatureType getFeatureType() {
        return source.getSchema();
    }
}
