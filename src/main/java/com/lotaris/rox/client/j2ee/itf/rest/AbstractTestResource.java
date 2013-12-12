package com.lotaris.rox.client.j2ee.itf.rest;

import com.lotaris.j2ee.itf.TestController;
import com.lotaris.j2ee.itf.filters.Filter;
import com.lotaris.j2ee.itf.listeners.Listener;
import com.lotaris.rox.client.j2ee.itf.RoxFilter;
import com.lotaris.rox.client.j2ee.itf.RoxListener;
import com.lotaris.rox.common.config.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expose the method to start the integration tests
 * 
 * @author Laurent Prévost, laurent.prevost@lotaris.com
 */
public abstract class AbstractTestResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestResource.class);
	
	/**
	 * Start the test through the integration test controller
	 * @param filters Filters to apply if necessary
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response run(
		@QueryParam("filters") String filters, 
		@QueryParam("seed") Long seed, 
		@QueryParam("category") String category, 
		@QueryParam("options") String options) {
		
		// Parse additional options
		parseOptions(options);
		
		// Logging
		if (LOGGER.isDebugEnabled()) {
			StringBuilder message = new StringBuilder();
			
			if (filters != null && !filters.isEmpty()) {
				message.append("Filters [").append(filters).append("]");
			}
			
			if (seed != null) {
				message.append("Seed[").append(seed).append("]");
			}

			LOGGER.debug(message.toString());
		}

		// Retrieve the test controller
		TestController testController = getController();
		
		Map<String, Listener> roxListeners = new HashMap<String, Listener>();
		Map<String, Filter> roxFilters = new HashMap<String, Filter>();

		RoxListener defaultListener;
		
		if (category != null && !category.isEmpty()) {
			defaultListener = new RoxListener(category);
		}
		else {
			defaultListener = new RoxListener();
		}
	
		// To manage the filters
		List<String> finalFilters = new ArrayList<String>();
		if (filters != null && !filters.isEmpty()) {
			finalFilters.addAll(Arrays.asList(filters.split(",")));			
		}
		
		// Add more filters
		finalFilters.addAll(getAdditionalFilters());
		
		// Configure filters and default listener
		roxFilters.put("roxFilter", new RoxFilter(finalFilters.toArray(new String[finalFilters.size()])));
		roxListeners.put("roxListener", defaultListener);
		
		// Add more listeners
		for (Entry<String, Listener> listener : getAdditionalListeners(category).entrySet()) {
			roxListeners.put(listener.getKey(), listener.getValue());
		}
		
		// Retrieve seed from configuration
		if (Configuration.getInstance().getGeneratorSeed() != null) {
			seed = Configuration.getInstance().getGeneratorSeed();
		}
		
		// Generate default seed
		else if (seed == null) {
			seed = System.currentTimeMillis();
		}
		
		// Run the integration tests
		LOGGER.info("Generator seed: {}", testController.run(roxFilters, roxListeners, seed));
		
		return Response.ok().build();
	}
	
	/**
	 * @return Retrieve the test controller
	 */
	public abstract TestController getController();
	
	public abstract void parseOptions(String options);
	
	/**
	 * @return More filters to add
	 */
	public List<String> getAdditionalFilters() {
		return new ArrayList<String>();
	}
	
	/**
	 * @return More listeners to use
	 */
	public Map<String, Listener> getAdditionalListeners(String category) {
		return new HashMap<String, Listener>();
	}
}
