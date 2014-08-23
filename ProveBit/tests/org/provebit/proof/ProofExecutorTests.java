package org.provebit.proof;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Test;

import com.google.bitcoin.core.Utils;

public class ProofExecutorTests {

	@Test
	public void testExecuteCat() {
		Object[] cats = {
				new Object[] {"op_cat", "s:a", "s:b"},
				new Object[] {"op_cat", "s:c", "m:-1"},
				new Object[] {"op_cat", "s:bag"},
				new Object[] {"op_cat", "h:65"}
		};
		ProofParser pp = new ProofParser(cats);
		InputStream dummy = new ByteArrayInputStream(new byte[] {});
		ProofExecutor pe = new ProofExecutor(dummy);
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("cabbage", out);
	}
	
	@Test
	public void testExecuteSHA2() {
		Object[] shax = {
				new Object[] {"op_sha256", "s:test", "m:-1", "s:stuff"},
		};
		ProofParser pp = new ProofParser(shax);
		String in = "ing ";
		InputStream text = null;
		try {
			text = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(text);
		byte[] res = Utils.reverseBytes(pe.execute(pp));
		String out = Utils.bytesToHexString(res);
		assertEquals("3367a47f48cd5948e68ed649f74d6fcc60725a881593543ae646a9b63f55fe19", out);
	}
	
	@Test
	public void testRev() {
		Object[] rev = {
				new Object[] {"op_rev"},
		};
		ProofParser pp = new ProofParser(rev);
		String in = "this is a test";
		InputStream text = null;
		try {
			text = new ByteArrayInputStream(in.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ProofExecutor pe = new ProofExecutor(text);
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("tset a si siht", out);
	}
	
	@Test
	public void testFileInput() {
		Object[] shan = {
				new Object[] {"op_sha256"},
		};
		ProofParser pp = new ProofParser(shan);
		InputStream file = getClass().getResourceAsStream("btc-logo.png");
		
		ProofExecutor pe = new ProofExecutor(file);
		byte[] res = Utils.reverseBytes(pe.execute(pp));
		String out = Utils.bytesToHexString(res);
		assertEquals("11961d79a8fde725e878473bd3497adff1fb6d362c1378e9eb182c870a617a2a", out);
		
		Object[] shan2 = {
				new Object[] {"op_sha256", "s:a", "m:-1", "s:b"},
		};
		ProofParser pp2 = new ProofParser(shan2);
		InputStream file2 = getClass().getResourceAsStream("btc-logo.png");
		
		ProofExecutor pe2 = new ProofExecutor(file2);
		byte[] res2 = Utils.reverseBytes(pe2.execute(pp2));
		String out2 = Utils.bytesToHexString(res2);
		// (echo -n a; cat btc-logo.png; echo -n b) | sha256sum
		assertEquals("99788d6eb2d9938ed6d455f9af4e9a8ac7e1fc3b5d146a27ee20dc10b22e9ccb", out2);
	}
	
	@Test
	public void testFunctions() {
		Object[] shan = {
				new Object[] {"op_func", "cats", new Object[] {
						new Object[] {"op_cat", "m:1", "s:s"}
				}},
				new Object[] {"op_func", "words", new Object[] {
						new Object[] {"op_cat", "s:This ", "m:1"}, // 'This test'
						new Object[] {"f_cats", "m:-1"}, // 'This tests'
						new Object[] {"op_store", 32},
						new Object[] {"f_cats", "m:2"}, // 'things'
						new Object[] {"op_cat", "s: ", "m:-1"}, // ' things'
						new Object[] {"op_cat", "m:32", "m:-1"}, // 'This tests things'
						new Object[] {"op_cat", "s:."} // 'This tests things.'
				}},
				new Object[] {"f_words", "s:test", "s:thing"}
		};
		ProofParser pp = new ProofParser(shan);		
		ProofExecutor pe = new ProofExecutor("");
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("This tests things.", out);
	}
	
	@Test
	public void testStoreLoadSetSwap() {
		Object[] cats = {
				new Object[] {"op_store", 35},
				new Object[] {"op_set", "s:bb"},
				new Object[] {"op_store", 36},
				new Object[] {"op_load", 35},
				new Object[] {"op_store", 37},
				new Object[] {"op_set", "s:c"},
				new Object[] {"op_cat", "m:35"},
				new Object[] {"op_cat", "m:36"},
				new Object[] {"op_cat", "m:37"},
				new Object[] {"op_swap", 36},
				new Object[] {"op_cat", "m:36"}
		};
		ProofParser pp = new ProofParser(cats);
		ProofExecutor pe = new ProofExecutor("aaa");
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("bbcaaabbaaa", out);
	}
	
	@Test
	public void testCompose() {
		Object[] cats = {
				new Object[] {"op_cat", "s:ing"},
				new Object[] {"op_compose", "s:We are testing..."},
				new Object[] {"op_cat", "s: things."}
		};
		ProofParser pp = new ProofParser(cats);
		ProofExecutor pe = new ProofExecutor("test");
		pe.restrictArbitraryStreamBuffer = false;
		byte[] res = pe.execute(pp);
		String out = new String(res);
		assertEquals("We are testing... things.", out);
		
		Object[] cats2 = {
				new Object[] {"op_cat", "s:ing"},
				new Object[] {"op_compose", "s:We are tes?ting..."},
				new Object[] {"op_cat", "s: things."}
		};
		try {
			ProofParser pp2 = new ProofParser(cats2);
			ProofExecutor pe2 = new ProofExecutor("test");
			pe2.restrictArbitraryStreamBuffer = false;
			byte[] res2 = pe2.execute(pp2);
			fail("We should not have a successful execution");
		} catch (ProofExecutor.ProgramDieException e) {
			// We should make it here
		}
	}
	
}
