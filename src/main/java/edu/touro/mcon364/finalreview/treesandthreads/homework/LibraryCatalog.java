package edu.touro.mcon364.finalreview.treesandthreads.homework;

import edu.touro.mcon364.finalreview.treesandthreads.model.Book;
import java.util.*;
import java.util.stream.*;

/**
 * Homework 1 - Library Catalog (TreeMap + TreeSet + Streams)
 *
 * Scenario: a library stores books. Each book has a title, author, and
 * publication year. The catalog must answer several questions in sorted order.
 *
 * Before coding, think about:
 * - Which structure gives us books sorted by title automatically?
 * - Should the author-to-books index use a List or a Set inside the map?
 *   What happens if the same book appears twice?
 * - What does NavigableMap.headMap give us, and when would we use it?
 *
 * Requirements:
 * - The constructor receives the list of books to index.
 * - buildTitleIndex() returns a TreeMap keyed by title for O(log n) exact lookups.
 * - buildAuthorIndex() returns a TreeMap grouping books by author; books for each
 *   author are sorted by title.
 * - getBooksPublishedBefore(year) returns all books published strictly before
 *   the given year, sorted by title.
 * - getAuthorsWithMoreThan(n) returns a sorted list of author names who have
 *   more than n books in the catalog.
 * - findByTitlePrefix(prefix) returns all books whose title starts with the
 *   given string, alphabetically. Use NavigableMap range operations.
 *
 * Do not use explicit loops. Use streams and collectors.
 */
public class LibraryCatalog {

    private final List<Book> books;

    public LibraryCatalog(List<Book> books) {
        // TODO: validate non-null, store a defensive copy
        this.books = List.copyOf(Objects.requireNonNull(books, "Books list cannot be null"));
    }

    /**
     * Returns a TreeMap keyed by book title for O(log n) exact lookups.
     * If two books share a title, keep only one (your choice which).
     *
     */
    public TreeMap<String, Book> buildTitleIndex() {
        // TODO
        return books.stream()
                .collect(Collectors.toMap(
                        Book::title,                // Key mapper: use the book title
                        book -> book,               // Value mapper: use the book object itself
                        (existing, replacement) -> existing, // Merge function: if duplicate title, keep the first one found
                        TreeMap::new                // Map factory: explicitly request a TreeMap
                ));
    }

    /**
     * Returns a TreeMap grouping books by author; each author maps to a
     * TreeSet of their books sorted by title.
     */
    public TreeMap<String, TreeSet<Book>> buildAuthorIndex() {
        // TODO
        return books.stream()
                .collect(Collectors.groupingBy(
                        Book::author,           // Key: Author Name
                        TreeMap::new,           // Map Type: TreeMap (keeps authors sorted A-Z)
                        Collectors.toCollection(TreeSet::new) // Value: TreeSet (keeps books sorted by Title)
                ));
    }

    /**
     * Returns all books published strictly before the given year, sorted by title.
     *
     */
    public List<Book> getBooksPublishedBefore(int year) {
        // TODO
        return books.stream()
                .filter(book -> book.year() < year)
                .sorted(Comparator.comparing(Book::title))
                .toList();
    }

    /**
     * Returns a sorted list of author names who have more than n books in this catalog.
     *
     */
    public List<String> getAuthorsWithMoreThan(int n) {
        // TODO
        // 1. Count books per author: Map<String, Long>
        Map<String, Long> authorCounts = books.stream()
                .collect(Collectors.groupingBy(
                        Book::author,
                        Collectors.counting()
                ));

        // 2. Filter map entries, sort by author name, and grab the keys
        return authorCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > n)
                .map(Map.Entry::getKey)
                .sorted() // Strings sort alphabetically by default
                .toList();
    }

    /**
     * Returns all books whose title starts with the given prefix, alphabetically.
     *
     */
    public List<Book> findByTitlePrefix(String prefix) {
        // TODO
        if (prefix == null || prefix.isEmpty()) {
            return List.of();
        }

        // 1. Get our O(log n) sorted title index map
        TreeMap<String, Book> titleIndex = buildTitleIndex();

        // 2. Define the starting boundary (e.g., "Java")
        String startKey = prefix;

        // 3. Define the exclusive ending boundary (e.g., "Javb")
        // We change the last character of the prefix to the next character in the Unicode alphabet
        String endKey = prefix.substring(0, prefix.length() - 1)
                + (char) (prefix.charAt(prefix.length() - 1) + 1);

        // 4. Slice the map view between [startKey, endKey) and pull the values
        return titleIndex.subMap(startKey, true, endKey, false)
                .values()
                .stream()
                .toList();
    }
}

