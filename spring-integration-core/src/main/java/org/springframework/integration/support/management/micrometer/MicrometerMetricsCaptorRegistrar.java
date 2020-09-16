/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.support.management.micrometer;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * An {@link ImportBeanDefinitionRegistrar} to conditionally add a {@link MicrometerMetricsCaptor}
 * bean when {@code io.micrometer.core.instrument.MeterRegistry} is present in classpath and
 * no {@link MicrometerMetricsCaptor#MICROMETER_CAPTOR_NAME} bean present yet.
 *
 * @author Artem Bilan
 *
 * @since 5.2.9
 */
public class MicrometerMetricsCaptorRegistrar implements ImportBeanDefinitionRegistrar {

	private static final Class<?> METER_REGISTRY_CLASS;

	static {
		Class<?> aClass = null;
		try {
			aClass = ClassUtils.forName("io.micrometer.core.instrument.MeterRegistry",
					ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException e) {
			// Ignore - no Micrometer in classpath
		}
		METER_REGISTRY_CLASS = aClass;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (METER_REGISTRY_CLASS != null
				&& !registry.containsBeanDefinition(MicrometerMetricsCaptor.MICROMETER_CAPTOR_NAME)) {
			String[] beanNamesForType =
					((ListableBeanFactory) registry).getBeanNamesForType(METER_REGISTRY_CLASS, false, false);
			for (String beanName : beanNamesForType) {
				registry.registerBeanDefinition(MicrometerMetricsCaptor.MICROMETER_CAPTOR_NAME,
						BeanDefinitionBuilder.genericBeanDefinition(MicrometerMetricsCaptor.class)
								.addConstructorArgReference(beanName)
								.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
								.getBeanDefinition());
				return;
			}

		}
	}

}
