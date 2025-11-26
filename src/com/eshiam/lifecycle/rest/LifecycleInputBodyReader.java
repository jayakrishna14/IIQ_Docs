package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.utils.LifecycleUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class LifecycleInputBodyReader implements MessageBodyReader<LifecycleInput> {

    // Using LifecycleUtils.jsonStreamToMap instead of local Gson instance
    private static final Log log = LogFactory.getLog(LifecycleInputBodyReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        boolean readable = LifecycleInput.class.isAssignableFrom(type);
        log.debug("LifecycleInputBodyReader.isReadable? " + readable + "; type=" + type.getName());
        return readable;
    }

    @Override
    public LifecycleInput readFrom(Class<LifecycleInput> type, Type genericType, Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws java.io.IOException, WebApplicationException {
        try {
            Map<String, Object> json = LifecycleUtils.jsonStreamToMap(entityStream);
            log.debug("LifecycleInputBodyReader.readFrom: payload keys=" + (json != null ? json.keySet() : "null")
                    + " using classloader=" + this.getClass().getClassLoader());
            LifecycleInput input = LifecycleUtils.mapToLifecycleInput(json);
            log.debug("LifecycleInputBodyReader.readFrom: created LifecycleInput (identityName)="
                    + (input != null ? input.getIdentityName() : "null") + " (classloader="
                    + input.getClass().getClassLoader() + ")");
            return input;
        } catch (Exception e) {
            log.error("LifecycleInputBodyReader.readFrom: error parsing input", e);
            throw e;
        }
    }
}
