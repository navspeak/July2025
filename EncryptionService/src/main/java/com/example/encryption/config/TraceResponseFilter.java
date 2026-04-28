package com.example.encryption.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("!client")
public class TraceResponseFilter extends OncePerRequestFilter {

    // Dedicated logger name so log-parsing can target "ROUND_TRIP" lines precisely:
    //   grep "ROUND_TRIP" app.log | awk '{for(i=1;i<=NF;i++) if($i~/durationMs/) print $i}' | cut -d= -f2
    private static final Logger log = LoggerFactory.getLogger(TraceResponseFilter.class);

    private final Tracer tracer;

    public TraceResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String traceId = null;

        Span span = tracer.currentSpan();
        if (span != null) {
            traceId = span.context().traceId();
            response.setHeader("X-TraceId", traceId);
            response.setHeader("X-SpanId", span.context().spanId());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // For StreamingResponseBody endpoints this measures time-to-first-byte (server side),
            // not full body transfer — still the right proxy for service latency.
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("ROUND_TRIP traceId={} method={} uri={} status={} durationMs={}",
                    traceId != null ? traceId : "-",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);
        }
    }
}
