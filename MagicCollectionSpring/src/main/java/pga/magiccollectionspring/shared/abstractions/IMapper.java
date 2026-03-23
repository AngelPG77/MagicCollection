package pga.magiccollectionspring.shared.abstractions;

import java.util.List;

public interface IMapper<TSource, TTarget> {
    TTarget map(TSource source);
    default List<TTarget> mapList(List<TSource> sources) {
        return sources.stream().map(this::map).toList();
    }
}