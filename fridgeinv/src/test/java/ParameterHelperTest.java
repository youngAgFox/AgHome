import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ag.DynamicObject;
import com.ag.DynamicType;
import com.ag.Network.ServerCommandHandler;
import com.ag.database.FlatFileStorerFactory;
import com.ag.database.InventoryItem;
import com.ag.database.Store;
import com.ag.database.Storer;
import com.ag.json.JsonClass;
import com.ag.json.JsonFormatter;
import com.ag.json.JsonParser;
import com.ag.util.DateUtils;

public class ParameterHelperTest {

    @Test
    public void testPrimitiveFormatter() {
        JsonFormatter formatter = new JsonFormatter();
        int i = 32;
        boolean b = true;
        String s = "Test string";
        String ns = null;
        assertEquals("null", formatter.format(ns));
        assertEquals("32", formatter.format(i));
        assertEquals("true", formatter.format(b));
        assertEquals("\"Test string\"", formatter.format(s));
    }

    @Test
    public void testDOFormatter() {
        JsonFormatter formatter = new JsonFormatter();
        DynamicObject dto = new DynamicObject(DynamicType.VALUE);
        assertEquals("null", formatter.format(dto));
        dto.set(32);
        assertEquals("32", formatter.format(dto));
        dto.set("32");
        assertEquals("\"32\"", formatter.format(dto));
        dto.set(true);
        assertEquals("true", formatter.format(dto));
        dto.set(false);
        assertEquals("false", formatter.format(dto));

        dto = new DynamicObject(DynamicType.ARRAY);
        assertEquals("[]", formatter.format(dto));

        dto.add(32);
        assertEquals("[32]", formatter.format(dto));
        dto.add("32");
        dto.add("test string");
        assertEquals("[32,\"32\",\"test string\"]", formatter.format(dto));

        boolean[] t = {false, false, true, false};
        dto.add(t);
        assertEquals("[32,\"32\",\"test string\",[false,false,true,false]]", formatter.format(dto));
        
    }
    
    @Test
    public void testJsonParser() {
        JsonParser parser = new JsonParser();
        DynamicObject arr = parser.parse("[0,1,2,34,-23,2]");
        DynamicObject obj = parser.parse("{\"test\":34234, \"test2\" : null,\"last\":\"last,:test\",\"quoted\":\"\\\"Hi there chief\\\"\"}");
        System.out.println(arr);
        System.out.println(obj);
    }

    @Test
    public void testJsonParserError() {
        JsonParser parser = new JsonParser();
        // JsonEntity error2 = parser.parse("[0},\n3,2]");
        DynamicObject obj = parser.parse("{\"quoted\":\n\"\\\"a\\\"}");
    }

    @Test
    public void testGenericPersistenceAndInit() {
        DynamicObject invModel = new DynamicObject(DynamicType.OBJECT);
        String dateString = "2023-01-01T23:59:59";
        invModel.put("name", "TEST NAME");
        invModel.put("lastAdded", dateString);
        invModel.put("quantity", 23);

        InventoryItem item = ServerCommandHandler.getInstance().createAndSaveStorable(InventoryItem.class, invModel);
        Date date = DateUtils.parseDate(dateString);

        assertEquals("TEST NAME", item.getName());
        assertEquals(date, item.getLastAdded());
        assertEquals(23, item.getQuantity());
    }

    @JsonClass
    public static class TestInit {
        private int[] dice;
        private boolean[] isRolling;
        private Long[] ids;

        private Map<String, Long> testMap;
        private DynamicObject testSubInit;

        private Collection<String> words;
        private List<String> conversations;

        public TestInit() {}
    }

