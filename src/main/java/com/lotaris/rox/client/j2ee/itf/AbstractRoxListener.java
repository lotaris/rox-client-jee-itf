package com.lotaris.rox.client.j2ee.itf;

import com.lotaris.j2ee.itf.annotations.NoRollback;
import com.lotaris.j2ee.itf.listeners.DefaultListener;
import com.lotaris.j2ee.itf.model.Description;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import com.lotaris.rox.annotations.TestFlag;
import com.lotaris.rox.common.config.Configuration;
import com.lotaris.rox.common.utils.Inflector;
import com.lotaris.rox.common.model.v1.Test;
import com.lotaris.rox.common.model.v1.ModelFactory;
import com.lotaris.rox.utils.CollectionHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Shared code to handle the test results in different ROX related listeners
 *
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public abstract class AbstractRoxListener extends DefaultListener {
	/**
	 * Rox configuration
	 */
	protected static final Configuration configuration = Configuration.getInstance();

	/**
	 * Default category when none is specified
	 */
	protected static final String DEFAULT_CATEGORY	= "Integration";
	protected static final String DEFAULT_TAG				= "itf";
	
	/**
	 * Category used only if set and no other specified
	 */
	private String category;
	
	public AbstractRoxListener() {}
	
	public AbstractRoxListener(String category) {
		this.category = category;
	}
	
	/**
	 * Try to retrieve the {@link RoxableTest} annotation of the test method
	 * 
	 * @param description The representation of the test
	 * @return The annotation found, or null if not found
	 * @throws NoSuchMethodException 
	 */
	protected RoxableTest getMethodAnnotation(Description description) {
		return description.getMethod().getAnnotation(RoxableTest.class);
	}
	
	/**
	 * Try to retrieve the {@link RoxableTestClass} annotation of the test class
	 * 
	 * @param description The representation of the test
	 * @return The annotation found, or null if not found
	 */
	protected RoxableTestClass getClassAnnotation(Description description) {
		return description.getMethod().getClass().getAnnotation(RoxableTestClass.class);
	}
	
	/**
	 * Create a test based on the different information gathered from class, method and description
	 * 
	 * @param description jUnit test description
	 * @param methodAnnotation Method annotation
	 * @param classAnnotation Class annotation
	 * @param passed Test passing or not
	 * @param message Message associated to the test result
	 * @return The test created from all the data available
	 */
	protected Test createTest(Description description, RoxableTest methodAnnotation, RoxableTestClass classAnnotation) {
		Map<String, String> data = new HashMap<>();
		if (description.getData() != null || description.getData().size() > 0) {
			data.putAll(description.getData());
		}
		
		data.put("package", description.getMethod().getClass().getPackage().getName());
		data.put("class", description.getMethod().getClass().getSimpleName());
		data.put("method", description.getSimpleName());
		data.put("rollback", "" + (description.getMethod().getAnnotation(NoRollback.class) == null));
		
		return ModelFactory.createTest(
			methodAnnotation.key(),
			getName(description, methodAnnotation),
			getCategory(classAnnotation, methodAnnotation, description),
			description.getEndDate(),
			description.getDuration(),
			description.getMessage(),
			description.isPassed(),
			TestFlag.flagsValue(Arrays.asList(methodAnnotation.flags())),
			getTags(methodAnnotation, classAnnotation),
			getTickets(methodAnnotation, classAnnotation),
			data
		);
	}
	
	/**
	 * Retrieve a name from a test
	 * 
	 * @param description The description of the test
	 * @param mAnnotation The method annotation
	 * @return The name retrieved
	 */
	private String getName(Description description, RoxableTest mAnnotation) {
		if (mAnnotation == null || mAnnotation.name() == null || mAnnotation.name().isEmpty()) {
			return Inflector.getHumanName(description.getSimpleName());
		}
		else {
			return mAnnotation.name();
		}
	} 
	
	/**
	 * Retrieve the category to apply to the test
	 * 
	 * @param classAnnotation The roxable class annotation to get the override category
	 * @param methodAnnotation The roxable annotation to get the override category
	 * @param description The description that correspond to the test
	 * @return The category found
	 */
	protected String getCategory(RoxableTestClass classAnnotation, RoxableTest methodAnnotation, Description description) {
		if (methodAnnotation != null && methodAnnotation.category() != null && !methodAnnotation.category().isEmpty()) {
			return methodAnnotation.category();
		}
		else if (classAnnotation != null && classAnnotation.category() != null && !classAnnotation.category().isEmpty()) {
			return classAnnotation.category();
		}
		else if (configuration.getCategory() != null && !configuration.getCategory().isEmpty()) {
			return configuration.getCategory();
		}
		else if (category != null) {
			return category;
		}
		else {
			return DEFAULT_CATEGORY;
		}
	}

	/**
	 * Compute the list of tags associated for a test
	 * 
	 * @param methodAnnotation The method annotation to get info
	 * @param classAnnotation The class annotation to get info
	 * @return The tags associated to the test
	 */
	private Set<String> getTags(RoxableTest methodAnnotation, RoxableTestClass classAnnotation) {
		Set<String> tags = CollectionHelper.getTags(configuration.getTags(), methodAnnotation, classAnnotation);
		
		if (!tags.contains(DEFAULT_TAG)) {
			tags.add(DEFAULT_TAG);
		}
		
		return tags;
	}

	/**
	 * Compute the list of tickets associated for a test
	 * 
	 * @param methodAnnotation The method annotation to get info
	 * @param classAnnotation The class annotation to get info
	 * @return The tickets associated to the test
	 */
	private Set<String> getTickets(RoxableTest methodAnnotation, RoxableTestClass classAnnotation) {
		return CollectionHelper.getTickets(configuration.getTickets(), methodAnnotation, classAnnotation);
	}
}