package killmap.runners.isolation;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

import killmap.runners.isolation.DeadEndDigestOutputStream;

public class DeadEndDigestOutputStreamTest extends TestCase {

  @Test public void testDigestIsDeterministic() throws java.io.IOException {
    DeadEndDigestOutputStream stream1 = new DeadEndDigestOutputStream();
    DeadEndDigestOutputStream stream2 = new DeadEndDigestOutputStream();
    assertEquals(stream1.getDigestString(), stream2.getDigestString());
    stream1.write(123); stream2.write(123);
    assertEquals(stream1.getDigestString(), stream2.getDigestString());

    stream1.close();
    stream2.close();
  }

  @Test public void testDigestChangesOnWrite() throws java.io.IOException {
    DeadEndDigestOutputStream stream = new DeadEndDigestOutputStream();
    String originalDigest = stream.getDigestString();
    stream.write(123);
    assertThat(originalDigest, not(equalTo(stream.getDigestString())));

    stream.close();
  }
}
