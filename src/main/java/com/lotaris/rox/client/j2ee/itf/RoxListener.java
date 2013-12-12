package com.lotaris.rox.client.j2ee.itf;

import com.lotaris.j2ee.itf.model.Description;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import com.lotaris.rox.common.config.RoxRuntimeException;
import com.lotaris.rox.common.model.v1.Payload;
import com.lotaris.rox.common.model.v1.Test;
import com.lotaris.rox.common.model.v1.ModelFactory;
import com.lotaris.rox.core.connector.Connector;
import com.lotaris.rox.core.storage.FileStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to send results to ROX Server
 *
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class RoxListener extends AbstractRoxListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoxListener.class);
	
	/**
	 * Store the list of the tests executed
	 */
	private List<Test> results = new ArrayList<>();

	public RoxListener() {}
	
	public RoxListener(String category) {
		super(category);
	}
	
	@Override
	public void testRunEnd() {
		super.testRunEnd();
		
		// Ensure there is nothing to do when ROX is disabled
		if (configuration.isDisabled()) {
			return;
		}
		
		if (!results.isEmpty()) {
			try {
				publishTestPayload();
			} catch (RoxRuntimeException e) {
				LOGGER.warn("Could not publish or save test payload", e);
			}
		}	
	}

	@Override
	public void testEnd(Description description) {
		super.testEnd(description);

		// Ensure there is nothing to do when ROX is disabled
		if (configuration.isDisabled()) {
			return;
		}
		
		RoxableTest methodAnnotation = getMethodAnnotation(description);
		RoxableTestClass cAnnotation = getClassAnnotation(description);

		if (methodAnnotation != null) {
			if (!methodAnnotation.key().isEmpty()) {
				results.add(createTest(description, methodAnnotation, cAnnotation));
			}
			else {
				LOGGER.warn("@{} annotation is present but the key is not configured.", RoxableTest.class.getSimpleName());
			}
		} 
		else {
			LOGGER.warn("@{} annotation is missing on method name: {}", RoxableTest.class.getSimpleName(), description.getName());
		}
	}

	private void publishTestPayload() {
		if (configuration.isPublish() || configuration.isSave()) {
			Payload payload = ModelFactory.createPayload(
					ModelFactory.createTestRun(
					configuration.getProjectApiId(),
					configuration.getProjectVersion(),
					endDate,
					endDate - startDate,
					configuration.getGroup(),
					configuration.getUid(
					getCategory(null, null, null),
					configuration.getProjectApiId(),
					configuration.getProjectVersion()),
					results));

			try {
				if (configuration.isSave()) {
					new FileStore(configuration).save(payload);
				}

				if (configuration.isPublish()) {
					new Connector(configuration).send(payload);
				}
			} catch (IOException ioe) {
				LOGGER.warn("Unable to save the payload or to send it to ROX", ioe);
			}
		}
	}
}