package io.felipepoliveira.jserializer.plugins.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.felipepoliveira.jserializer.JSerializer;
import io.felipepoliveira.jserializer.json.JfoObject;
import io.felipepoliveira.jserializer.json.JsonStructure;

@Component
public class JSerializationJsonMessageConverter implements HttpMessageConverter<Object>{
	
	private String defaultCharset = "utf-8";
	
	private String filterHeaderName = "X-Filter";
	
	private JfoObject jfoObject = null;
	
	
	public String getDefaultCharset() {
		return defaultCharset;
	}
	
	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}
	
	public String getFilterHeaderName() {
		return filterHeaderName;
	}

	public void setFilterHeaderName(String filterHeaderName) {
		this.filterHeaderName = filterHeaderName;
	}
	
	public static HttpServletRequest getCurrentHttpRequest(){
	    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
	    if (requestAttributes instanceof ServletRequestAttributes) {
	        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
	        return request;
	    }
	    return null;
	}
	
	private String readInputStream(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder str = new StringBuilder();
		String linha = null;
		while((linha = reader.readLine()) != null) {
			str.append(linha);
		}
		reader.close();
		
		return str.toString();
	}
	
	private void loadJfoFromRequestHeader(HttpServletRequest request) {
		String filterHeader = request.getHeader(filterHeaderName);
		this.jfoObject = null;
		
		//Check if the header was given by the client
		if(filterHeader != null) {
			//Check if the header is on JFO mode
			if(filterHeader.startsWith("JFO ")) {
				//Get the JFO part from the header
				String jfo = filterHeader.substring(4);
				
				this.jfoObject = JSerializer.json().parseJfo(jfo);
			}
		}
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		
		if(mediaType.getType().equals("text")) {
			return true;
		}
		if(mediaType.getSubtype().equals("json")) {
			return true;
		}
		else {
			return false;
		}
		
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		if(mediaType == null) {
			return true;
		}
		if(mediaType.getType().equals("text")) {
			return true;
		}
		if(mediaType.getSubtype().equals("json")) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		
		return supportedMediaTypes;
	}

	@Override
	public Object read(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		
		
		
		
		//Try to parse the json
		try {
						
			JsonStructure json = JSerializer.json().parse(readInputStream(inputMessage.getBody()));
			
			if(json.isJsonArray()) {
				return json.asJsonArray().to(clazz);
			}else{
				return json.asJsonObject().to(clazz);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void write(Object t, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		
		try {
			loadJfoFromRequestHeader(getCurrentHttpRequest());
		} catch (Exception e) {}
		
		
		outputMessage.getHeaders().add("content-type", "application/json;" + defaultCharset);
		if((t instanceof String)) {
			outputMessage.getBody().write(t.toString().getBytes());
		}
		else {
			outputMessage.getBody().write(JSerializer.json().withJfo(jfoObject).serialize(t).toString().getBytes());
		}
		
	}

}
