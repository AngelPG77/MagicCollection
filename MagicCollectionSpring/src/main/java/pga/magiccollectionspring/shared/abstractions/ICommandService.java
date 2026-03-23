package pga.magiccollectionspring.shared.abstractions;

public interface ICommandService<TCommand, TResponse> {
    TResponse execute(TCommand command);
}