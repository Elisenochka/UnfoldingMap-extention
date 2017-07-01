package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.

	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";



	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	List<Button>buttons = new ArrayList<Button>();

	String[] titles = {"Latest quakes","Biggest quakes"};
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	private Button buttonClicked;
	private Button buttonSelected;

	public void setup() {
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.RoadProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);

		int xbase = 25;
		int ybase = 305;
		for(String s : titles){
			buttons.add(new Button(this, s,xbase,ybase,false));
			ybase = ybase+25;
		}// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";

		// Uncomment this line to take the quiz
		earthquakesURL = "quiz2.atom";

		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}

		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();

	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    //printQuakes();
	    //sortAndPrint(100);
	    //sort();

	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);


	}  // End setup


	public void draw() {
		background(0);
		map.draw();
		addKey();

		for (Button b : buttons){
			if(mouseX<(b.buttonX+150)&&mouseX>b.buttonX&&mouseY<(b.buttonY+20)&&mouseY>b.buttonY)
				b.mouseIns = true;
			else
				b.mouseIns = false;
			drawButton(b, b.buttonX, b.buttonY);
		}
	}

	// TODO: Add the method:
	//   private void sortAndPrint(int numToPrint)
	// and then call that method from setUp
	private void sortAndPrint(int numToPrint){
		List<EarthquakeMarker> leqm = new ArrayList<EarthquakeMarker>();
		for(Marker m : quakeMarkers){
			EarthquakeMarker e = (EarthquakeMarker) m;
			leqm.add(e);
		}
		Object[] eqm = leqm.toArray();
		if(numToPrint>eqm.length)
			numToPrint = eqm.length;
		int currentPos;
		for(int i =1;i<eqm.length;i++){
			currentPos = i;
			while(currentPos>0 && ((EarthquakeMarker) eqm[currentPos]).compareTo((EarthquakeMarker) eqm[currentPos-1])<0){
				EarthquakeMarker m = (EarthquakeMarker) eqm[currentPos];
				eqm[currentPos] = eqm[currentPos-1];
				eqm[currentPos-1] = m;
				currentPos--;
			}
		}
		for(int i = 0; i<numToPrint;i++){
			System.out.println(eqm[i].toString());
		}
	}
	/*private void sort(){
		int currentPos;
		for(int i =1;i<quakeMarkers.size();i++){
			currentPos = i;
			while(currentPos>0 && ((EarthquakeMarker) quakeMarkers.get(currentPos)).compareTo((EarthquakeMarker)quakeMarkers.get(currentPos-1))<0){
				currentPos--;
			}
			quakeMarkers.get(i).setProperty("Ascending", currentPos);
			System.out.println(quakeMarkers.get(i).getProperty("Ascending")+" "+quakeMarkers.size());
		}
	}*/

	public void drawButton (Button b,float x, float y) {
		if(b.selected == true){
			fill(0);
			rect(x, y, 150, 20);
			fill(255, 250, 240);
			textAlign(LEFT, CENTER);
			textSize(12);
			text(b.getTitle(), x+8, y+8);
		}
		else if(b.clicked == true){
			fill(153);
			rect(x, y, 150, 20);
			fill(0);
			textAlign(LEFT, CENTER);
			textSize(12);
			text(b.getTitle(), x+8, y+8);
		}
		else{
			fill(255, 250, 240);
			rect(x, y, 150, 20);
			fill(0);
			textAlign(LEFT, CENTER);
			textSize(12);
			text(b.getTitle(), x+8, y+8);
		}

	}

	/** Event handler that gets called automatically when the
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		else if (buttonSelected != null) {
			buttonSelected.setSelected(false);
			buttonSelected = null;
			for (Button b : buttons){
				if(b.mouseIns){
					b.setSelected(true);
					buttonSelected =b;
				}
			}
		}
			for (Button b : buttons){
				if(b.mouseIns){
					b.setSelected(true);
					buttonSelected =b;
				}
			}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		//loop();
	}

	// If there is a marker selected
	private void selectMarkerIfHover(List<Marker> markers)	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers)
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}


	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if(buttonClicked !=null) {
			buttonClicked.setClicked(false);
			unhideMarkers();
			buttonClicked = null;
				for (Button b : buttons){
					if(b.mouseIns){
						buttonClicked = b;
						b.setClicked(true);
						checkButtonForClick(b);
					}
				}
		}
		else if (lastClicked == null) {
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}
		}
		for (Button b : buttons){
			if(b.mouseIns){
				buttonClicked = b;
				b.setClicked(true);
				checkButtonForClick(b);
			}
		}
	}



	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation())
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}
	}

	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick() {
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected

		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation())
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

	private void checkButtonForClick(Button b)	{
		//if (buttonClicked != null) return;
		for (Marker c : cityMarkers) {
				c.setHidden(true);
		}
		if(b.getTitle().equals("Latest quakes")&&b.clicked==true){
			for (Marker q : quakeMarkers) {
				EarthquakeMarker eqm = (EarthquakeMarker)q;

				String age = eqm.getStringProperty("age");
				if (!"Past Hour".equals(age) && !"Past Day".equals(age)){
					eqm.setHidden(true);
					for (Marker c : cityMarkers) {
						if (c.getDistanceTo(eqm.getLocation())< eqm.threatCircle()) {
							c.setHidden(false);
						}
					}
				}
			}
			return;
		}
		if(b.getTitle().equals("Biggest quakes")&&b.clicked==true){
			for (Marker q : quakeMarkers) {
				EarthquakeMarker eqm = (EarthquakeMarker) q;
				if (eqm.getMagnitude()<4.0&&eqm.getDepth()<300) {
					q.setHidden(true);
					for (Marker c : cityMarkers) {
						if (c.getDistanceTo(eqm.getLocation())< eqm.threatCircle()) {
							c.setHidden(false);
						}
					}
				}
			}
			return;
		}
	}

	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}

		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}

	// helper method to draw key in GUI
	private void addKey() {
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);

		int xbase = 25;
		int ybase = 50;

		rect(xbase, ybase, 150, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);

		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE,
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE,
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);

		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);

		fill(255, 255, 255);
		ellipse(xbase+35,
				ybase+70,
				10,
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);

		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);

		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);

		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);

	}



	// Checks whether this quake occurred on land.  If it did, it sets the
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {

		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		// not inside any country
		return false;
	}

	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property,
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}



	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {

				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));

			return true;
		}
		return false;
	}

}
