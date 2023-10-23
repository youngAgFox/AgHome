import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.ag.database.InvalidParameterException;
import com.ag.database.InventoryItem;
import com.ag.database.ParameterHelper;
import com.ag.database.ParameterHelper.Convertor;

public class ParameterHelperTest {
    
    @ParameterizedTest @CsvSource(value = {
        "[,]::",
        "[Hey,Testing \"Quoted\"]:Hey:Testing \"Quoted\"",
        "[\"Quoted, field, yeah\",\"\"]:Quoted, field, yeah:"
    }, delimiter = ':' )
    public void testListConvertorValidStrings(String source, String exp1, String exp2) {
        if (null == exp1) {
            exp1 = "";
        }
        if (null == exp2) {
            exp2 = "";
        }
        Convertor<String, List<String>> testConvertor = ParameterHelper.getListConvertor(ParameterHelper.getStringConvertor());
        List<String> results = testConvertor.convert(source);
        assertEquals(2, results.size(), "Results: " + results.toString());
        assertEquals(exp1, results.get(0));
        assertEquals(exp2, results.get(1));
    }

    @ParameterizedTest @ValueSource(strings = {
        "\"Testing unmatched", "Testing,\"Unmatched quote"
    })
    public void testListConvertorInvalidStrings(String source) {
        Convertor<String, List<String>> testConvertor = ParameterHelper.getListConvertor(ParameterHelper.getStringConvertor());
        assertThrows(RuntimeException.class, () -> testConvertor.convert(source));
    }

    @ParameterizedTest @ValueSource(strings = {
        "[0,1,2,3]", "[-1032,23421,23,21]", "[235233,23,-23423,0]", "[\"2323\",'-1234',\"57\",'23']" 
    })
    public void testIntListConvertor(String source) {
        Convertor<String, List<Integer>> convertor = ParameterHelper.getListConvertor(ParameterHelper.getIntConvertor());
        List<Integer> ints = convertor.convert(source);
        assertEquals(4, ints.size());
    }

    @Test
    public void testCreateParameterMap() {
        double d = 0.75;
        boolean b = true;
        int i = 2323;
        String s = "Testing a string, here";
        Boolean nb = null;
        Map<String, String> testMap = new HashMap<>();
        testMap.put("i", String.valueOf(i));
        testMap.put("b", String.valueOf(b));
        testMap.put("nb", String.valueOf(nb));

        List<Integer> testList = new ArrayList<>();
        testList.add(-23);
        testList.add(2323);
        testList.add(1009823);

        Boolean[] testArray = {true, true, false, false, true, false, null};

        Map<String, String> results = ParameterHelper.createStringValueMap(
            "d", d,
            "b", b,
            "i", i,
            "s", s,
            "nb", nb,
            "testMap", testMap,
            "testList", testList,
            "testArray", testArray
        );
        System.out.println(results);
    }

    @Test
    public void testToArgs() {
        Map<String, String> params = ParameterHelper.createStringValueMap(
            "name", null,
            "quantity", 3,
            "date", Calendar.getInstance().getTime()
        );
        InventoryItem item = new InventoryItem();
        assertThrows(InvalidParameterException.class, () -> item.initialize(params));
        params.put("name", "testName");

        item.initialize(params);

        assertEquals("testName", item.getName());
        assertEquals(3, item.getQuantity());
        // System.out.println(item.getDate());
    }
}
