import java.util.List;
import java.util.stream.Collectors;

class FuzzySearcher {

    public List<String> search(String query, List<String> items) {
        return items.stream()
                .filter(item -> fuzzyMatch(query.toLowerCase(), item.toLowerCase()))
                .sorted((a, b) -> Integer.compare(
                        fuzzyScore(query.toLowerCase(), a.toLowerCase()),
                        fuzzyScore(query.toLowerCase(), b.toLowerCase())))
                .collect(Collectors.toList());
    }

    private boolean fuzzyMatch(String query, String item) {
        int queryIndex = 0;
        int itemIndex = 0;

        while (queryIndex < query.length() && itemIndex < item.length()) {
            if (query.charAt(queryIndex) == item.charAt(itemIndex)) {
                queryIndex++;
            }
            itemIndex++;
        }

        return queryIndex == query.length();
    }

    private int fuzzyScore(String query, String item) {
        // Simple scoring: lower score is better
        // Exact match gets best score
        if (item.contains(query)) {
            return item.indexOf(query);
        }

        // Fuzzy match scoring
        int score = 0;
        int queryIndex = 0;

        for (int i = 0; i < item.length() && queryIndex < query.length(); i++) {
            if (query.charAt(queryIndex) == item.charAt(i)) {
                queryIndex++;
            } else {
                score++;
            }
        }

        return score;
    }
}
