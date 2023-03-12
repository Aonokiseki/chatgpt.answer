public class ChineseSegmenter {
    private Set<String> dict;

    public ChineseSegmenter(Set<String> dict) {
        this.dict = dict;
    }

    public List<String> segment(String sentence) {
        List<String> result = new ArrayList<>();
        int maxLen = getMaxLen(dict);

        while (!sentence.isEmpty()) {
            int endIndex = Math.min(sentence.length(), maxLen);
            String token = sentence.substring(0, endIndex);

            while (!dict.contains(token)) {
                if (token.length() == 1) {
                    break;
                }
                endIndex--;
                token = sentence.substring(0, endIndex);
            }

            result.add(token);
            sentence = sentence.substring(token.length());
        }

        return result;
    }

    private int getMaxLen(Set<String> dict) {
        int maxLen = 0;
        for (String word : dict) {
            maxLen = Math.max(maxLen, word.length());
        }
        return maxLen;
    }

    public static void main(String[] args) {
        Set<String> dict = new HashSet<>();
        dict.add("我");
        dict.add("爱");
        dict.add("中国");
        dict.add("北京");
        dict.add("天安门");

        ChineseSegmenter segmenter = new ChineseSegmenter(dict);
        List<String> tokens = segmenter.segment("我爱中国北京天安门");

        System.out.println(tokens); // prints [我, 爱, 中国, 北京, 天安门]
    }
}
