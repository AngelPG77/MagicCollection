package pga.magiccollectionspring.shared.abstractions;

public interface IQueryService<TQuery, TResponse> {
    TResponse execute(TQuery query);
}