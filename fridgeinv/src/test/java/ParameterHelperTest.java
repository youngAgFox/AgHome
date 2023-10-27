import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ag.DynamicObject;
import com.ag.DynamicType;
import com.ag.ServerCommandHandler;
import com.ag.database.InventoryItem;
import com.ag.json.AutoInit;
import com.ag.json.JsonParser;

import util.DateUtils;

public class ParameterHelperTest {
    
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

    private class TestInit {
        private int[] dice;
        private boolean[] isRolling;
        private Long[] ids;

        private Map<String, Long> testMap;
        private DynamicObject testSubInit;

        private Collection<String> words;
        private List<String> conversations;
    }

    @Test
    public void testComplicatedInit() {
        DynamicObject model = new DynamicObject(DynamicType.OBJECT);
        DynamicObject dice = model.putArray("dice");
        dice.add(3).add(6).add(9);
        DynamicObject rolling = model.putArray("isRolling");
        rolling.add(true).add(false).add(false);
        DynamicObject ids = model.putArray("ids");
        ids.add(7L).add(3).add(9L);

        TestInit init = new TestInit();
        JsonParser parser = new JsonParser();
        try {
            parser.initializeInstance(init, model);
        } catch (Exception e) {
            fail(e);
        }
    }
}
