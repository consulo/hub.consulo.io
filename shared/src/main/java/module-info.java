/**
 * @author VISTALL
 * @since 01/05/2023
 */
open module consulo.hub.shared
{
	requires transitive com.fasterxml.jackson.annotation;
	requires transitive jakarta.persistence;
	requires transitive commons.lang3;
	requires transitive spring.security.core;
	requires transitive spring.data.commons;

	exports consulo.hub.shared;
	exports consulo.hub.shared.auth;
	exports consulo.hub.shared.auth.domain;
	exports consulo.hub.shared.auth.oauth2.domain;
	exports consulo.hub.shared.base;
	exports consulo.hub.shared.errorReporter.domain;
	exports consulo.hub.shared.repository;
	exports consulo.hub.shared.repository.util;
	exports consulo.hub.shared.statistics.domain;
	exports consulo.hub.shared.storage.domain;
	exports consulo.hub.shared.util;
	exports consulo.hub.shared.repository.domain;
	exports consulo.hub.shared.auth.rest;
}