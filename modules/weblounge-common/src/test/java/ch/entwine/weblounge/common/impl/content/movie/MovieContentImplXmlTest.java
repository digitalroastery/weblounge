package ch.entwine.weblounge.common.impl.content.movie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.util.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Test case for class
 * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl}.
 */
public class MovieContentImplXmlTest extends MovieContentImplTest {

  /** Name of the test file */
  protected String testFile = "/moviecontent.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    URL testContext = this.getClass().getResource(testFile);
    MovieContentReader reader = new MovieContentReader();
    movie = reader.createFromXml(testContext.openStream());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#toXml()}
   * .
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    try {
      assertEquals(testXml, new String(movie.toXml().getBytes("utf-8"), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
