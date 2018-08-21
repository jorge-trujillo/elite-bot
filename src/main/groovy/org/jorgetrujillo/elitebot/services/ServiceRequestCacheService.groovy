package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.springframework.stereotype.Service

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class ServiceRequestCacheService {

  private static final int CACHE_TTL_SECONDS = 20*60

  Map<ServiceRequest, CacheEntry> requestCache = new ConcurrentHashMap<ServiceRequest, CacheEntry>()

  String getEntry(ServiceRequest serviceRequest) {
    CacheEntry cacheEntry = requestCache.get(serviceRequest)

    if (cacheEntry && cacheEntry.created > Instant.now() + Duration.ofSeconds(CACHE_TTL_SECONDS)) {
      return cacheEntry
    }

    return null
  }

  void saveEntry(ServiceRequest serviceRequest, String response) {
    requestCache.put(serviceRequest, new CacheEntry(response))
  }

  static class CacheEntry {
    String response
    Instant created

    CacheEntry(String response) {
      this.response = response
      created = Instant.now()
    }
  }
}
