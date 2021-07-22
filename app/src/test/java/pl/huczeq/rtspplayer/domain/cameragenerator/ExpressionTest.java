package pl.huczeq.rtspplayer.domain.cameragenerator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.domain.cameragenerator.expression.Expression;

@RunWith(RobolectricTestRunner.class)
public class ExpressionTest {

    @Test
    public void test_oneParameter() {
        Map<String, String> variables = new HashMap<>();
        variables.put("par1", "1-4");
        Expression expression = new Expression("abcd{par1}", variables);
        List<Map<String, String>> data = expression.generateVariations();
        assertEquals(4, data.size());
        for(int i = 0; i < data.size(); i++) {
            assertEquals(String.valueOf(i+1), data.get(i).get("par1"));
        }
    }
}
