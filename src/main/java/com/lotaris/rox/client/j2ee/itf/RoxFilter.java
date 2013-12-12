package com.lotaris.rox.client.j2ee.itf;

import com.lotaris.j2ee.itf.filters.Filter;
import com.lotaris.j2ee.itf.model.Description;
import com.lotaris.rox.core.filters.FilterUtils;

/**
 * The integration test framework listener is used to send the result to the server
 *
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class RoxFilter implements Filter {
//	private static final Logger LOGGER = LoggerFactory.getLogger(RoxFilter.class);
	
	/**
	 * Define the filters to apply
	 */
	private String[] filters;
	
	public RoxFilter(String[] filters) {
		this.filters = filters;
	}
	
	@Override
	public boolean isRunnable(Description description) {
		// The test is deactivated
		if (!description.isRunnable()) {
			return false;
		}
		
		// Delegate the filtering to the filter utils
		else {
			return FilterUtils.isRunnable(description.getMethod(), filters);
		}
	}
}