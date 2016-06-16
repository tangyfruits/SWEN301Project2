package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import event.CustomerPrice;
import event.DeliveryRequest;
import event.DiscontinueRoute;
import event.Leg;
import event.Route;

public class Main {

	// FIELDS
	private ArrayList<Location> locations;
	private ArrayList<User> accounts;
	private User currentUser;
	private HashMap<TuplePriority, ArrayList<Integer>> amountOfMailDeliveryTimes;
	private HashMap<Tuple, ArrayList<Double>> amountOfMail;
	private ArrayList<DeliveryRequest> deliveryRequests;

	private File configFile;
	private File logFile;
	private LogWriter writer;

	private int events;
	private double totalExp;
	private double totalRev;

	
	// CONSTRUCTOR
	public Main() {
		// Core Logic
		locations = new ArrayList<Location>();
		deliveryRequests = new ArrayList<DeliveryRequest>();
		accounts = new ArrayList<User>();
		configFile = new File(".config");
		// Reports
		amountOfMailDeliveryTimes = new HashMap<>();
		amountOfMail = new HashMap<>();
		loadFromConfig();
		writer = new LogWriter(logFile);
	}
	
	private void loadFromConfig() {
		
		// IF CONFIG DOESN'T EXIST
		if (!configFile.isFile()){
			System.out.println("config doesn't exist - make new file");
			try {
				configFile.createNewFile();
				FileWriter fw = new FileWriter(configFile);
				logFile = new File("logfile.xml");
				fw.write(logFile.getName()+"\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		// IF CONFIG EXISTS
		} else {
			System.out.println("Config exists");
			try {
				List<String> lines = Files.readAllLines(configFile.toPath());
				System.out.println(lines);
				Scanner sc = new Scanner(configFile);
				
				// Config is Empty
				if (lines.size() <= 0) {
					System.out.println("Config is empty");
					try {
						logFile = new File("logfile.txt");
						FileWriter fw = new FileWriter(configFile, false);
						
						fw.write(logFile.getName() + "\n");
						fw.flush();
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				// Config isn't Empty
				} else {
					System.out.println("Config isn't empty");
					
					// Read LogfileName
					String firstline = lines.get(0);
					if (firstline.endsWith(".xml")) {
						// Valid Logfile Name
						logFile = new File(firstline);
					} else {
						// Invalid Logfile Name
						System.out.println("LALLALALA INVALID LOGFILE NAME!!!!!");
						try {
							//List<String> remainder = lines.subList(1, lines.size());

							FileWriter fw = new FileWriter(configFile, false);
							logFile = new File("logfile.xml");
							fw.write(logFile.getName() + "\n");
							
							for (String line : lines) {
								fw.write(line);
							}
							
							fw.flush();
							fw.close();
							sc = new Scanner(configFile);
							sc.nextLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				
					// Read Users
					int lineNo = 1;
					while (sc.hasNextLine()) {
						try {
							lineNo++;
							String scanLine = sc.nextLine();
							String[] line = scanLine.split(" ");
							if (line.length == 3) {
								String username = line[0];
								String password = line[1];
								String manager = line[2];
								boolean b = false;
								if (manager.equals("true")) {
									b = true;
								}
								accounts.add(new User(username, password, b));
							} else {
								System.out.println("Read weird line: \""+scanLine+"\"");
							}
						} catch (Exception e) {
							System.out.println("Error reading config file line "+lineNo);
						}
					}
					sc.close();
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	// USER ACCOUNT METHODS
	public boolean login(String username, String password) {
		for (User u : accounts) {
			if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
				currentUser = u;
				return true;
			}
		}
		return false;
	}
	public boolean editPassword(String password) {
		boolean edited = false;
		for (User u : accounts) {
			if (u.getUsername().equals(currentUser.getUsername()) && 
				u.getPassword().equals(currentUser.getPassword())) {
				u.setPassword(password);
				edited = true;
			}
		}
		currentUser.setPassword(password);
		try {
			configFile.delete();
			configFile.createNewFile();
			FileWriter fw = new FileWriter(configFile, true);
			fw.write(logFile.getName()+"\n");
			for (int i = 0; i < accounts.size(); i++) {
				User u = accounts.get(i);
				fw.write(u.getUsername() + " " + u.getPassword() + " " + Boolean.toString(u.isManager()) + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return edited;
	}
	public boolean editManager(boolean manager ) {
		boolean edited = false;
		for (User u : accounts) {
			if (u.getUsername().equals(currentUser.getUsername()) && 
				u.getPassword().equals(currentUser.getPassword())) {
				u.setManager(manager);
				edited = true;
			}
		}
		currentUser.setManager(manager);
		try {
			configFile.delete();
			configFile.createNewFile();
			FileWriter fw = new FileWriter(configFile, true);
			fw.write(logFile.getName()+"\n");
			for (int i = 0; i < accounts.size(); i++) {
				User u = accounts.get(i);
				fw.write(u.getUsername() + " " + u.getPassword() + " " + Boolean.toString(u.isManager()) + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return edited;
	}
	public boolean delete() {
		boolean removed = false;
		for (User u : accounts) {
			if (u.getUsername().equals(currentUser.getUsername()) && 
				u.getPassword().equals(currentUser.getPassword())) {
				accounts.remove(u);
				logout();
				removed = true;
				break;
			}
		}
		try {
			configFile.delete();
			configFile.createNewFile();
			FileWriter fw = new FileWriter(configFile, true);
			fw.write(logFile.getName()+"\n");
			for (int i = 0; i < accounts.size(); i++) {
				User u = accounts.get(i);
				fw.write(u.getUsername() + " " + u.getPassword() + " " + Boolean.toString(u.isManager()) + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return removed;
	}
	public void add(User user) {
		boolean b = false;
		for (User u : accounts){
			if(u.getUsername().equals(user.getUsername())){
				b=true;
			}
		}
		if(!b){
		try {
			FileWriter writer = new FileWriter(configFile, true);
			writer.write(user.getUsername() + " " + user.getPassword() + " " + Boolean.toString(user.isManager()) + "\n");
			writer.flush();
			writer.close();
			accounts.add(user);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	}
	
	public User findUser(String username){
		for (User u : accounts) {
			if (u.getUsername().equals(username)){
				return u;
			}
	}
		return null;
	}
	public void logout() {
		currentUser = null;
	}
	public User getCurrentUser() {
		return currentUser;
	}
	public void addUserToList(User u) {
		accounts.add(u);
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public ArrayList<User> getAccounts() {
		return accounts;
	}

	// LOGGERS
	public DeliveryRequest logDeliveryRequest(LocalDateTime logTime, String origin, String destination,
			ArrayList<Leg> legs, double weight, double volume, String priority, int duration, boolean initial) {


		// find the locations matching the given strings
		Location originLoc = getLocation(origin);
		Location destinationLoc = getLocation(destination);

		// create Delivery request
		DeliveryRequest request = new DeliveryRequest(LocalDateTime.now(), originLoc, destinationLoc, weight, volume,
				priority, duration, legs);

		// add to delivery events field
		addToAverageDeliveryTimes(origin, destination, duration, priority);
		addToAmountOfMail(origin, destination, weight, volume);
		deliveryRequests.add(request);

		// if (!initial) {
		// //log in file and add to reports
		// try {
		// writer.writeDeliveryRequest(request);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// get total cost and rev
		double cost = 0;
		double price = 0;
		for (Leg l : legs) {
			cost += l.getCost();
			price += l.getPrice();
		}

		addTotalExp(cost);
		addTotalRev(price);
		addEvent();

		return request;

	}
	public Route logTransportCostUpdate(String origin, String destination, String company, String type,
			double weightCost, double volumeCost, int maxWeight, int maxVolume, int duration, int frequency,
			DayOfWeek day, int startTime, boolean initial) {

		// find the Locations matching the given strings, if they are already in
		// the graph
		Location originLoc = getLocation(origin);
		Location destinationLoc = getLocation(destination);

		// if Locations don't exist yet, add them to the graph
		if (originLoc == null) {
			originLoc = new Location(origin);
			addLocation(originLoc);
		}
		if (destinationLoc == null) {
			destinationLoc = new Location(destination);
			addLocation(destinationLoc);
		}

		// work out Priority
		String priority;
		if (type.equals("Air")) {
			priority = "Air";
		} else {
			priority = "Standard";
		}

		// get customer price matching the route
		CustomerPrice price = null;
		price = getCustomerPrice(originLoc, destinationLoc, origin, destination, priority);

		// check if route already exists, if it does, update it
		Boolean routeExists = false;
		for (int k = 0; k < originLoc.getRoutes().size(); k++) {
			Route r = originLoc.getRoutes().get(k);
			if (r.getDestination().equals(destinationLoc) && r.getCompany().equals(company)
					&& r.getType().equals(type)) {
				r.setWeightCost(weightCost);
				r.setVolumeCost(volumeCost);
				r.setMaxWeight(maxWeight);
				r.setMaxVolume(maxVolume);
				r.setDuration(duration);
				r.setFrequency(frequency);
				r.setDay(day);
				r.setStartTime(startTime);
				routeExists = true;
			}
		}

		Route route = null;
		if (!routeExists) {
			// if it doesn't always exist, create route and add to graph
			route = new Route(originLoc, destinationLoc, company, type, priority, weightCost, volumeCost,
					maxWeight, maxVolume, duration, frequency, day, startTime, price);
			originLoc.addRoute(route);
		}

	/* Log Customer Price */
		//log in file and add to reports
		addEvent();
//		if (!initial) {
//			try {
//				writer.writeRoute(route);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		return route;
		
	}
	public CustomerPrice logCustomerPriceUpdate(String origin, String destination, String priority, double weightCost,
			double volumeCost, boolean initial) {

		// find the locations matching the given strings, if they are already in
		// the graph
		Location originLoc = getLocation(origin);
		Location destinationLoc = getLocation(destination);

		// if locations don't exist yet, add them to the graph
		if (originLoc == null) {
			originLoc = new Location(origin);
			addLocation(originLoc);
		}
		if (destinationLoc == null) {
			destinationLoc = new Location(destination);
			addLocation(destinationLoc);
		}

		// check if customer price already exists, if so, update it
		for (int i = 0; i < originLoc.getPrices().size(); i++) {
			CustomerPrice c = originLoc.getPrices().get(i);
			if (c.getDestination().equals(destinationLoc) && c.getPriority().equals(priority)) {
				c.setVolumeCost(volumeCost);
				c.setWeightCost(weightCost);

				// log in file and add to reports
				addEvent();
				// if (!initial) {
				// try {
				// writer.writeCustomerPrice(c);
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// }
				return c;
			}
		}

		// if it doesn't exist, create it, add it to the relevant Location
		CustomerPrice price;
		price = new CustomerPrice(originLoc, destinationLoc, priority, weightCost, volumeCost);
		originLoc.addPrice(price);

		// log in file and add to reports
		addEvent();
		// if (!initial) {
		// try {
		// writer.writeCustomerPrice(price);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

		return price;
	}
	public DiscontinueRoute discontinueTransportRoute(String origin, String destination, String company, String type,
			boolean initial) {

		Location originLoc = getLocation(origin);
		Location destinationLoc = getLocation(destination);

		Route toCancel = null;
		// find the matching route out of origin
		for (Route r : originLoc.getRoutes()) {
			if (r.getCompany().equals(company) && r.getDestination().getName().equals(destination)
					&& r.getType().equals(type)) {
				toCancel = r;
			}
		}

		DiscontinueRoute disconRoute = new DiscontinueRoute(originLoc, destinationLoc, company, type);
		if (toCancel != null) {
			originLoc.removeRoute(toCancel);
			// if (!initial) {
			// try {
			// writer.writeDiscontinue(disconRoute);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
			addEvent();
			return disconRoute;
		} else {
			// TODO display error
			return null;
		}

	}

	
	// GETTERS
	// Locations
	public ArrayList<Location> getLocations() {
		return locations;
	}
	public Location getLocation(String name) {
		Location location = null;
		for (Location loc : locations) {
			if (loc.getName().equals(name)) {
				location = loc;
			}
		}
		return location;
	}
	// Routes
	public ArrayList<RouteDisplay> getPossibleRoutes(String origin, String destination, double weight, double volume) {

		// find the locations matching the given strings
		Location originLoc = getLocation(origin);
		Location destinationLoc = getLocation(destination);

		// route selection
		AStar astar = new AStar(locations, originLoc, destinationLoc);
		ArrayList<ArrayList<Route>> routes = astar.twoListsOfRoutes(weight, volume);

		// set up list to pass to GUI
		ArrayList<RouteDisplay> out = new ArrayList<>();
		for (ArrayList<Route> list : routes) {

			// calculate priority
			String overallPriority = "";
			List<String> domesticCities = new ArrayList<>();
			domesticCities.add("Auckland");
			domesticCities.add("Hamilton");
			domesticCities.add("Rotorua");
			domesticCities.add("Palmerston North");
			domesticCities.add("Wellington");
			domesticCities.add("Christchurch");
			domesticCities.add("Dunedin");

			if (domesticCities.contains(origin) && domesticCities.contains(destination)) {
				overallPriority = overallPriority + "Domestic ";
			} else {
				overallPriority = overallPriority + "International ";
			}

			String priority = "Air";
			for (Route r : list) {
				if (!r.getPriority().equals("Air")) {
					priority = "Standard";
				}
			}

			overallPriority = overallPriority + priority;

			// calculate customer price
			double price = 0.0;
			for (Route k : list) {
				price += (weight * k.getPrice().getWeightCost() + volume * k.getPrice().getVolumeCost());
			}

			RouteDisplay rDisp = new RouteDisplay(overallPriority, list, price);

			// check if route is already in the list to be returned - only add
			// it if it isn't
			Boolean exists = false;
			for (RouteDisplay r : out) {
				if (r.equals(rDisp)) {
					exists = true;
				}
			}

			if (!exists) {
				out.add(rDisp);
			}
		}
		return out;
	}
	public ArrayList<Route> getRoutes(String origin, String destination){
		Location originLoc = getLocation(origin);

		ArrayList<Route> disconRoutes = new ArrayList<>();
		for (Route r : originLoc.getRoutes()) {
			if (r.getDestination().getName().equals(destination)) {
				disconRoutes.add(r);
			}
		}
		return disconRoutes;
	}
	// Customer Prices
	public CustomerPrice getCustomerPrice(Location originLoc, Location destinationLoc, String origin,
			String destination, String priority) {
		// check if there's already a price for the (origin, destination,
		// priority)
		CustomerPrice customerPrice = null;
		for (int k = 0; k < originLoc.getPrices().size(); k++) {

			if (originLoc.getPrices().get(k).getDestination().equals(destinationLoc)
					&& originLoc.getPrices().get(k).getPriority().equals(priority)) {
				customerPrice = originLoc.getPrices().get(k);
			}
		}
		// if there's no customer price, request one
		// TODO replace console input with GUI
		if (customerPrice == null) {
			return null;
		}
		return customerPrice;
	}
	// Delivery Requests
	public List<DeliveryRequest> getDeliveryRequests() {
		return deliveryRequests;
	}
	public DeliveryRequest getDeliveryDetails(String origin, String destination, double weight, double volume,
			RouteDisplay route) {

		// get duration
		int duration = route.getTotalDuration(LocalDateTime.now());
	
		// translate route list into legs
		ArrayList<Leg> legs = new ArrayList<>();
		for (Route r : route.getRoute()) {
			double freightCost = weight * r.getWeightCost() + volume * r.getVolumeCost();
			double customerPrice = weight * r.getPrice().getWeightCost() + volume * r.getPrice().getVolumeCost();
			legs.add(new Leg(r.getOrigin(), r.getDestination(), r.getType(), r.getCompany(), freightCost,
					customerPrice));
		}

		return logDeliveryRequest(LocalDateTime.now(),origin,destination, legs, weight,volume,route.getPriority(),duration, false);
	}
	
	// SETTERS + Adders
	public void addLocation(Location location) {
		locations.add(location);
	}
	
	
	// REPORTS
	// Amount of Mail
	public HashMap<Tuple, ArrayList<Double>> getAmountOfMail() {
		return amountOfMail;
	}
	public HashMap<TuplePriority, ArrayList<Integer>> getAmountOfMailDeliveryTimes() {
		return amountOfMailDeliveryTimes;
	}
	public void addToAmountOfMail(String origin, String destination, double weight, double volume) {
		boolean success = false;
		for (Tuple t : amountOfMail.keySet()) {
			if (t.getOrigin().equals(origin) && t.getDestination().equals(destination)) {
				ArrayList<Double> amountList = amountOfMail.get(t);
				double i = amountList.get(0);
				i = i + weight;
				double j = amountList.get(1);
				j = j + volume;
				double k = amountList.get(2);
				k++;
				amountList.clear();
				amountList.add(i);
				amountList.add(j);
				amountList.add(k);
				amountOfMail.put(t, amountList);
				success = true;
			}
		}
		if (!success) {
			Tuple t = new Tuple(origin, destination);
			ArrayList<Double> weightAndVolume = new ArrayList<>();
			weightAndVolume.add(weight);
			weightAndVolume.add(volume);
			weightAndVolume.add(1.0);
			amountOfMail.put(t, weightAndVolume);
		}
		mailReportPrint();
	}
	private void mailReportPrint() {
		for (Tuple t : amountOfMail.keySet()) {
			ArrayList<Double> amountList = amountOfMail.get(t);
			System.out.println(t.getOrigin() + " to " + t.getDestination() + ". Total Weight: " + amountList.get(0)
					+ " Total Volume:" + amountList.get(1) + " Total Instances: " + amountList.get(2));
		}
	}

	// Average Delivery Times
	public void addToAverageDeliveryTimes(String origin, String destination, int duration, String priority) {
		boolean success = false;
		for (TuplePriority t : amountOfMailDeliveryTimes.keySet()) {
			if (t.getOrigin().equals(origin) && t.getDestination().equals(destination)) {
				ArrayList<Integer> totalAndCount = amountOfMailDeliveryTimes.get(t);
				totalAndCount.set(0, totalAndCount.get(0) + duration);
				totalAndCount.set(1, totalAndCount.get(1) + 1);
				amountOfMailDeliveryTimes.put(t, totalAndCount);
				success = true;
			}
		}
		if (!success) {
			TuplePriority t = new TuplePriority(origin, destination, priority);
			ArrayList<Integer> totalAndCount = new ArrayList<>();
			totalAndCount.add(duration);
			totalAndCount.add(1);
			amountOfMailDeliveryTimes.put(t, totalAndCount);
		}
	}
	public Integer averageDeliveryTime(String origin, String destination, String priority) {
		for (TuplePriority t : amountOfMailDeliveryTimes.keySet()) {
			if (t.getOrigin().equals(origin) && t.getDestination().equals(destination)
					&& t.getPriority().equals(priority)) {
				ArrayList<Integer> totalAndCount = amountOfMailDeliveryTimes.get(t);
				return totalAndCount.get(0) / totalAndCount.get(1);
			}
		}
		return -1;
	}
	
	// Little Reports
	public void addTotalRev(double amount) {
		totalRev += amount;
	}
	public void addTotalExp(double amount) {
		totalExp += amount;
	}
	public void addEvent() {
		events += 1;
	}
	public double getTotalRev() {
		System.out.println("Total Revenue: $" + totalRev);
		return totalRev;
	}
	public double getTotalExp() {
		System.out.println("Total Expenditure: $" + totalExp);
		return totalExp;
	}
	public int getTotalEvents() {
		System.out.println("Total Events: " + events);
		return events;
	}
}