package org.jorgetrujillo.elitebot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class CommonConfig {

  @Primary
  @Bean
  static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper()
    objectMapper.findAndRegisterModules()
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)  // write them as strings
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    SimpleModule simpleModule = new SimpleModule()
    simpleModule.addDeserializer(String, new StringDeserializer())
    objectMapper.registerModule(simpleModule)

    return objectMapper
  }

  @Primary
  @Bean
  RestTemplate getRestTemplate() {
    RestTemplate restTemplate = new RestTemplate()
    replaceJacksonMessageConverter(restTemplate)
    return restTemplate
  }

  @SuppressWarnings('Instanceof')
  private void replaceJacksonMessageConverter(RestTemplate restTemplate) {
    restTemplate.getMessageConverters().eachWithIndex { HttpMessageConverter<?> entry, int i ->
      if (entry instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter()
        jacksonConverter.setObjectMapper(getObjectMapper())

        restTemplate.getMessageConverters().set(i, jacksonConverter)
      }
    }
  }
}
