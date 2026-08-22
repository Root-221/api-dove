package sn.dove.backend.dove.web;

import java.util.List;

public record DovePage<T>(List<T> items, int page, int size, int pageSize, long totalItems, long total, int totalPages) {
    public static <T> DovePage<T> of(List<T> allItems, int requestedPage, int requestedSize) {
        int page = Math.max(0, requestedPage);
        int size = Math.min(100, Math.max(1, requestedSize));
        int from = Math.min(allItems.size(), page * size);
        int to = Math.min(allItems.size(), from + size);
        long total = allItems.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new DovePage<>(allItems.subList(from, to), page, size, size, total, total, totalPages);
    }
}
