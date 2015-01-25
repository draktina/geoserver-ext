package org.cwatch.http;

/**
 * Created by draktina on 1/24/15.
 */



/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cwatch.datastore.WebServiceDataStore;
import org.cwatch.datastore.WebServiceFeatureSource;
import org.cwatch.datastore.factory.WebServiceDataStoreFactory;
import org.geotools.data.DataTestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * @source $URL$
 */
public class WebServiceDataStoreTest extends DataTestCase {

    public WebServiceDataStoreTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    static WebServiceDataStore[] wbs;

    static boolean setup = false;

    @Override
    protected void setUp() throws Exception {
        if (!setup) {
            super.setUp();

            wbs = new WebServiceDataStore[1];
            int i = 0;
            HashMap<String, Serializable> params = new HashMap<String, Serializable>();
            //System.out.println(url);
            File propsFile = null;


            params.put("path", "http://localhost:8080/jerseyrest/rest/jsonServices");
            params.put("projection", "epsg:4326");

            WebServiceDataStoreFactory fac = new WebServiceDataStoreFactory();
            assertTrue("Can't process params", fac.canProcess(params));
            WebServiceDataStore ex = (WebServiceDataStore) fac.createDataStore(params);
            assertNotNull("Null data store", ex);
            //System.out.println("adding " + i + " " + ex);
            wbs[i++] = ex;

        }
        setup = true;
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testWSDatastore() throws IOException {
        File file = TestData.file(this, "locations.xls");

        java.net.URL url = DataUtilities.fileToURL(file);
        Iterator<DataStoreFactorySpi> it = DataStoreFinder.getAvailableDataStores();
        int count = 0;
        while (it.hasNext()) {
            count++;
            final DataStoreFactorySpi next = it.next();
            System.out.println(next + " " + next.getDisplayName() + " " + next.isAvailable());
        }

        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("type", "excel");
        params.put("url", url);
        params.put("sheet", "locations");
        params.put("latcol", "LAT");
        params.put("longcol", "LON");
        params.put("projection", "epsg:4326");
        if (count == 1) {
            // running in Eclipse I only get a MockDataStoreFactory found
            DataStore store = DataStoreFinder.getDataStore(params);
            assertNotNull("no datastore found", store);
            System.out.println(store.getInfo());
        }
        WebServiceDataStoreFactory fac = new WebServiceDataStoreFactory();

        assertTrue("Can't process params", fac.canProcess(params));
        DataStore store = fac.createDataStore(params);
        assertNotNull("no datastore created", store);
        params.put("headerrow", 0);
        assertTrue("Can't process params with headerrow", fac.canProcess(params));
        params.remove("sheet");
        assertFalse("Can process params without sheet", fac.canProcess(params));
    }

    public void testGetNames() throws IOException {
        for (WebServiceDataStore ed : wbs) {
            System.out.println(ed.getName());

            String[] names = ed.getTypeNames();
            System.out.println(names);
            if (ed.getName().contains("qed"))
                continue;
            assertEquals("Sheet Name is wrong", "locations", names[0]);
        }
    }

    public void testGetFeatureSource() throws IOException {
        for (WebServiceDataStore ed : wbs) {
            System.out.println(ed.getName());

            List<Name> names = ed.getNames();
            WebServiceFeatureSource source = (WebServiceFeatureSource) ed.getFeatureSource(names.get(0));
            assertNotNull("FeatureSource is null", source);
            SimpleFeatureType schema = source.getSchema();
            assertNotNull("Null Schema", schema);
            System.out.println(schema.getAttributeCount());
            List<AttributeDescriptor> attrs = schema.getAttributeDescriptors();
            for (AttributeDescriptor attr : attrs) {
                System.out.println(attr.getName() + ": " + attr.getType());
            }
            ContentFeatureCollection fts = source.getFeatures();
            System.out.println("BBox = " + source.getBounds());
            System.out.println("got " + fts.size() + " features");
            SimpleFeatureIterator its = fts.features();
            int count = 10;
            int counter = 0;
            while (its.hasNext() && counter++ < count) {
                SimpleFeature feature = its.next();
                System.out.print(feature.getID() + ": ");
                for (AttributeDescriptor attr : attrs) {
                    System.out.print(feature.getAttribute(attr.getName()) + ", ");
                }
                System.out.println();
            }
            if (ed.getName().contains("qed"))
                continue;
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            Filter filter = ff.equal(ff.property("CITY"), ff.literal("Trento"), true);
            Query query = new Query("locations", filter);
            fts = source.getFeatures(query);
            System.out.println(fts.size());
            System.out.println(fts.features().next());
        }
    }

    /**
     * Test query with a start index
     *
     * @throws IOException
     * @throws FileNotFoundException
     */

    public void testOffset() throws FileNotFoundException, IOException {
        Query query = new Query(Query.ALL);
        query.setStartIndex(1);

        for (WebServiceDataStore ed : wbs) {
            System.out.println(ed.getName());

            List<Name> names = ed.getNames();
            WebServiceFeatureSource source = (WebServiceFeatureSource) ed.getFeatureSource(names.get(0));
            assertEquals(source.getCount(Query.ALL) - 1, source.getCount(query));
        }
    }

    /**
     * Test query with maxFeatures
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void testLimit() throws FileNotFoundException, IOException {
        Query query = new Query(Query.ALL);
        query.setMaxFeatures(2);
        for (WebServiceDataStore ed : wbs) {
            System.out.println(ed.getName());

            List<Name> names = ed.getNames();
            WebServiceFeatureSource source = (WebServiceFeatureSource) ed.getFeatureSource(names.get(0));
            assertTrue(2 >= source.getCount(query));
        }
    }

    /**
     * Test query with maxFeatures and startIndex
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void testLimitOffset() throws FileNotFoundException, IOException {
        Query query = new Query(Query.ALL);
        query.setMaxFeatures(2);
        query.setStartIndex(1);
        for (WebServiceDataStore ed : wbs) {
            System.out.println(ed.getName());

            List<Name> names = ed.getNames();
            WebServiceFeatureSource source = (WebServiceFeatureSource) ed.getFeatureSource(names.get(0));
            assertEquals(Math.min(2, source.getCount(Query.ALL) - 1), source.getCount(query));
        }
    }
}