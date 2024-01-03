package pl.huczeq.rtspplayer.domain.cameragenerator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.huczeq.rtspplayer.domain.cameragenerator.expression.ExpressionHelper;
import pl.huczeq.rtspplayer.domain.cameragenerator.expression.ProcessedVariable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExpressionHelperTest {

    private final List<String> specialNames = List.of("i");

    @Test
    public void expressionHelper_splitExpression_test() {
        ExpressionHelper<String> helper = new ExpressionHelper<>();
        HashMap<String, String> vars = new HashMap<>();
        expressionHelper_splitExpression_testHelper(helper, "{var1}abc", vars, new String[]{"{var1}abc"});
        expressionHelper_splitExpression_testHelper(helper, "{var1", vars, new String[]{"{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1}xd2{var2xd}elo2", vars, new String[]{"elo{var1}xd2{var2xd}elo2"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1", vars, new String[]{"elo{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo}var1", vars, new String[]{"elo}var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{}var1", vars, new String[]{"elo{}var1"});
        expressionHelper_splitExpression_testHelper(helper, "", vars, new String[]{});

        vars.put("var1", "value");
        expressionHelper_splitExpression_testHelper(helper, "{var1}abc", vars, new String[]{"{var1","abc"});
        expressionHelper_splitExpression_testHelper(helper, "var1{i}", vars, new String[]{"var1","{i"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1}xd2{var2xd}elo2", vars, new String[]{"elo","{var1","xd2{var2xd}elo2"});
        expressionHelper_splitExpression_testHelper(helper, "elo{var1", vars, new String[]{"elo{var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo}var1", vars, new String[]{"elo}var1"});
        expressionHelper_splitExpression_testHelper(helper, "elo{}var1", vars, new String[]{"elo{}var1"});
        expressionHelper_splitExpression_testHelper(helper, "", vars, new String[]{});
    }

    private void expressionHelper_splitExpression_testHelper(ExpressionHelper<String> helper, String expression, HashMap<String, String> variables, String[] expectedParts) {
        List<String> parts = helper.splitExpression(expression, variables, specialNames);
        assertArrayEquals(expectedParts, parts.toArray());
    }

    @Test
    public void expressionHelper_generateVariablesMap_test() {
        ExpressionHelper<String> helper = new ExpressionHelper<>();
        HashMap<String, String> vars = new HashMap<>();
        vars.put("var1", "1");
        expressionHelper_generateVariablesMap_testHelper(helper, "test{var1}xd{var1}", vars);
    }

    private void expressionHelper_generateVariablesMap_testHelper(ExpressionHelper<String> helper, String expression, HashMap<String, String> variables) {
        List<ProcessedVariable> allVariables = new ArrayList<>();
        HashMap<Integer, ProcessedVariable> variablesMap = new HashMap<>();
        helper.prepareVariableMaps(helper.splitExpression(expression, variables, specialNames), variables, allVariables, variablesMap);
        assertEquals(allVariables.size(), 1);
        assertEquals(variablesMap.size(), 2);
        int i = 1;
        for(Map.Entry<Integer,ProcessedVariable> entry : variablesMap.entrySet()) {
            assertTrue(entry.getKey() == i);
            assertTrue(entry.getValue().getName().equals("var1"));
            assertTrue(entry.getValue() == allVariables.get(0));
            i += 2;
        }
    }

    @Test
    public void expressionHelper_loadDataToExpression_test() {
        String expression = "rtsp://127.0.0.1:558/camera/{n}";
        Map<String, String> patternData = Map.of("n", "3");
        String results = ExpressionHelper.loadDataToExpression(expression, patternData, specialNames);
        assertEquals("rtsp://127.0.0.1:558/camera/3", results);
    }
}