    @Test
    public void testComplicatedInit() {
        DynamicObject model = new DynamicObject(DynamicType.OBJECT);
        DynamicObject dice = model.putArray("dice");
        dice.add(3).add(6).add(9);
        DynamicObject rolling = model.putArray("isRolling");
        rolling.add(true).add(false).add(false);
        DynamicObject ids = model.putArray("ids");
        ids.add(7L).add(3L).add(9L);

        TestInit init = new TestInit();
        JsonParser parser = new JsonParser();
        try {
            parser.initializeInstance(init, model);
        } catch (Exception e) {
            fail(e);
        }
        DynamicObject diceModel = model.getDynamicObject("dice");
        DynamicObject rollingModel = model.getDynamicObject("isRolling");
        DynamicObject idsModel = model.getDynamicObject("ids");

        // test model
        assertEquals(3, diceModel.get(0));
        assertEquals(6, diceModel.get(1));
        assertEquals(9, diceModel.get(2));

        assertEquals(true, rollingModel.get(0));
        assertEquals(false, rollingModel.get(1));
        assertEquals(false, rollingModel.get(2));

        assertEquals(7L, idsModel.get(0));
        assertEquals(3L, idsModel.get(1));
        assertEquals(9L, idsModel.get(2));

        // test init
        assertEquals(3, init.dice[0]);
        assertEquals(6, init.dice[1]);
        assertEquals(9, init.dice[2]);

        assertEquals(true, init.isRolling[0]);
        assertEquals(false, init.isRolling[1]);
        assertEquals(false, init.isRolling[2]);

        assertEquals(7L, init.ids[0]);
        assertEquals(3, init.ids[1]);
        assertEquals(9L, init.ids[2]);
    }

    @Test
    public void testComplicatedFormatInit() {

        JsonParser parser = new JsonParser();
        TestInit init = null;
        
        try {
            init = parser.initialize(TestInit.class, 
                  "{ "
                + " \"dice\": [3, 6, 9], "
                + " \"isRolling\": [true, false, false], "
                + " \"ids\": [7, 3, 9]"
                + "} "
            );
        } catch (Exception e) {
            fail(e);
        }

        assertNotNull(init);
        assertNotNull(init.dice);
        assertNotNull(init.isRolling);
        assertNotNull(init.ids);

        assertEquals(3, init.dice[0]);
        assertEquals(6, init.dice[1]);
        assertEquals(9, init.dice[2]);

        assertEquals(true, init.isRolling[0]);
        assertEquals(false, init.isRolling[1]);
        assertEquals(false, init.isRolling[2]);

        assertEquals(7L, init.ids[0]);
        assertEquals(3, init.ids[1]);
        assertEquals(9L, init.ids[2]);
    }

    @Test
    public void arrayInit() {
        JsonParser parser = new JsonParser();
        int[] expected = {1, 5, 63, -34, 32};
        int[] ints = parser.initialize(int[].class, 
            "[1, 5, 63, -34, 32]");
            
        assertArrayEquals(expected, ints);
    }

    @Test
    public void testStoreable() throws IOException {
        Storer<Store> storeStorer = FlatFileStorerFactory.getStorer(Store.class);
        List<Store> stores = storeStorer.loadAllId(storeStorer.getIds());
        System.out.println(stores);
    }

    @Test
    public void testFormatObject() {
        DynamicObject model = new DynamicObject(DynamicType.OBJECT);
        DynamicObject dice = model.putArray("dice");
        dice.add(3).add(6).add(9);
        DynamicObject rolling = model.putArray("isRolling");
        rolling.add(true).add(false).add(false);
        DynamicObject ids = model.putArray("ids");
        ids.add(7L).add(3L).add(9L);

        JsonFormatter formatter = new JsonFormatter();
        System.out.println(formatter.format(model));
        System.out.println(formatter.format(model.getProperties()));

        JsonParser parser = new JsonParser();
        TestInit init = null;
        
        try {
            init = parser.initialize(TestInit.class, 
                  "{ "
                + " \"dice\": [3, 6, 9], "
                + " \"isRolling\": [true, false, false], "
                + " \"ids\": [7, 3, 9]"
                + "} "
            );
        } catch (Exception e) {
            fail(e);
        }

        String format = formatter.format(init);
        System.out.println("format: " + format);
        DynamicObject testPostFormat = parser.parse(format);
        System.out.println("post format: " + formatter.format(testPostFormat));
    }

}
