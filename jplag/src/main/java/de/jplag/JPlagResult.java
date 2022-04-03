package de.jplag;

import java.util.List;

import de.jplag.options.JPlagOptions;

/**
 * Encapsulates the results of a comparison of a set of source code submissions.
 */
public class JPlagResult {

    /**
     * List of detected comparisons whose similarity was about the specified threshold.
     */
    private List<JPlagComparison> comparisons;

    /**
     * Duration of the JPlag run in milliseconds.
     */
    private long durationInMillis;

    /**
     * Total number of submissions that have been compared.
     */
    private int numberOfSubmissions;

    /**
     * Options for the plagiarism detection run.
     */
    private JPlagOptions options;

    /**
     * 10-element array representing the similarity distribution of the detected matches.
     * <p>
     * Each entry represents the absolute frequency of matches whose similarity lies within the respective interval.
     * <p>
     * Intervals:
     * <p>
     * 0: [0% - 10%), 1: [10% - 20%), 2: [20% - 30%), ..., 9: [90% - 100%]
     */
    private int[] similarityDistribution = null;

    /**
     * Creates empty results.
     */
    public JPlagResult() {
        // No results available.
    }

    public JPlagResult(List<JPlagComparison> comparisons, long durationInMillis, int numberOfSubmissions, JPlagOptions options) {
        this.comparisons = comparisons;
        this.durationInMillis = durationInMillis;
        this.numberOfSubmissions = numberOfSubmissions;
        this.options = options;
        similarityDistribution = calculateSimilarityDistribution(comparisons);
        comparisons.sort((first, second) -> Float.compare(second.similarity(), first.similarity())); // Sort by percentage (descending).
    }

    /**
     * @return a list of all comparisons sorted by percentage (descending)
     */
    public List<JPlagComparison> getComparisons() {
        return comparisons;
    }

    /**
     * Returns the first n comparisons (sorted by percentage, descending), limited by the specified parameter.
     * @param numberOfComparisons specifies the number of requested comparisons. If set to -1, all comparisons will be returned.
     * @return a list of comparisons sorted descending by percentage.
     */
    public List<JPlagComparison> getComparisons(int numberOfComparisons) {
        if (numberOfComparisons == -1) {
            return comparisons;
        }
        return comparisons.subList(0, Math.min(numberOfComparisons, comparisons.size()));
    }

    /**
     * @return the duration of the comparison in milliseconds.
     */
    public long getDuration() {
        return durationInMillis;
    }

    public int getNumberOfSubmissions() {
        return numberOfSubmissions;
    }

    public JPlagOptions getOptions() {
        return options;
    }

    public int[] getSimilarityDistribution() {
        return similarityDistribution;
    }

    @Override
    public String toString() {
        return String.format("JPlagResult { comparisons: %d, duration: %d ms, language: %s, submissions: %d }", getComparisons().size(),
                getDuration(), getOptions().getLanguageOption(), getNumberOfSubmissions());
    }

    /**
     * Note: Before, comparisons with a similarity below the given threshold were also included in the similarity matrix.
     */
    private int[] calculateSimilarityDistribution(List<JPlagComparison> comparisons) {
        int[] similarityDistribution = new int[10];

        comparisons.stream().map(JPlagComparison::similarity).map(percent -> percent / 10).map(Float::intValue).map(index -> index == 10 ? 9 : index)
                .forEach(index -> similarityDistribution[index]++);

        return similarityDistribution;
    }
}
