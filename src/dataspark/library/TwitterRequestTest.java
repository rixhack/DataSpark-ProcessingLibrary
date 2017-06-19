package dataspark.library;

import static org.junit.Assert.*;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TwitterRequestTest extends ReceiverAdapter {
	private static ThreadLocal<TwitterRequest> tr = new ThreadLocal<TwitterRequest>(); // = new TwitterDataHandler("");
	JChannel channel;
	
	@BeforeClass
	public static void setDataHandler(){
		tr.set(new TwitterRequest(null, "eindhoven", 15));
	}
	
	@Before
	public void setChannel() throws Exception{
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("TwitterDataCluster");
		channel.setDiscardOwnMessages(true);
	}

	@Test
	public void test1JChannel() {
		assertEquals("TwitterDataCluster", tr.get().channel.getClusterName());
	}
	
	@Test
	public void test2Default() {
		assertEquals("eindhoven", tr.get().query);
		assertEquals(0, tr.get().pValue);
		assertNull(tr.get().parent);
		assertEquals(0, tr.get().getValue());
	}
	
	@Test
	public void test3MinValue(){
		tr.get().setMinValue(3);
		assertEquals(0, tr.get().pValue);
		assertEquals(3, tr.get().getValue());
	}
	
	@Test
	public void test4MaxValue(){
		tr.get().setMinValue(-10);
		tr.get().setMaxValue(-2);
		assertEquals(0, tr.get().pValue);
		assertEquals(-2, tr.get().getValue());
	}
	
	@Test
	public void testValueUpdatedAfterMessage() throws Exception{
		tr.get().setMinValue(0);
		tr.get().setMaxValue(10);
		int newValue = 5;
		Message msg = new Message(null, newValue);
		channel.send(msg);
		Thread.sleep(1000);
		assertEquals(5, tr.get().pValue);
		assertEquals(5, tr.get().getValue());
	}
}
