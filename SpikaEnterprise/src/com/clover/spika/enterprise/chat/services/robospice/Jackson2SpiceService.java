package com.clover.spika.enterprise.chat.services.robospice;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Application;
import android.app.Notification;

import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.json.jackson.JacksonObjectPersisterFactory;

public class Jackson2SpiceService extends SpringAndroidSpiceService {

	// private static final int WEBSERVICES_TIMEOUT = 10000;

	@Override
	public RestTemplate createRestTemplate() {

		RestTemplate restTemplate = new RestTemplate() {

			@Override
			protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
				ClientHttpRequest request = super.createRequest(url, method);
				HttpHeaders headers = request.getHeaders();
				headers.setAcceptEncoding(ContentCodingType.ALL);
				return request;
			}
		};

		// bug on http connection for Android < 2.2
		// http://android-developers.blogspot.fr/2011/09/androids-http-clients.html
		// but still a problem for upload with Spring-android on android 4.1
		// System.setProperty("http.keepAlive", "false");

		// // set timeout for requests
		// ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		// if (factory instanceof HttpComponentsClientHttpRequestFactory) {
		// HttpComponentsClientHttpRequestFactory advancedFactory =
		// (HttpComponentsClientHttpRequestFactory) factory;
		// advancedFactory.setConnectTimeout(WEBSERVICES_TIMEOUT);
		// advancedFactory.setReadTimeout(WEBSERVICES_TIMEOUT);
		// } else if (factory instanceof SimpleClientHttpRequestFactory) {
		// SimpleClientHttpRequestFactory advancedFactory =
		// (SimpleClientHttpRequestFactory) factory;
		// advancedFactory.setConnectTimeout(WEBSERVICES_TIMEOUT);
		// advancedFactory.setReadTimeout(WEBSERVICES_TIMEOUT);
		// }

		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
		// StringHttpMessageConverter stringHttpMessageConverter = new
		// StringHttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.TEXT_HTML);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);

		// stringHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
		jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

		final List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();

		listHttpMessageConverters.add(jsonConverter);
		listHttpMessageConverters.add(formHttpMessageConverter);
		// listHttpMessageConverters.add(stringHttpMessageConverter);

		restTemplate.setMessageConverters(listHttpMessageConverters);

		return restTemplate;
	}

	@Override
	public CacheManager createCacheManager(Application application) throws CacheCreationException {

		CacheManager cacheManager = new CacheManager();
		JacksonObjectPersisterFactory jacksonObjectPersisterFactory = new JacksonObjectPersisterFactory(application);
		cacheManager.addPersister(jacksonObjectPersisterFactory);

		return cacheManager;
	}
	
	@Override
	public Notification createDefaultNotification() {
		return null;
	}

}
