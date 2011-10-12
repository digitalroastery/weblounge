package ch.entwine.weblounge.bridge.oaipmh;

import ch.entwine.weblounge.bridge.oaipmh.harvester.RecordHandler;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.language.LanguageImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.site.Site;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class WebloungeRecordHandler implements RecordHandler {

  /** the site */
  protected final Site site;

  /** The content repository */
  protected final WritableContentRepository contentRepository;

  /** ISO3 language map */
  protected final Map<String, Language> iso3Languages = new HashMap<String, Language>();

  public WebloungeRecordHandler(Site site,
      WritableContentRepository contentRepository) {
    this.site = site;
    this.contentRepository = contentRepository;
  }

  /**
   * Parse a matterhorn iso3 language string to a weblounge language.
   * 
   * @param languageCode
   *          the matterhorn iso3 language
   * @return the weblounge language
   * @throws UnknownLanguageException
   *           if language was not found
   */
  protected Language getISO3Language(String languageCode)
      throws UnknownLanguageException {
    Language language = iso3Languages.get(languageCode);
    if (language != null)
      return language;
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getISO3Language().equals(languageCode)) {
        language = new LanguageImpl(new Locale(locale.getLanguage(), "", ""));
        iso3Languages.put(languageCode, language);
        break;
      }
    }
    if (language == null)
      throw new UnknownLanguageException(languageCode);
    return language;
  }

}