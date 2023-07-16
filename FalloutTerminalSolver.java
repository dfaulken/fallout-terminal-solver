import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FalloutTerminalSolver {
  private static final String PASSWORDS_FILE = "passwords.txt";
  private final int wordLength;
  private final List<String> words;

  private boolean shouldQuit;

  private Map<String, Map<Integer, List<String>>> commonLettersMap;
  private Map<String, Integer> wordScores;

  private FalloutTerminalSolver(int wordLength, List<String> words) {
    this.shouldQuit = false;
    this.wordLength = wordLength;
    this.words = words;
    for(String word: words) {
      if(word.length() != wordLength) {
        throw new IllegalArgumentException(String.format("Input word %s is not of length %d", word, wordLength));
      }
    }
  }

  private boolean shouldQuit() {
    return shouldQuit;
  }

  private void createCommonLettersMap() {
    commonLettersMap = new TreeMap<>();
    for(int i = 0; i < words.size() - 1; i++) {
      String word1 = words.get(i);
      for(int j = i + 1; j < words.size(); j++) {
        String word2 = words.get(j);
        addToCommonLettersMap(word1, word2, getNumberOfCommonLetters(word1, word2));
      }
    }
  }

  private int getNumberOfCommonLetters(String word1, String word2) {
    int lettersInCommon = 0;
    for(int i = 0; i < wordLength; i++){
      if(word1.getBytes()[i] == word2.getBytes()[i]) {
        lettersInCommon++;
      }
    }
    return lettersInCommon;
  }

  private void addToCommonLettersMap(String word1, String word2, int numberOfCommonLetters) {
    commonLettersMap.putIfAbsent(word1, new TreeMap<>());
    commonLettersMap.putIfAbsent(word2, new TreeMap<>());
    commonLettersMap.get(word1).putIfAbsent(numberOfCommonLetters, new ArrayList<>());
    commonLettersMap.get(word2).putIfAbsent(numberOfCommonLetters, new ArrayList<>());
    commonLettersMap.get(word1).get(numberOfCommonLetters).add(word2);
    commonLettersMap.get(word2).get(numberOfCommonLetters).add(word1);
  }

  private void printCommonLettersMap() {
    for(String password : commonLettersMap.keySet()) {
      System.out.println(password + ":");
      Map<Integer, List<String>> passwordMap = commonLettersMap.get(password);
      for(Integer commonLetterCount : passwordMap.keySet()) {
        StringJoiner joiner = new StringJoiner(", ");
        for(String passwordWithCommonLetters : passwordMap.get(commonLetterCount)) {
          joiner.add(passwordWithCommonLetters);
        }
        System.out.printf("  %d: %s\n", commonLetterCount, joiner);
      }
    }
  }

  private void computeWordScores() {
    wordScores = new HashMap<>();
    for(String password : commonLettersMap.keySet()) {
      int score = commonLettersMap
        .get(password)
        .entrySet()
        .stream()
        .mapToInt(entry -> entry.getKey() * entry.getValue().size())
        .sum();
      wordScores.put(password, score);
    }
  }

  private String getHighestScoringWord() {
    Optional<Entry<String, Integer>> maybeHighestScoringWord = wordScores.entrySet().stream().max(Entry.comparingByValue());
    if(maybeHighestScoringWord.isEmpty()) {
      throw new RuntimeException("Highest scoring word can't be suggested without any word scores being calculated");
    }
    return maybeHighestScoringWord.get().getKey();
  }

  private int suggestHighestScoringWord(String highestScoringWord) {
    System.out.printf("Try \"%s\".\nHow many letters does that have in common with the password? (Enter 'q' to quit) ", highestScoringWord);
    return getInputIntegerOrQuit();
  }

  private int getInputIntegerOrQuit() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String input;
    try {
      input = reader.readLine();
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    if(input.toLowerCase().contains("q")) {
      shouldQuit = true;
      return 0; // doesn't matter
    }
    return Integer.parseInt(input);
  }

  private List<String> getMatchingWords(String guessedWord, int lettersInCommon) {
    return commonLettersMap.getOrDefault(guessedWord, new HashMap<>()).getOrDefault(lettersInCommon, new ArrayList<>());
  }

  private void printWordList() {
    StringJoiner joiner = new StringJoiner(", ");
    words.forEach(joiner::add);
    System.out.printf("Matching words: %s\n", joiner);
  }

  private static List<String> readPasswordList(String filename) throws IOException {
    try(Stream<String> lineStream = Files.lines(Paths.get(filename))) {
      return lineStream.collect(Collectors.toList());
    }
  }

  public static void main(String[] args) {
    List<String> passwordList;
    try {
      passwordList = readPasswordList(PASSWORDS_FILE);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    final int wordLength = passwordList.get(0).length();
    FalloutTerminalSolver solver = new FalloutTerminalSolver(wordLength, passwordList);
    while(true) {
      solver.createCommonLettersMap();
      solver.computeWordScores();
      String highestScoringWord = solver.getHighestScoringWord();
      int lettersInCommon = solver.suggestHighestScoringWord(highestScoringWord);
      if (solver.shouldQuit()) {
        return;
      }
      List<String> newWordList = solver.getMatchingWords(highestScoringWord, lettersInCommon);
      if (newWordList.isEmpty()) {
        throw new RuntimeException(
          String.format(
            "No words in the input list have %d letters in common with \"%s\".",
            lettersInCommon,
            highestScoringWord
          )
        );
      }
      if(newWordList.size() == 1) {
        System.out.printf("Solution is \"%s\".\n", newWordList.get(0));
        return;
      }
      solver = new FalloutTerminalSolver(wordLength, newWordList);
      solver.printWordList();
    }
  }
}
