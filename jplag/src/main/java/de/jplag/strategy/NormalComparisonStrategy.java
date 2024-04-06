package de.jplag.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import de.jplag.GreedyStringTiling;
import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import de.jplag.Submission;
import de.jplag.SubmissionSet;
import de.jplag.options.JPlagOptions;

public class NormalComparisonStrategy extends AbstractComparisonStrategy {

    public NormalComparisonStrategy(JPlagOptions options, GreedyStringTiling greedyStringTiling) {
        super(options, greedyStringTiling);
    }

    @Override
    public JPlagResult compareSubmissions(SubmissionSet submissionSet) {
        boolean withBaseCode = submissionSet.hasBaseCode();
        if (withBaseCode) {
            compareSubmissionsToBaseCode(submissionSet);
        }

        List<Submission> submissions = submissionSet.getSubmissions();
        long timeBeforeStartInMillis = System.currentTimeMillis();
        Submission first, second;
        Map<Set<String>,JPlagComparison> comp_map = new HashMap<>();

        for (int i = 0; i < (submissions.size() - 1); i++) {
            first = submissions.get(i);
            if (first.getTokenList() == null) {
                continue;
            }
            for (int j = (i + 1); j < submissions.size(); j++) {
                second = submissions.get(j);
                if (second.getTokenList() == null) {
                    continue;
                }
                compareSubmissions(first, second, withBaseCode).ifPresent(it -> {
                    Set<String> pair = it.getStudentPair();
                    JPlagComparison best_comparison = comp_map.get(pair);
                    if (best_comparison == null || it.similarity() > best_comparison.similarity()) {
                      // If exists or is better, (re)place this comparison
                      comp_map.put(pair,it);
                    }
                });
            }
        }


        List<JPlagComparison> comparisons = new ArrayList<JPlagComparison>(comp_map.values());
        long durationInMillis = System.currentTimeMillis() - timeBeforeStartInMillis;
        return new JPlagResult(comparisons, durationInMillis, comparisons.size(), options);
    }

}
