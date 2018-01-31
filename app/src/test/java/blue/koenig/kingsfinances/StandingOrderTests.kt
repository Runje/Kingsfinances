package blue.koenig.kingsfinances

import junit.framework.Assert
import org.junit.Test
import java.util.*

/**
 * Created by Thomas on 28.01.2018.
 */
class StandingOrderTests {

    @Test
    fun makeDeterministicUUIDs() {
        val newUuid = UUID.nameUUIDFromBytes("0600ed53-1e03-421d-a6ee-60a6244d3a93".toByteArray())
        val newUuid2 = UUID.nameUUIDFromBytes("0600ed53-1e03-421d-a6ee-60a6244d3a93".toByteArray())
        Assert.assertEquals(newUuid, newUuid2)
        Assert.assertTrue(!newUuid.toString().equals("0600ed53-1e03-421d-a6ee-60a6244d3a93"))

    }
}