/* An OutputStream that, instead of writing its output to a file or anything,
 * feeds the output into a hash-function. This lets us tell whether two test-runs
 * printed the same stuff to stdout/stderr, without having to store their
 * entire output (which, with mutants, can be huge).
 */

package killmap.runners.isolation;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.DigestOutputStream;

public class DeadEndDigestOutputStream extends DigestOutputStream {

  private static class DeadEndStream extends OutputStream {
    public void write(int b) {}
    public DeadEndStream() {}
  }

  public DeadEndDigestOutputStream() {
    super(new DeadEndStream(), null);
    try {
      setMessageDigest(MessageDigest.getInstance("SHA-1"));
    }
    catch (java.security.NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println("According to http://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html , java.security.MessageDigest *must* provide SHA-1. What happened?");
      System.exit(1);
    }
  }

  public String getDigestString() {
    // Gives the hash as a typical hex string.
    byte[] digestBytes = getMessageDigest().digest();
    StringBuffer sb = new StringBuffer();
    for (byte b : digestBytes) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }
}