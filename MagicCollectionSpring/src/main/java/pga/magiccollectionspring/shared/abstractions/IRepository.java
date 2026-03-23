package pga.magiccollectionspring.shared.abstractions;

import java.util.List;
import java.util.Optional;

public interface IRepository<T, ID> {
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
    List<T> findAll();
}