package cern.lhc.commons.web.property;

import reactor.core.publisher.Flux;

public interface Source<T> {

    T getLatest();

    Flux<T> asStream();

}
