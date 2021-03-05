/*
 * Copyright 2012-2020 the original author or authors.
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

package io.spring.start.site.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import io.spring.initializr.web.support.SaganInitializrMetadataUpdateStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

/**
 * An {@link InitializrMetadataUpdateStrategy} that performs additional filtering of
 * versions available on spring.io.
 *
 * @author Stephane Nicoll
 */
public class StartInitializrMetadataUpdateStrategy extends SaganInitializrMetadataUpdateStrategy {

	public StartInitializrMetadataUpdateStrategy(RestTemplate restTemplate, ObjectMapper objectMapper) {
		super(restTemplate, objectMapper);
	}

	@Override
	protected List<DefaultMetadataElement> fetchSpringBootVersions(String url) {
		List<DefaultMetadataElement> versions = super.fetchSpringBootVersions(url);
		return versions.stream().filter(this::isCompatibleVersion).collect(Collectors.toList());
	}

	private boolean isCompatibleVersion(DefaultMetadataElement versionMetadata) {
		Version version = Version.parse(versionMetadata.getId());
		return (version.getMajor() >= 2 && version.getMinor() > 2);
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		String url = current.getConfiguration().getEnv().getSpringBootMetadataUrl();
		List<DefaultMetadataElement> bootVersions = customeSpringBootVersions();
		if (bootVersions != null && !bootVersions.isEmpty()) {
			if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
				// No default specified
				bootVersions.get(0).setDefault(true);
			}
			current.updateSpringBootVersions(bootVersions);
		}
		return current;
	}

	private List<DefaultMetadataElement> customeSpringBootVersions() {
		List<DefaultMetadataElement> res = new ArrayList<>();
		res.add(new DefaultMetadataElement("2.1.8.RELEASE", "2.1.18.RELEASE", true));
		res.add(new DefaultMetadataElement("2.3.9.RELEASE", "2.3.9.RELEASE", false));
		res.add(new DefaultMetadataElement("2.4.3", "2.4.3", false));
		return res;
	}
}
