package pga.magiccollectionspring.shared.abstractions;

import java.util.concurrent.CompletableFuture;

public interface IQueryServiceAsync<TQuery, TResponse> {
    CompletableFuture<TResponse> execute(TQuery query);
}