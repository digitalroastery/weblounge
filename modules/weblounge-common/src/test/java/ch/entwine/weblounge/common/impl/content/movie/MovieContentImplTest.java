package ch.entwine.weblounge.common.impl.content.movie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.content.movie.AudioStream;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.ScanType;
import ch.entwine.weblounge.common.content.movie.Stream;
import ch.entwine.weblounge.common.content.movie.VideoStream;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for class {@link MovieContentImpl}.
 */
public class MovieContentImplTest {

  /** The movie content object to test */
  protected MovieContent movie = null;

  /** The filename */
  protected String filename = "movie.mov";

  /** The source file */
  protected String source = "http://entwinemedia.com/moviexyz.ogg";

  /** The English language */
  protected Language english = LanguageUtils.getLanguage("en");

  /** The movie size */
  protected long size = 745569L;

  /** The mime type */
  protected String mimetype = "video/quicktime";

  /** The creation date */
  protected Date creationDate = new Date(1231358741000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** The creation date */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /** The movie duration */
  protected long duration = 1004400000L;

  /** The audio bitdepth */
  protected int audioBitdepth = 24;

  /** The audio channels */
  protected int audioChannels = 2;

  /** The audio sampling rate */
  protected int audioSamplingrate = 43898;

  /** The audio bitrate */
  protected float bitrate = 3348F;

  /** The audio format */
  protected String format = "H.234";

  /** The video frame height */
  protected int frameheight = 32434;

  /** the video frame width */
  protected int framewidth = 487732;

  /** The video frame rate */
  protected float framerate = 8974F;

  /** The video scan type */
  protected ScanType scanType = ScanType.Interlaced;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    AudioStream audioStream = new AudioStreamImpl();
    audioStream.setBitDepth(audioBitdepth);
    audioStream.setBitRate(bitrate);
    audioStream.setChannels(audioChannels);
    audioStream.setFormat(format);
    audioStream.setSamplingRate(audioSamplingrate);

    VideoStream videoStream = new VideoStreamImpl();
    videoStream.setBitRate(bitrate);
    videoStream.setFormat(format);
    videoStream.setFrameHeight(frameheight);
    videoStream.setFrameRate(framerate);
    videoStream.setFrameWidth(framewidth);
    videoStream.setScanType(scanType);

    movie = new MovieContentImpl(filename, english, mimetype, size, duration);
    movie.setSource(source);
    movie.addStream(audioStream);
    movie.addStream(videoStream);
    ((MovieContentImpl) movie).setCreated(creationDate, amelie);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getLanguage()}
   * .
   */
  @Test
  public void testGetLanguage() {
    assertEquals(english, movie.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getMimetype()}
   * .
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, movie.getMimetype());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getSource()}
   * .
   */
  @Test
  public void testGetSource() {
    assertEquals(source, movie.getSource());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getFilename()}
   * .
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, movie.getFilename());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getSize()}
   * .
   */
  public void testGetSize() {
    assertEquals(size, movie.getSize());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#getDuration()}
   * .
   */
  public void testGetDuration() {
    assertEquals(duration, movie.getDuration());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#hasAudio()}
   * .
   */
  public void testHasAudioStream() {
    assertTrue(movie.hasAudio());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.movie.MovieContentImpl#hasVideo()}
   * .
   */
  public void testHasVideoStream() {
    assertTrue(movie.hasVideo());
  }

  /**
   * Test the audio stream fields
   */
  public void testAudioStream() {
    Stream[] streams = movie.getStreams();
    for (Stream stream : streams) {
      if (stream instanceof AudioStream) {
        assertEquals(audioBitdepth, ((AudioStream) stream).getBitDepth());
        assertEquals(bitrate, ((AudioStream) stream).getBitRate());
        assertEquals(format, stream.getFormat());
        assertEquals(audioChannels, ((AudioStream) stream).getChannels());
        assertEquals(audioSamplingrate, ((AudioStream) stream).getSamplingRate());
      }
    }
  }

  /**
   * Test the video stream fields
   */
  public void testVideoStream() {
    Stream[] streams = movie.getStreams();
    for (Stream stream : streams) {
      if (stream instanceof VideoStream) {
        assertEquals(bitrate, ((VideoStream) stream).getBitRate());
        assertEquals(format, stream.getFormat());
        assertEquals(frameheight, ((VideoStream) stream).getFrameHeight());
        assertEquals(framewidth, ((VideoStream) stream).getFrameWidth());
        assertEquals(framerate, ((VideoStream) stream).getFrameRate());
        assertEquals(scanType, ((VideoStream) stream).getScanType());
      }
    }
  }

}
