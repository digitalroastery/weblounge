package ch.entwine.weblounge.tools.importer;

import org.apache.commons.io.FilenameUtils;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class ImporterUtils {

  private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

  private static final Pattern EEQUIVALENTS = Pattern.compile("[ǝƏ]+");
  private static final Pattern IEQUIVALENTS = Pattern.compile("[ı]+");
  private static final Pattern DEQUIVALENTS = Pattern.compile("[Ððđ]+");
  private static final Pattern OEQUIVALENTS = Pattern.compile("[Øø]+");
  private static final Pattern LEQUIVALENTS = Pattern.compile("[Ł]+");

  // all spaces, non-ascii and punctuation characters except _ and -
  private static final Pattern CRAP = Pattern.compile("[\\p{IsSpace}\\P{IsASCII}\\p{IsP}\\+&&[^_]]");
  private static final Pattern SEPARATORS = Pattern.compile("[\\p{IsSpace}/`-]");

  private static final CharsetEncoder ASCII_ENCODER = Charset.forName("ISO-8859-1").newEncoder();

  /**
   * Returns true when the input test contains only characters from the ASCII
   * set, false otherwise.
   */
  public static boolean isPureAscii(String text) {
    return ASCII_ENCODER.canEncode(text);
  }

  /**
   * Replaces all characters that normalize into two characters with their base
   * symbol (e.g. ü -> u)
   */
  public static String replaceCombiningDiacriticalMarks(String text) {
    return DIACRITICS_AND_FRIENDS.matcher(Normalizer.normalize(text, Normalizer.Form.NFKD)).replaceAll("");
  }

  /**
   * Turns the input string into a url friendly variant (containing only
   * alphanumeric characters and '-' and '_').
   */
  public static String urlFriendly(String unfriendlyString) {
    return removeCrappyCharacters(replaceEquivalentsOfSymbols(replaceCombiningDiacriticalMarks(

    replaceSeparatorsWithUnderscores(unfriendlyString.trim())))).toLowerCase();
  }

  private static String replaceEquivalentsOfSymbols(String unfriendlyString) {
    return LEQUIVALENTS.matcher(OEQUIVALENTS.matcher(DEQUIVALENTS.matcher(IEQUIVALENTS.matcher(EEQUIVALENTS.matcher(unfriendlyString).replaceAll("e")).replaceAll("i")).replaceAll("d")).replaceAll("o")).replaceAll("l");
  }

  private static String removeCrappyCharacters(String unfriendlyString) {
    return CRAP.matcher(unfriendlyString).replaceAll("");
  }

  private static String replaceSeparatorsWithUnderscores(String unfriendlyString) {
    return SEPARATORS.matcher(unfriendlyString).replaceAll("_");
  }
}